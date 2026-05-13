package models;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

// Immutable record of a single system event
public class ActivityLog {

    public enum Action {
        LOGIN, LOGOUT, REGISTER,
        REQUEST_SUBMITTED, REQUEST_UPDATED,
        USER_CREATED, USER_DELETED, PASSWORD_RESET
    }

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final LocalDateTime timestamp = LocalDateTime.now();
    private final String        actorId;   // ID number of the user who acted
    private final String        actorName;
    private final String        actorRole; // "Student" | "Staff" | "Admin"
    private final Action        action;
    private final String        detail;    // free-text description

    public ActivityLog(String actorId, String actorName,
                       String actorRole, Action action, String detail) {
        this.actorId   = actorId;
        this.actorName = actorName;
        this.actorRole = actorRole;
        this.action    = action;
        this.detail    = detail;
    }

    // getters
    public LocalDateTime getTimestamp() { return timestamp; }
    public String        getActorId()   { return actorId; }
    public String        getActorName() { return actorName; }
    public String        getActorRole() { return actorRole; }
    public Action        getAction()    { return action; }
    public String        getDetail()    { return detail; }

    public String getFormattedTimestamp() { return timestamp.format(FMT); }

    @Override
    public String toString() {
        return String.format("[%s] %s (%s / %s): %s — %s",
                getFormattedTimestamp(), actorName, actorId, actorRole,
                action.name(), detail);
    }
}
