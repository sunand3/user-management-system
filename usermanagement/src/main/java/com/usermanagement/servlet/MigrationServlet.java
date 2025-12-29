package com.usermanagement.servlet;

import com.google.gson.Gson;
import com.usermanagement.model.User;
import com.usermanagement.service.BigQueryService;
import com.usermanagement.service.DatastoreService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/api/migration/*")
public class MigrationServlet extends HttpServlet {
    private DatastoreService datastoreService;
    private BigQueryService bigQueryService;
    private Gson gson;

    @Override
    public void init() throws ServletException {
        datastoreService = DatastoreService.getInstance();
        bigQueryService = BigQueryService.getInstance();
        gson = new Gson();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String pathInfo = request.getPathInfo();

        try {
            if (pathInfo != null && pathInfo.equals("/status")) {
                // Get migration status
                long migratedCount = bigQueryService.getMigratedUserCount();
                int totalCount = datastoreService.getUserCount();

                Map<String, Object> result = new HashMap<>();
                result.put("success", true);
                result.put("totalUsers", totalCount);
                result.put("migratedUsers", migratedCount);
                result.put("pendingUsers", totalCount - migratedCount);

                response.getWriter().write(gson.toJson(result));
            } else if (pathInfo != null && pathInfo.equals("/records")) {
                // Get migrated records
                String limitParam = request.getParameter("limit");
                int limit = limitParam != null ? Integer.parseInt(limitParam) : 100;

                List<Map<String, Object>> records = bigQueryService.getMigratedUsers(limit);

                Map<String, Object> result = new HashMap<>();
                result.put("success", true);
                result.put("records", records);
                result.put("count", records.size());

                response.getWriter().write(gson.toJson(result));
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "Error: " + e.getMessage());
            response.getWriter().write(gson.toJson(result));
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String pathInfo = request.getPathInfo();

        try {
            if (pathInfo != null && pathInfo.equals("/bulk")) {
                // Bulk migration
                List<User> users = datastoreService.getAllUsers();

                if (users.isEmpty()) {
                    Map<String, Object> result = new HashMap<>();
                    result.put("success", false);
                    result.put("message", "No users found in Datastore");
                    response.getWriter().write(gson.toJson(result));
                    return;
                }

                Map<String, Object> migrationResult = bigQueryService.bulkMigrate(users);
                migrationResult.put("success", true);
                migrationResult.put("message", "Bulk migration completed");

                response.getWriter().write(gson.toJson(migrationResult));

            } else if (pathInfo != null && pathInfo.startsWith("/user/")) {
                // Single user migration
                String userId = pathInfo.substring(6);
                User user = datastoreService.getUserById(userId);

                if (user == null) {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    Map<String, Object> result = new HashMap<>();
                    result.put("success", false);
                    result.put("message", "User not found");
                    response.getWriter().write(gson.toJson(result));
                    return;
                }

                boolean success = bigQueryService.migrateUser(user);

                Map<String, Object> result = new HashMap<>();
                if (success) {
                    result.put("success", true);
                    result.put("message", "User migrated successfully");
                } else {
                    result.put("success", false);
                    result.put("message", "Failed to migrate user");
                }

                response.getWriter().write(gson.toJson(result));

            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("message", "Invalid endpoint");
                response.getWriter().write(gson.toJson(result));
            }

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "Error: " + e.getMessage());
            response.getWriter().write(gson.toJson(result));
            e.printStackTrace();
        }
    }
}