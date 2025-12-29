package com.usermanagement.servlet;

import com.google.gson.Gson;
import com.usermanagement.model.User;
import com.usermanagement.service.DatastoreService;
import com.usermanagement.util.ExcelReader;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/api/upload")
public class UploadServlet extends HttpServlet {
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

        Map<String, Object> result = new HashMap<>();

        try {
            // Check if request is multipart
            if (!ServletFileUpload.isMultipartContent(request)) {
                result.put("success", false);
                result.put("message", "Request must be multipart/form-data");
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write(gson.toJson(result));
                return;
            }

            // Configure file upload
            DiskFileItemFactory factory = new DiskFileItemFactory();
            ServletFileUpload upload = new ServletFileUpload(factory);
            upload.setFileSizeMax(10 * 1024 * 1024); // 10MB max

            // Parse request
            List<FileItem> items = upload.parseRequest(request);

            for (FileItem item : items) {
                if (!item.isFormField() && item.getName().endsWith(".xlsx")) {
                    try (InputStream inputStream = item.getInputStream()) {
                        // Read users from Excel
                        List<User> users = ExcelReader.readUsersFromExcel(inputStream);

                        // Store users in Datastore
                        int successCount = 0;
                        int failCount = 0;

                        for (User user : users) {
                            try {
                                // Check if user already exists
                                User existingUser = datastoreService.getUserByEmail(user.getEmail());
                                if (existingUser == null) {
                                    datastoreService.createUser(user);
                                    successCount++;
                                } else {
                                    failCount++;
                                }
                            } catch (Exception e) {
                                failCount++;
                                System.err.println("Error creating user: " + e.getMessage());
                            }
                        }

                        result.put("success", true);
                        result.put("message", "File uploaded successfully");
                        result.put("totalRecords", users.size());
                        result.put("successCount", successCount);
                        result.put("failCount", failCount);
                        response.setStatus(HttpServletResponse.SC_OK);
                    }
                    break;
                }
            }

            if (!result.containsKey("success")) {
                result.put("success", false);
                result.put("message", "No valid Excel file found");
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            }

        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "Error processing file: " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            e.printStackTrace();
        }

        response.getWriter().write(gson.toJson(result));
    }
}