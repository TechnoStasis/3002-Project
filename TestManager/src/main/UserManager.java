package main;

import java.util.HashMap;

import users.User;

public class UserManager {

    public static final UserManager INSTANCE = new UserManager(
            "/Users/yvesreyes/Documents/3002-Project/TestManager/src/users/users.txt");

    private final String userPath;

    private HashMap<String, User> userMap = new HashMap<String, User>();

    private UserManager(String file) {
        userPath = file;

        userMap.put("admin|admin", new User("admin", "admin"));    
    }

    private void readUsers() {

    }

    public User getUser(String username, String password) {
        if (userMap.get(username + "|" + password) != null)
            return userMap.get(username + "|" + password);
        else
            return null;
    }
}
