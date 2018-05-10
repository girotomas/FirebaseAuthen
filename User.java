package giro.tomas.com.googlemaptracker.Firebase;

public class User {
    private String email;
    private String name;
    public User() {
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public User(String email, String name) {
        this.email = email;
        this.name = name;
    }
}
