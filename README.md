# SmartQueue v2

A Java Swing desktop application for managing student document requests in a university setting. Students submit requests, staff process them, and admins manage accounts — all in a maroon and gold themed interface.

---

## Features

**Students**
- Self-register with course, college, and year level
- Submit document requests (Transcript, Enrollment Certificate, etc.)
- View their own request history and status updates

**Staff**
- View all submitted requests across the system
- Update request status, add a message, and set a pick-up date/time

**Admin**
- Create staff accounts
- Delete any user account
- Reset any user's password
- View the full activity log

---

## Screenshots

> Login screen, student dashboard, staff dashboard, and admin dashboard all use a consistent **maroon `#800000`** and **gold `#FFD700`** color theme.

---

## Project Structure

```
smartqueuev2/
├── Main.java                        # Entry point, seeds default admin
├── models/
│   ├── User.java                    # Abstract base class for all users
│   ├── Admin.java                   # Admin role + AdminMethods interface
│   ├── Staff.java                   # Staff role + StaffMethods interface
│   ├── Student.java                 # Student role + StudentMethods interface
│   ├── Request.java                 # Document request data object
│   └── ActivityLog.java             # Immutable audit log entry
├── store/
│   └── DataStore.java               # In-memory shared state (users, requests, logs)
├── controllers/
│   ├── AuthController.java          # Login, logout, student registration
│   ├── RequestController.java       # Submit and update document requests
│   └── AdminController.java        # User management operations
└── view/
    ├── LoginFrame.java              # Login screen
    ├── RegisterDialog.java          # Student self-registration dialog
    ├── StudentFrame.java            # Student dashboard
    ├── StaffFrame.java              # Staff dashboard
    └── AdminFrame.java              # Admin dashboard (users + activity log)
```

---

## Getting Started

### Requirements

- Java 11 or higher
- No external dependencies — pure Java SE + Swing

### Compile

```bash
javac -d out -sourcepath . Main.java controllers/*.java models/*.java store/*.java view/*.java
```

### Run

```bash
java -cp out Main
```

### Default Admin Account

| Field    | Value      |
|----------|------------|
| ID       | `ADMIN001` |
| Password | `admin123` |

Log in with these credentials on first launch. From the admin dashboard you can create staff accounts and manage users.

---

## How It Works

### Architecture

The app follows a simple three-layer pattern:

```
View  →  Controller  →  Model / DataStore
```

- **Views** only call controller methods — they never touch `DataStore` directly.
- **Controllers** handle business logic, read/write `DataStore`, and delegate mutations to model methods.
- **Models** contain the core logic (e.g. `Student.getRequests()` filters by owner, `Admin.deleteUser()` cleans up orphaned requests).

### Role-Based Access

Each user type overrides `getRequests()` differently:

| Role    | Sees                          |
|---------|-------------------------------|
| Student | Only their own requests       |
| Staff   | All requests in the system    |
| Admin   | All requests in the system    |

### Activity Logging

Every significant action — login, logout, register, request submitted/updated, user created/deleted, password reset — writes an `ActivityLog` entry to `DataStore.logs`. Admins can view the full log in real time from their dashboard.

---

## User Flows

**Student**
1. Click **Register** on the login screen and fill in your details.
2. Log in with your ID number and password.
3. Select a document type, enter a reason, and click **Submit Request**.
4. Check back to see status updates from staff.

**Staff**
1. Log in with credentials provided by an admin.
2. Select a request row in the table.
3. Choose a status, optionally add a message and pick-up datetime, then click **Update Selected**.

**Admin**
1. Log in with `ADMIN001 / admin123` (or your own admin credentials).
2. Use the **Users** tab to create staff accounts, delete users, or reset passwords.
3. Use the **Activity Log** tab to audit all system events.

---

## Data Storage

All data is **in-memory only** — nothing is persisted to disk or a database. Restarting the application resets all users (except the seeded admin), requests, and logs.

---

## License

MIT
