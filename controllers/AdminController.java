package controllers;

import models.*;
import store.DataStore;

// Wraps Admin model methods so views don't touch DataStore directly
public class AdminController {

    public static void createStaff(Admin admin, String name, String password,
                                   String email, String idNumber, String dept) {
        admin.createStaffAccount(name, password, email, idNumber, dept,
                                 DataStore.users, DataStore.logs);
    }

    public static boolean deleteUser(Admin admin, String idNumber) {
        if (!DataStore.users.containsKey(idNumber)) return false;
        admin.deleteUser(idNumber, DataStore.users, DataStore.requests, DataStore.logs);
        return true;
    }

    public static boolean resetPassword(Admin admin, String idNumber, String newPass) {
        if (!DataStore.users.containsKey(idNumber)) return false;
        admin.resetUserPassword(idNumber, newPass, DataStore.users, DataStore.logs);
        return true;
    }
}
