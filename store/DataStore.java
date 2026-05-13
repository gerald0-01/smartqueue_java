package store;

import models.*;
import java.util.*;

// Central in-memory store shared across the whole app
public class DataStore {
    public static HashMap<String, User>    users    = new HashMap<>(); // keyed by idNumber
    public static HashMap<UUID, Request>   requests = new HashMap<>(); // keyed by request UUID
    public static List<ActivityLog>        logs     = new ArrayList<>();
}
