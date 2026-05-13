package models;

import java.time.LocalDateTime;
import java.util.UUID;

// Holds all the data for a single document request
public class Request {
    private final UUID          id        = UUID.randomUUID();
    private final UUID          studentId;
    private final String documentType;
    private String status;
    private final String reason;
    private String message;
    private LocalDateTime       pickUp;
    private final LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime       completedAt; // set when status is changed to "Completed"

    public Request(UUID studentId, String documentType,
                   String status, String reason) {
        this.studentId    = studentId;
        this.documentType = documentType;
        this.status       = status;
        this.reason       = reason;
    }

    // getters
    public UUID          getId()             { return id; }
    public UUID          getStudentId()      { return studentId; }
    public String        getDocumentType()   { return documentType; }
    public String        getStatus()         { return status; }
    public String        getReason()         { return reason; }
    public String        getMessage()        { return message; }
    public LocalDateTime getPickUpDateTime() { return pickUp; }
    public LocalDateTime getCreatedAt()      { return createdAt; }
    public LocalDateTime getCompletedAt()    { return completedAt; }

    // setters - only staff should be calling these
    public void setStatus(String status) {
        this.status = status;
        // record the exact moment it was marked Completed
        if ("Completed".equals(status)) completedAt = LocalDateTime.now();
    }
    public void setMessage(String message)          { this.message = message; }
    public void setPickUpDateTime(LocalDateTime dt) { this.pickUp  = dt; }
}

