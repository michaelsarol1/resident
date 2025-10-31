package services;

import config.config;
import java.util.*;

public class PurokService {
    private final config db;

    public PurokService(config db) {
        this.db = db;
    }

    // ===================== ID VALIDATION HELPERS ===========================

    /**
     * Checks if a Purok ID exists in the database.
     */
    public boolean purokExists(int purokId) {
        String query = "SELECT COUNT(p_id) AS Count FROM puroks WHERE p_id = ?";
        List<Map<String, Object>> result = db.fetchRecords(query, purokId);
        if (!result.isEmpty()) {
            Object countObj = result.get(0).get("Count");
            if (countObj instanceof Long) return (Long) countObj > 0;
            if (countObj instanceof Integer) return (Integer) countObj > 0;
        }
        return false;
    }

    // ===================== VIEW FUNCTIONS ===========================

    /**
     * Displays all Puroks.
     */
    public void viewPuroks() {
        String query = "SELECT p_id, p_name FROM puroks";
        String[] headers = {"ID", "Purok Name"};
        String[] cols = {"p_id", "p_name"};
        // Note: Assuming db.viewRecords() exists for clean printing
        db.viewRecords(query, headers, cols);
    }
    
    // ===================== PUROK CRUD ===========================

    public void managePuroks(Scanner sc) {
        int choice = 0;
        do {
            System.out.println("\n--- MANAGE PUROKS ---");
            System.out.println("1. Add Purok ");
            System.out.println("2. View All Puroks ");
            System.out.println("3. Update Purok ");
            System.out.println("4. Delete Purok ");
            System.out.println("0. Back");
            System.out.print("Choice: ");
            if (!sc.hasNextInt()) {
                System.out.println("🛑 Error: Invalid input. Please enter a number.");
                sc.nextLine();
                continue;
            }
            choice = sc.nextInt();
            sc.nextLine();

            switch (choice) {
                case 1:
                    System.out.print("Enter purok name: ");
                    String name = sc.nextLine();
                    db.addRecord("INSERT INTO puroks(p_name) VALUES (?)", name);
                    System.out.println("Purok added successfully. 🎉");
                    break;
                case 2: viewPuroks(); break;
                case 3: updatePurok(sc); break;
                case 4: deletePurok(sc); break;
                case 0: return;
                default: System.out.println("Invalid choice!");
            }
        } while (choice != 0);
    }

    private void updatePurok(Scanner sc) {
        System.out.println("\n--- Current Puroks ---");
        viewPuroks();
        System.out.print("Enter Purok ID to update: ");
        int pid;
        if (sc.hasNextInt()) {
            pid = sc.nextInt();
        } else {
            System.out.println("🛑 Error: Invalid Purok ID input. Returning to menu.");
            sc.nextLine();
            return;
        }
        sc.nextLine();

        if (!purokExists(pid)) {
            System.out.println("🛑 Error: Purok ID " + pid + " does not exist. Returning to menu.");
            return;
        }

        System.out.print("New name: ");
        String newName = sc.nextLine();

        db.updateRecord("UPDATE puroks SET p_name = ? WHERE p_id = ?", newName, pid);
        System.out.println("Purok updated successfully. ✅");
    }

    private void deletePurok(Scanner sc) {
    System.out.println("\n--- Current Puroks ---");
    viewPuroks();
    System.out.print("Enter Purok ID to delete: ");
    int delPId;
    if (sc.hasNextInt()) {
        delPId = sc.nextInt();
    } else {
        System.out.println("🛑 Error: Invalid Purok ID input. Returning to menu.");
        sc.nextLine();
        return;
    }
    sc.nextLine();

    if (!purokExists(delPId)) {
        System.out.println("🛑 Error: Purok ID " + delPId + " does not exist. Returning to menu.");
        return;
    }

    // ⭐ START OF FIX: Data Integrity Check ⭐
    String residentCountQuery = "SELECT COUNT(*) AS count FROM residents WHERE r_purok_id = ?";
    List<Map<String, Object>> residentResult = db.fetchRecords(residentCountQuery, delPId);
    long residentCount = 0;
    
    if (!residentResult.isEmpty()) {
        Object countObj = residentResult.get(0).get("count");
        if (countObj instanceof Long) {
            residentCount = (Long) countObj;
        } else if (countObj instanceof Integer) {
            residentCount = (Integer) countObj;
        }
    }

    if (residentCount > 0) {
        System.out.println("🛑 Error: Cannot delete Purok ID " + delPId + ".");
        System.out.println("There are " + residentCount + " resident(s) still assigned to this Purok. Delete residents first.");
        return; // Exit the method without deleting
    }
    // ⭐ END OF FIX ⭐

    // If the count is 0, proceed with deletion
    db.deleteRecord("DELETE FROM puroks WHERE p_id = ?", delPId);
    System.out.println("Purok deleted successfully. 🗑️");
}
}
