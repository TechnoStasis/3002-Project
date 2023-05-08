package main;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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
        try {
            BufferedReader in = new BufferedReader(new FileReader(userPath));
            String str;
            while ((str = in.readLine()) != null) {
                String username = str.split(":")[0];
                String password = str.split(":")[1];

                User user = new User(username, password);
                userMap.put(user.toString(), user);
            }
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void saveUsers() {
        try {
            FileWriter file = new FileWriter(userPath);
            for (Map.Entry<String, User> entry : userMap.entrySet()) {
                file.write(entry.getKey() + '\n');
                System.out.println(entry.getKey());
            }
            file.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
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

    public void registerUser(User user) {
        addUser(user);
        saveUsers();
    }

    public boolean validate(String user, String password) {
        if (userMap.get(user + ":" + password) != null)
            return userMap.get(user + ":" + password).toString().equals(user + ":" + password);
        return false;
    }
}
