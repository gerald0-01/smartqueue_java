package controllers;

import models.*;
import store.DataStore;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.UUID;

// Handles creating and editing document requests
public class RequestController {

    // student submits a new request
    public static void submit(Student student, String docType, String reason) {
        Request r = student.createRequest(docType, "Pending", reason);
        DataStore.requests.put(r.getId(), r);
        DataStore.logs.add(new ActivityLog(
            student.getIdNumber(), student.getName(), "Student",
            ActivityLog.Action.REQUEST_SUBMITTED,
            "Submitted request: " + docType));
    }

    // staff updates an existing request
    public static void update(Staff staff, UUID requestId,
                              String status, String message, LocalDateTime pickUp) {
        Request r = DataStore.requests.get(requestId);
        if (r == null) return;
        staff.editRequest(r, status, message, pickUp);
        DataStore.logs.add(new ActivityLog(
            staff.getIdNumber(), staff.getName(), "Staff",
            ActivityLog.Action.REQUEST_UPDATED,
            "Updated request " + requestId + " → " + status));
    }

    // returns requests visible to the given user
    public static HashMap<UUID, Request> getFor(User user) {
        return user.getRequests(DataStore.requests);
    }
}
