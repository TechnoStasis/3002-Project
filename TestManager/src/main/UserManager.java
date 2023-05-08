package main;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;

import users.User;

public class UserManager {

    public static final UserManager INSTANCE = new UserManager(
            "/Users/yvesreyes/Documents/3002-Project/TestManager/src/users/users.txt");

    private final String userPath;

    private HashMap<String, User> userMap = new HashMap<String, User>();

    private UserManager(String file) {
        userPath = file;
        readUsers();
    }

    private void readUsers() {
        BufferedReader in;

        try {
            in = new BufferedReader(new FileReader(userPath));
            String str;
            while ((str = in.readLine()) != null) {
                String username = str.split(":")[0];
                String password = str.split(":")[1];

                User user = new User(username, password);
                userMap.put(user.toString(), user);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void saveUsers() {

    }

    public void addUser(User user) {
        userMap.put(user.toString(), user);
    }

    public User getUser(String username, String password) {
        if (userMap.get(username + ":" + password) != null)
            return userMap.get(username + ":" + password);
        else
            return null;
    }

    public boolean validate(String user, String password) {
        if (userMap.get(user + ":" + password) != null)
            return userMap.get(user + ":" + password).toString().equals(user + ":" + password);
        return false;
    }
}
