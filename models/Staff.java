package models;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.UUID;

// Staff can edit any request in the system
interface StaffMethods {
    void editRequest(Request request, String newStatus, String message, LocalDateTime pickUp);
}

// Staff - can view and edit all requests
public class Staff extends User implements StaffMethods {
    private final String department;

    public Staff(String name, String password, String email,
                 String idNumber, String department) {
        super(name, password, email, idNumber);
        this.department = department;
    }

    // getter
    public String getDepartment() { return department; }

    @Override
    public void editRequest(Request request, String newStatus,
                            String message, LocalDateTime pickUp) {
        request.setStatus(newStatus);
        request.setMessage(message);
        request.setPickUpDateTime(pickUp);
    }

    // staff sees every request, not just their own
    @Override
    public HashMap<UUID, Request> getRequests(HashMap<UUID, Request> requests) {
        return new HashMap<>(requests);
    }
}

