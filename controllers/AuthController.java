package controllers;

import models.*;
import store.DataStore;

// Handles login, logout, and student self-registration
public class AuthController {

    // returns the matched User or null if credentials are wrong
    public static User login(String idNumber, String password) {
        User u = DataStore.users.get(idNumber);
        if (u != null && u.getPassword().equals(password)) {
            DataStore.logs.add(new ActivityLog(
                idNumber, u.getName(), u.getClass().getSimpleName(),
                ActivityLog.Action.LOGIN, "Logged in"));
            return u;
        }
        return null;
    }

    public static void logout(User u) {
        DataStore.logs.add(new ActivityLog(
            u.getIdNumber(), u.getName(), u.getClass().getSimpleName(),
            ActivityLog.Action.LOGOUT, "Logged out"));
    }

    // students register themselves
    public static boolean register(String name, String password, String email,
                                   String idNumber, short year,
                                   String course, String college) {
        if (DataStore.users.containsKey(idNumber)) return false; // duplicate
        Student s = new Student(name, password, email, idNumber, year, course, college);
        DataStore.users.put(idNumber, s);
        DataStore.logs.add(new ActivityLog(
            idNumber, name, "Student",
            ActivityLog.Action.REGISTER, "Self-registered"));
        return true;
    }
}
