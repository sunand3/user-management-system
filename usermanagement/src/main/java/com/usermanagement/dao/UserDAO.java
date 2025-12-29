package com.usermanagement.dao;

import com.google.cloud.Timestamp;
import com.google.cloud.datastore.*;
import com.usermanagement.model.User;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class UserDAO {
    private final Datastore datastore;
    private static final String KIND = "User";
    private final KeyFactory keyFactory;

    public UserDAO() {
        this.datastore = DatastoreOptions.getDefaultInstance().getService();
        this.keyFactory = datastore.newKeyFactory().setKind(KIND);
    }


    public String create(User user) throws Exception {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }

        if (emailExists(user.getEmail())) {
            throw new Exception("Email already exists");
        }

        Key key = datastore.allocateId(keyFactory.newKey());

        Entity entity = Entity.newBuilder(key)
                .set("name", user.getName())
                .set("dob", Timestamp.of(user.getDob()))
                .set("email", user.getEmail())
                .set("password", user.getPassword())
                .set("phone", user.getPhone())
                .set("gender", user.getGender())
                .set("address", user.getAddress())
                .set("createdAt", Timestamp.now())
                .set("updatedAt", Timestamp.now())
                .build();

        datastore.put(entity);
        return key.getId().toString();
    }

    /**
     * Find user by ID
     */
    public User findById(String id) {
        try {
            Key key = keyFactory.newKey(Long.parseLong(id));
            Entity entity = datastore.get(key);
            return entity != null ? entityToUser(entity) : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }


    public User findByEmail(String email) {
        Query<Entity> query = Query.newEntityQueryBuilder()
                .setKind(KIND)
                .setFilter(StructuredQuery.PropertyFilter.eq("email", email))
                .setLimit(1)
                .build();

        QueryResults<Entity> results = datastore.run(query);
        return results.hasNext() ? entityToUser(results.next()) : null;
    }

    public List<User> findAll() {
        Query<Entity> query = Query.newEntityQueryBuilder()
                .setKind(KIND)
                .setOrderBy(StructuredQuery.OrderBy.desc("createdAt"))
                .build();

        QueryResults<Entity> results = datastore.run(query);
        List<User> users = new ArrayList<>();

        while (results.hasNext()) {
            users.add(entityToUser(results.next()));
        }

        return users;
    }

    public List<User> findAll(int limit, int offset) {
        Query<Entity> query = Query.newEntityQueryBuilder()
                .setKind(KIND)
                .setOrderBy(StructuredQuery.OrderBy.desc("createdAt"))
                .setLimit(limit)
                .setOffset(offset)
                .build();

        QueryResults<Entity> results = datastore.run(query);
        List<User> users = new ArrayList<>();

        while (results.hasNext()) {
            users.add(entityToUser(results.next()));
        }

        return users;
    }


    public List<User> search(String searchTerm) {
        List<User> allUsers = findAll();
        List<User> filteredUsers = new ArrayList<>();

        String lowerSearchTerm = searchTerm.toLowerCase();

        for (User user : allUsers) {
            if (user.getName().toLowerCase().contains(lowerSearchTerm) ||
                    user.getEmail().toLowerCase().contains(lowerSearchTerm) ||
                    user.getPhone().contains(searchTerm)) {
                filteredUsers.add(user);
            }
        }

        return filteredUsers;
    }


    public boolean update(String id, User updatedUser) {
        try {
            Key key = keyFactory.newKey(Long.parseLong(id));
            Entity existingEntity = datastore.get(key);

            if (existingEntity == null) {
                return false;
            }

            String currentEmail = existingEntity.getString("email");
            if (!currentEmail.equals(updatedUser.getEmail()) && emailExists(updatedUser.getEmail())) {
                return false;
            }

            Entity entity = Entity.newBuilder(key)
                    .set("name", updatedUser.getName())
                    .set("dob", Timestamp.of(updatedUser.getDob()))
                    .set("email", updatedUser.getEmail())
                    .set("password", updatedUser.getPassword())
                    .set("phone", updatedUser.getPhone())
                    .set("gender", updatedUser.getGender())
                    .set("address", updatedUser.getAddress())
                    .set("createdAt", existingEntity.getTimestamp("createdAt"))
                    .set("updatedAt", Timestamp.now())
                    .build();

            datastore.put(entity);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public boolean delete(String id) {
        try {
            Key key = keyFactory.newKey(Long.parseLong(id));
            Entity entity = datastore.get(key);

            if (entity == null) {
                return false;
            }

            datastore.delete(key);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public boolean emailExists(String email) {
        return findByEmail(email) != null;
    }


    public int count() {
        Query<Key> query = Query.newKeyQueryBuilder()
                .setKind(KIND)
                .build();

        QueryResults<Key> results = datastore.run(query);
        int count = 0;

        while (results.hasNext()) {
            results.next();
            count++;
        }

        return count;
    }


    public int bulkCreate(List<User> users) {
        int successCount = 0;
        List<Entity> entities = new ArrayList<>();

        for (User user : users) {
            try {
                // Skip if email already exists
                if (emailExists(user.getEmail())) {
                    continue;
                }

                Key key = datastore.allocateId(keyFactory.newKey());

                Entity entity = Entity.newBuilder(key)
                        .set("name", user.getName())
                        .set("dob", Timestamp.of(user.getDob()))
                        .set("email", user.getEmail())
                        .set("password", user.getPassword())
                        .set("phone", user.getPhone())
                        .set("gender", user.getGender())
                        .set("address", user.getAddress())
                        .set("createdAt", Timestamp.now())
                        .set("updatedAt", Timestamp.now())
                        .build();

                entities.add(entity);
                successCount++;

                // Batch insert every 500 entities
                if (entities.size() >= 500) {
                    datastore.put(entities.toArray(new Entity[0]));
                    entities.clear();
                }
            } catch (Exception e) {
                System.err.println("Error creating user: " + e.getMessage());
            }
        }

        if (!entities.isEmpty()) {
            datastore.put(entities.toArray(new Entity[0]));
        }

        return successCount;
    }


    public void deleteAll() {
        Query<Key> query = Query.newKeyQueryBuilder()
                .setKind(KIND)
                .build();

        QueryResults<Key> results = datastore.run(query);
        List<Key> keys = new ArrayList<>();

        while (results.hasNext()) {
            keys.add(results.next());

            if (keys.size() >= 500) {
                datastore.delete(keys.toArray(new Key[0]));
                keys.clear();
            }
        }

        if (!keys.isEmpty()) {
            datastore.delete(keys.toArray(new Key[0]));
        }
    }

    private User entityToUser(Entity entity) {
        User user = new User();
        user.setId(entity.getKey().getId().toString());
        user.setName(entity.getString("name"));
        user.setDob(new Date(entity.getTimestamp("dob").toSqlTimestamp().getTime()));
        user.setEmail(entity.getString("email"));
        user.setPassword(entity.getString("password"));
        user.setPhone(entity.getString("phone"));
        user.setGender(entity.getString("gender"));
        user.setAddress(entity.getString("address"));
        user.setCreatedAt(new Date(entity.getTimestamp("createdAt").toSqlTimestamp().getTime()));
        user.setUpdatedAt(new Date(entity.getTimestamp("updatedAt").toSqlTimestamp().getTime()));
        return user;
    }
}