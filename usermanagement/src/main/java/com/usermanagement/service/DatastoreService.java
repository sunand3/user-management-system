package com.usermanagement.service;

import com.google.cloud.Timestamp;
import com.google.cloud.datastore.*;
import com.usermanagement.model.User;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DatastoreService {
    private static DatastoreService instance;
    private final Datastore datastore;
    private static final String KIND = "User";

    private DatastoreService() {
        this.datastore = DatastoreOptions.getDefaultInstance().getService();
    }

    public static synchronized DatastoreService getInstance() {
        if (instance == null) {
            instance = new DatastoreService();
        }
        return instance;
    }

    public String createUser(User user) {
        KeyFactory keyFactory = datastore.newKeyFactory().setKind(KIND);
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

    public User getUserById(String id) {
        Key key = datastore.newKeyFactory().setKind(KIND).newKey(Long.parseLong(id));
        Entity entity = datastore.get(key);

        if (entity == null) {
            return null;
        }

        return entityToUser(entity);
    }

    public User getUserByEmail(String email) {
        Query<Entity> query = Query.newEntityQueryBuilder()
                .setKind(KIND)
                .setFilter(StructuredQuery.PropertyFilter.eq("email", email))
                .setLimit(1)
                .build();

        QueryResults<Entity> results = datastore.run(query);

        if (results.hasNext()) {
            return entityToUser(results.next());
        }

        return null;
    }

    public List<User> getAllUsers() {
        Query<Entity> query = Query.newEntityQueryBuilder()
                .setKind(KIND)
                .build();

        QueryResults<Entity> results = datastore.run(query);
        List<User> users = new ArrayList<>();

        while (results.hasNext()) {
            users.add(entityToUser(results.next()));
        }

        return users;
    }

    public List<User> searchUsers(String searchTerm) {
        List<User> allUsers = getAllUsers();
        List<User> filteredUsers = new ArrayList<>();

        String lowerSearchTerm = searchTerm.toLowerCase();

        for (User user : allUsers) {
            if (user.getName().toLowerCase().contains(lowerSearchTerm) ||
                    user.getEmail().toLowerCase().contains(lowerSearchTerm)) {
                filteredUsers.add(user);
            }
        }

        return filteredUsers;
    }

    public boolean updateUser(String id, User updatedUser) {
        Key key = datastore.newKeyFactory().setKind(KIND).newKey(Long.parseLong(id));
        Entity existingEntity = datastore.get(key);

        if (existingEntity == null) {
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
    }

    public boolean deleteUser(String id) {
        Key key = datastore.newKeyFactory().setKind(KIND).newKey(Long.parseLong(id));
        Entity entity = datastore.get(key);

        if (entity == null) {
            return false;
        }

        datastore.delete(key);
        return true;
    }

    public int getUserCount() {
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