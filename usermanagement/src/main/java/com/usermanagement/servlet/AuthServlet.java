package com.usermanagement.servlet;

import com.google.gson.Gson;
import com.usermanagement.model.User;
import com.usermanagement.service.DatastoreService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/api/auth/*")
public class AuthServlet extends HttpServlet {
    private DatastoreService datastoreService;
    private Gson gson;

    @Override
    public void init() throws ServletException {
        datastoreService = DatastoreService.getInstance();
        gson = new Gson();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String pathInfo = request.getPathInfo();

        if (pathInfo == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        try {
            if (pathInfo.equals("/login")) {
                handleLogin(request, response);
            } else if (pathInfo.equals("/logout")) {
                handleLogout(request, response);
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

    private void handleLogin(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        BufferedReader reader = request.getReader();
        Map<String, String> credentials = gson.fromJson(reader, Map.class);

        String email = credentials.get("email");
        String password = credentials.get("password");

        if (email == null || password == null || email.isEmpty() || password.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "Email and password are required");
            response.getWriter().write(gson.toJson(result));
            return;
        }

        User user = datastoreService.getUserByEmail(email);

        if (user != null && user.getPassword().equals(password)) {
            // Create session
            HttpSession session = request.getSession(true);
            session.setAttribute("userId", user.getId());
            session.setAttribute("userEmail", user.getEmail());
            session.setMaxInactiveInterval(30 * 60); // 30 minutes

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "Login successful");
            result.put("user", Map.of(
                    "id", user.getId(),
                    "name", user.getName(),
                    "email", user.getEmail()
            ));

            response.getWriter().write(gson.toJson(result));
        } else {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "Invalid email or password");
            response.getWriter().write(gson.toJson(result));
        }
    }

    private void handleLogout(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "Logout successful");

        response.getWriter().write(gson.toJson(result));
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String pathInfo = request.getPathInfo();

        if (pathInfo != null && pathInfo.equals("/check")) {
            HttpSession session = request.getSession(false);

            Map<String, Object> result = new HashMap<>();

            if (session != null && session.getAttribute("userId") != null) {
                String userId = (String) session.getAttribute("userId");
                User user = datastoreService.getUserById(userId);

                if (user != null) {
                    result.put("authenticated", true);
                    result.put("user", Map.of(
                            "id", user.getId(),
                            "name", user.getName(),
                            "email", user.getEmail()
                    ));
                } else {
                    result.put("authenticated", false);
                }
            } else {
                result.put("authenticated", false);
            }

            response.getWriter().write(gson.toJson(result));
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }
}