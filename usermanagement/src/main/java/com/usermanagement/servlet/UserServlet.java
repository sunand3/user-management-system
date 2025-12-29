package com.usermanagement.servlet;

import com.google.gson.Gson;
import com.usermanagement.model.User;
import com.usermanagement.service.DatastoreService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/api/users/*")
public class UserServlet extends HttpServlet {
    private DatastoreService datastoreService;
    private Gson gson;
    private SimpleDateFormat dateFormat;

    @Override
    public void init() throws ServletException {
        datastoreService = DatastoreService.getInstance();
        gson = new Gson();
        dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String pathInfo = request.getPathInfo();
        String searchQuery = request.getParameter("search");

        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                List<User> users;
                if (searchQuery != null && !searchQuery.isEmpty()) {
                    users = datastoreService.searchUsers(searchQuery);
                } else {
                    users = datastoreService.getAllUsers();
                }

                Map<String, Object> result = new HashMap<>();
                result.put("success", true);
                result.put("users", users);
                result.put("count", users.size());

                response.getWriter().write(gson.toJson(result));
            } else {
                String userId = pathInfo.substring(1);
                User user = datastoreService.getUserById(userId);

                if (user != null) {
                    Map<String, Object> result = new HashMap<>();
                    result.put("success", true);
                    result.put("user", user);
                    response.getWriter().write(gson.toJson(result));
                } else {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    Map<String, Object> result = new HashMap<>();
                    result.put("success", false);
                    result.put("message", "User not found");
                    response.getWriter().write(gson.toJson(result));
                }
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

        try {
            BufferedReader reader = request.getReader();
            User user = gson.fromJson(reader, User.class);

            // Check if email already exists
            User existingUser = datastoreService.getUserByEmail(user.getEmail());
            if (existingUser != null) {
                response.setStatus(HttpServletResponse.SC_CONFLICT);
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("message", "Email already exists");
                response.getWriter().write(gson.toJson(result));
                return;
            }

            String userId = datastoreService.createUser(user);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "User created successfully");
            result.put("userId", userId);

            response.setStatus(HttpServletResponse.SC_CREATED);
            response.getWriter().write(gson.toJson(result));

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "Error creating user: " + e.getMessage());
            response.getWriter().write(gson.toJson(result));
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String pathInfo = request.getPathInfo();

        if (pathInfo == null || pathInfo.equals("/")) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "User ID is required");
            response.getWriter().write(gson.toJson(result));
            return;
        }

        try {
            String userId = pathInfo.substring(1);
            BufferedReader reader = request.getReader();
            User updatedUser = gson.fromJson(reader, User.class);

            boolean success = datastoreService.updateUser(userId, updatedUser);

            if (success) {
                Map<String, Object> result = new HashMap<>();
                result.put("success", true);
                result.put("message", "User updated successfully");
                response.getWriter().write(gson.toJson(result));
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("message", "User not found");
                response.getWriter().write(gson.toJson(result));
            }

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "Error updating user: " + e.getMessage());
            response.getWriter().write(gson.toJson(result));
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String pathInfo = request.getPathInfo();

        if (pathInfo == null || pathInfo.equals("/")) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "User ID is required");
            response.getWriter().write(gson.toJson(result));
            return;
        }

        try {
            String userId = pathInfo.substring(1);
            boolean success = datastoreService.deleteUser(userId);

            if (success) {
                Map<String, Object> result = new HashMap<>();
                result.put("success", true);
                result.put("message", "User deleted successfully");
                response.getWriter().write(gson.toJson(result));
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("message", "User not found");
                response.getWriter().write(gson.toJson(result));
            }

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "Error deleting user: " + e.getMessage());
            response.getWriter().write(gson.toJson(result));
        }
    }
}