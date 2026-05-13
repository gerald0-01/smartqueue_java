package models;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

// Admin can manage users, view all requests, and create staff accounts
interface AdminMethods {
    void createStaffAccount(String name, String password, String email,
                            String idNumber, String department,
                            HashMap<String, User> users,
                            List<ActivityLog> logs);

    void deleteUser(String idNumber,
                    HashMap<String, User> users,
                    HashMap<UUID, Request> requests,
                    List<ActivityLog> logs);

    void resetUserPassword(String idNumber, String newPassword,
                           HashMap<String, User> users,
                           List<ActivityLog> logs);
}

// Admin - full system access: user management + all requests
public class Admin extends User implements AdminMethods {

    private final String role;

    public Admin(String name, String password, String email,
                 String idNumber, String role) {
        super(name, password, email, idNumber);
        this.role = role;
    }

    public String getRole() { return role; }

    @Override
    public void createStaffAccount(String name, String password, String email,
                                   String idNumber, String department,
                                   HashMap<String, User> users,
                                   List<ActivityLog> logs) {
        Staff staff = new Staff(name, password, email, idNumber, department);
        users.put(idNumber, staff);
        logs.add(new ActivityLog(
                getIdNumber(), getName(), "Admin",
                ActivityLog.Action.USER_CREATED,
                "Created staff account: " + name + " (" + idNumber + ") — " + department));
    }

    @Override
    public void deleteUser(String idNumber,
                           HashMap<String, User> users,
                           HashMap<UUID, Request> requests,
                           List<ActivityLog> logs) {
        User target = users.get(idNumber);
        if (target == null) return;

        String targetDesc = target.getName() + " (" + idNumber + ")";
        users.remove(idNumber);

        if (target instanceof Student) {
            UUID uid = target.getId();
            requests.entrySet().removeIf(e -> e.getValue().getStudentId().equals(uid));
        }

        logs.add(new ActivityLog(
                getIdNumber(), getName(), "Admin",
                ActivityLog.Action.USER_DELETED,
                "Deleted account: " + targetDesc));
    }

    @Override
    public void resetUserPassword(String idNumber, String newPassword,
                                  HashMap<String, User> users,
                                  List<ActivityLog> logs) {
        User target = users.get(idNumber);
        if (target == null) return;
        target.updatePassword(newPassword);
        logs.add(new ActivityLog(
                getIdNumber(), getName(), "Admin",
                ActivityLog.Action.PASSWORD_RESET,
                "Reset password for: " + target.getName() + " (" + idNumber + ")"));
    }

    @Override
    public HashMap<UUID, Request> getRequests(HashMap<UUID, Request> requests) {
        return new HashMap<>(requests);
    }
}
