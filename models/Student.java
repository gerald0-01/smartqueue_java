package models;

import java.util.HashMap;
import java.util.UUID;

// Student can create requests and only sees their own
interface StudentMethods {
    Request createRequest(String documentType, String status, String reason);
}

// Student - can submit requests and view their own
public class Student extends User implements StudentMethods {
    private String course, college;
    private short  year;

    public Student(String name, String password, String email,
                   String idNumber, short year, String course, String college) {
        super(name, password, email, idNumber);
        this.course  = course;
        this.college = college;
        this.year    = year;
    }

// getters
    public short  getYear()    { return year; }
    public String getCourse()  { return course; }
    public String getCollege() { return college; }

    // setters
    public void setYear(short year)        { this.year    = year; }
    public void setCourse(String course)   { this.course  = course; }
    public void setCollege(String college) { this.college = college; }

    @Override
    public Request createRequest(String documentType, String status, String reason) {
        return new Request(getId(), documentType, status, reason);
    }

    // only returns requests belonging to this student
    @Override
    public HashMap<UUID, Request> getRequests(HashMap<UUID, Request> requests) {
        HashMap<UUID, Request> mine = new HashMap<>();
        for (Request r : requests.values()) {
            if (r.getStudentId().equals(getId())) {
                mine.put(r.getId(), r);
            }
        }
        return mine;
    }
}

