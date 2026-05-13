package models;

import java.util.HashMap;
import java.util.UUID;

// Abstract base - fields are private, subclasses must implement getRequests()
public abstract class User {

    private final String name;
    private String password;
    private final String email;
    private final String idNumber;
    private final UUID id = UUID.randomUUID();

    // returns the requests relevant to this user - each subclass decides what that means
    public abstract HashMap<UUID, Request> getRequests(HashMap<UUID, Request> requests);

    public User(String name, String password, String email, String idNumber) {
        this.name     = name;
        this.password = password;
        this.email    = email;
        this.idNumber = idNumber;
    }

    // getters
    public UUID   getId()       { return id; }
    public String getName()     { return name; }
    public String getPassword() { return password; }
    public String getEmail()    { return email; }
    public String getIdNumber() { return idNumber; }

    // setter
    public void updatePassword(String password) { this.password = password; }
}
