package main.users;

/**
 * @authors 22887893 YVES MIGUEL REYES 33.3%
 * @authors 23262446 SRINIKETH KARLAPUDI 33.3%
 * @authors 23468614 CHENG LI 33.3%
 */
public class User {

    private String username, password;

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    @Override
    public String toString() {
        return username + ":" + password;
    }

    public String getPassword() {
        return password;
    }

    public String getUsername() {
        return username;
    }
}
