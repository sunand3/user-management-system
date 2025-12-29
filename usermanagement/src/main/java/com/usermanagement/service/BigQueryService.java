package com.usermanagement.service;

import com.google.cloud.bigquery.*;
import com.usermanagement.model.User;

import java.text.SimpleDateFormat;
import java.util.*;

public class BigQueryService {
    private static BigQueryService instance;
    private final BigQuery bigQuery;
    private static final String DATASET_NAME = "user_management";
    private static final String TABLE_NAME = "users";
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    private BigQueryService() {
        this.bigQuery = BigQueryOptions.getDefaultInstance().getService();
        createDatasetAndTable();
    }

    public static synchronized BigQueryService getInstance() {
        if (instance == null) {
            instance = new BigQueryService();
        }
        return instance;
    }

    private void createDatasetAndTable() {
        try {
            // Create dataset if not exists
            DatasetId datasetId = DatasetId.of(DATASET_NAME);
            Dataset dataset = bigQuery.getDataset(datasetId);

            if (dataset == null) {
                DatasetInfo datasetInfo = DatasetInfo.newBuilder(datasetId).build();
                bigQuery.create(datasetInfo);
            }

            // Create table if not exists
            TableId tableId = TableId.of(DATASET_NAME, TABLE_NAME);
            Table table = bigQuery.getTable(tableId);

            if (table == null) {
                Schema schema = Schema.of(
                        Field.of("id", StandardSQLTypeName.STRING),
                        Field.of("name", StandardSQLTypeName.STRING),
                        Field.of("dob", StandardSQLTypeName.DATE),
                        Field.of("email", StandardSQLTypeName.STRING),
                        Field.of("phone", StandardSQLTypeName.STRING),
                        Field.of("gender", StandardSQLTypeName.STRING),
                        Field.of("address", StandardSQLTypeName.STRING),
                        Field.of("created_at", StandardSQLTypeName.TIMESTAMP),
                        Field.of("migrated_at", StandardSQLTypeName.TIMESTAMP)
                );

                TableDefinition tableDefinition = StandardTableDefinition.of(schema);
                TableInfo tableInfo = TableInfo.newBuilder(tableId, tableDefinition).build();
                bigQuery.create(tableInfo);
            }
        } catch (Exception e) {
            System.err.println("Error creating dataset/table: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public boolean migrateUser(User user) {
        try {
            TableId tableId = TableId.of(DATASET_NAME, TABLE_NAME);

            Map<String, Object> rowContent = new HashMap<>();
            rowContent.put("id", user.getId());
            rowContent.put("name", user.getName());
            rowContent.put("dob", dateFormat.format(user.getDob()));
            rowContent.put("email", user.getEmail());
            rowContent.put("phone", user.getPhone());
            rowContent.put("gender", user.getGender());
            rowContent.put("address", user.getAddress());
            rowContent.put("created_at", user.getCreatedAt().getTime() / 1000.0);
            rowContent.put("migrated_at", System.currentTimeMillis() / 1000.0);

            InsertAllRequest insertRequest = InsertAllRequest.newBuilder(tableId)
                    .addRow(rowContent)
                    .build();

            InsertAllResponse response = bigQuery.insertAll(insertRequest);

            if (response.hasErrors()) {
                response.getInsertErrors().forEach((key, errors) -> {
                    System.err.println("Errors for row " + key + ":");
                    errors.forEach(error -> System.err.println("  " + error.getMessage()));
                });
                return false;
            }

            return true;
        } catch (Exception e) {
            System.err.println("Error migrating user: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public Map<String, Object> bulkMigrate(List<User> users) {
        Map<String, Object> result = new HashMap<>();
        int successCount = 0;
        int failCount = 0;
        List<String> errors = new ArrayList<>();

        for (User user : users) {
            boolean success = migrateUser(user);
            if (success) {
                successCount++;
            } else {
                failCount++;
                errors.add("Failed to migrate user: " + user.getEmail());
            }
        }

        result.put("total", users.size());
        result.put("success", successCount);
        result.put("failed", failCount);
        result.put("errors", errors);

        return result;
    }

    public long getMigratedUserCount() {
        try {
            String query = "SELECT COUNT(*) as count FROM `" + DATASET_NAME + "." + TABLE_NAME + "`";
            QueryJobConfiguration queryConfig = QueryJobConfiguration.newBuilder(query).build();
            TableResult result = bigQuery.query(queryConfig);

            for (FieldValueList row : result.iterateAll()) {
                return row.get("count").getLongValue();
            }
        } catch (Exception e) {
            System.err.println("Error getting migrated user count: " + e.getMessage());
        }

        return 0;
    }

    public List<Map<String, Object>> getMigratedUsers(int limit) {
        List<Map<String, Object>> users = new ArrayList<>();

        try {
            String query = "SELECT * FROM `" + DATASET_NAME + "." + TABLE_NAME + "` LIMIT " + limit;
            QueryJobConfiguration queryConfig = QueryJobConfiguration.newBuilder(query).build();
            TableResult result = bigQuery.query(queryConfig);

            for (FieldValueList row : result.iterateAll()) {
                Map<String, Object> user = new HashMap<>();
                user.put("id", row.get("id").getStringValue());
                user.put("name", row.get("name").getStringValue());
                user.put("email", row.get("email").getStringValue());
                user.put("phone", row.get("phone").getStringValue());
                users.add(user);
            }
        } catch (Exception e) {
            System.err.println("Error getting migrated users: " + e.getMessage());
        }

        return users;
    }
}