package main;

public class UserManager {

    public static final UserManager INSTANCE = new UserManager("/Users/yvesreyes/Documents/3002-Project/TestManager/src/users/users.txt");

    private UserManager(String file) {

    }

}
