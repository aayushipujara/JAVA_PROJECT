package com.signbridge.service;

import com.signbridge.db.dao.UserDAO;
import com.signbridge.model.User;

public class AuthService {
    private static AuthService instance;
    private final UserDAO userDAO;
    private User currentUser;

    private AuthService() {
        this.userDAO = new UserDAO();
    }

    public static synchronized AuthService getInstance() {
        if (instance == null) {
            instance = new AuthService();
        }
        return instance;
    }

    public boolean login(String username, String password) {
        User user = userDAO.authenticate(username, password);
        if (user != null) {
            this.currentUser = user;
            return true;
        }
        return false;
    }

    public boolean register(String username, String password, String fullName) {
        if (userDAO.isUsernameTaken(username)) {
            return false;
        }
        boolean ok = userDAO.register(username, password, fullName);
        if (ok) {
            this.currentUser = userDAO.authenticate(username, password);
        }
        return ok;
    }

    public void logout() {
        this.currentUser = null;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void refreshUser() {
        if (currentUser != null) {
            User updated = userDAO.getUserById(currentUser.getId());
            if (updated != null) {
                this.currentUser = updated;
            }
        }
    }

    public void awardXp(int xp) {
        if (currentUser != null) {
            userDAO.addXp(currentUser.getId(), xp);
            refreshUser();
        }
    }
}
