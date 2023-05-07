package users;

public class User {

    private String username, password;

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    @Override
    public String toString()
    {
        return username + ":" + password;
    }
}
