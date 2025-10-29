package services;

import config.config;
import java.util.*;

public class ResidentService {
    private final config db;

    public ResidentService(config db) {
        this.db = db;
    }

    // ===================== ID VALIDATION HELPERS ===========================

    private boolean purokExists(int purokId) {
        String query = "SELECT COUNT(p_id) AS Count FROM puroks WHERE p_id = ?";
        List<Map<String, Object>> result = db.fetchRecords(query, purokId);
        if (!result.isEmpty()) {
            Object countObj = result.get(0).get("Count"); 
            if (countObj instanceof Long) return (Long) countObj > 0;
            if (countObj instanceof Integer) return (Integer) countObj > 0;
        }
        return false;
    }

    private boolean residentExists(int residentId) {
        String query = "SELECT COUNT(r_id) AS Count FROM residents WHERE r_id = ?";
        List<Map<String, Object>> result = db.fetchRecords(query, residentId);
        if (!result.isEmpty()) {
            Object countObj = result.get(0).get("Count"); 
            if (countObj instanceof Long) return (Long) countObj > 0;
            if (countObj instanceof Integer) return (Integer) countObj > 0;
        }
        return false;
    }

    // ===================== VIEW FUNCTIONS ===========================
    
    public void viewResidents() {
        String query = "SELECT r.r_id, r.r_name, r.r_age, r.r_gender, p.p_name AS purok_name FROM residents r JOIN puroks p ON r.r_purok_id = p.p_id";
        String[] headers = {"ID", "Name", "Age", "Gender", "Purok Name"};
        String[] cols = {"r_id", "r_name", "r_age", "r_gender", "purok_name"};
        db.viewRecords(query, headers, cols);
    }

    public void viewPuroks() {
        String query = "SELECT p_id, p_name FROM puroks";
        String[] headers = {"ID", "Purok Name"};
        String[] cols = {"p_id", "p_name"};
        db.viewRecords(query, headers, cols);
    }
    
    public void viewResidentsByPurok(Scanner sc) {
        viewPuroks();
        
        System.out.print("\nEnter Purok ID to view residents: ");
        int purokId;
        if (sc.hasNextInt()) {
            purokId = sc.nextInt();
        } else {
            System.out.println("🛑 Error: Invalid input. Please enter a number.");
            sc.nextLine(); 
            return;
        }
        sc.nextLine();

        if (!purokExists(purokId)) {
            System.out.println("🛑 Error: Purok ID " + purokId + " does not exist.");
            return;
        }

        String query = "SELECT r_id, r_name, r_age, r_gender FROM residents WHERE r_purok_id = ?";
        String[] headers = {"ID", "Name", "Age", "Gender"};
        String[] cols = {"r_id", "r_name", "r_age", "r_gender"};
        
        List<Map<String, Object>> result = db.fetchRecords(query, purokId);
        
        if (result.isEmpty()) {
            System.out.println("\nNo residents found for Purok ID: " + purokId);
            return;
        }

        System.out.println("\n--- Residents in Purok ID " + purokId + " ---");
        
        System.out.printf("%-5s %-20s %-5s %-10s\n", headers[0], headers[1], headers[2], headers[3]);
        System.out.println("--------------------------------------------");
        
        for (Map<String, Object> record : result) {
            System.out.printf("%-5s %-20s %-5s %-10s\n", 
                record.get(cols[0]), 
                record.get(cols[1]), 
                record.get(cols[2]), 
                record.get(cols[3]));
        }
        System.out.println("--------------------------------------------");
    }

    public void viewMyPurokInfo(String email) {
        // NOTE: This links resident to user by name. Assumes r_name == u_name.
        String p_query = "SELECT p.p_id, p.p_name " +
                         "FROM puroks p JOIN residents r ON p.p_id = r.r_purok_id " +
                         "JOIN users u ON r.r_name = u.u_name WHERE u.u_email = ?"; 
        List<Map<String, Object>> p_result = db.fetchRecords(p_query, email); 
        
        if (!p_result.isEmpty()) {
            Map<String, Object> purokInfo = p_result.get(0); 
            System.out.println("\n--- My Purok Information ---"); 
            System.out.println("Purok ID  : " + purokInfo.get("p_id")); 
            System.out.println("Purok Name: " + purokInfo.get("p_name")); 
        } else { System.out.println("No purok information found (Is your user name linked to a resident?)."); }
    }

    // ===================== DATA MANAGEMENT MENU ===========================

    public void dataManagementMenu(Scanner sc) {
        int subChoice = 0;
        do {
            System.out.println("\n--- DATA MANAGEMENT MENU ---");
            System.out.println("1. Manage Residents");
            System.out.println("2. Manage Puroks"); 
            System.out.println("0. Back to Admin Menu");
            System.out.print("Choice: ");
            if (!sc.hasNextInt()) {
                System.out.println("🛑 Error: Invalid input. Please enter a number.");
                sc.nextLine(); 
                continue;
            }
            subChoice = sc.nextInt();
            sc.nextLine(); 

            switch (subChoice) {
                case 1: manageResidents(sc); break;
                case 2: managePuroks(sc); break; 
                case 0: return;
                default: System.out.println("Invalid choice!");
            }
        } while (subChoice != 0);
    }

    // ===================== RESIDENT CRUD ===========================
    
    private void manageResidents(Scanner sc) {
        int choice = 0;
        do {
            System.out.println("\n--- MANAGE RESIDENTS ---");
            System.out.println("1. Add Resident ");
            System.out.println("2. View All Residents");
            System.out.println("3. Update Resident");
            System.out.println("4. Delete Resident");
            System.out.println("5. View Residents by Purok");
            System.out.println("0. Back");
            System.out.print("Choice: ");
            if (!sc.hasNextInt()) {
                 System.out.println("🛑 Error: Invalid input. Please enter a number.");
                 sc.nextLine(); 
                 continue;
            }
            choice = sc.nextInt();
            sc.nextLine(); // Consume newline after choice

            switch (choice) {
                case 1:
                    System.out.print("Enter resident name: ");
                    String rname = sc.nextLine();
                    System.out.print("Enter age: ");
                    int age;
                    if (sc.hasNextInt()) {
                        age = sc.nextInt();
                    } else {
                        System.out.println("🛑 Error: Invalid age input. Returning to menu.");
                        sc.nextLine(); 
                        break;
                    }
                    sc.nextLine(); // Consume newline after age
                    System.out.print("Enter gender: ");
                    String gen = sc.nextLine();
                    
                    System.out.println("\n--- Available Puroks ---");
                    viewPuroks();
                    System.out.print("Enter Purok ID (must be an existing ID above): ");
                    int p_id;
                    if (sc.hasNextInt()) {
                        p_id = sc.nextInt();
                    } else {
                        System.out.println("🛑 Error: Invalid Purok ID input. Returning to menu.");
                        sc.nextLine(); 
                        break;
                    }
                    sc.nextLine(); 

                    if (!purokExists(p_id)) {
                        System.out.println("🛑 Error: Cannot add resident. Purok ID " + p_id + " does not exist. Register the Purok first.");
                        break;
                    }

                
                    db.addRecord("INSERT INTO residents(r_name, r_age, r_gender, r_purok_id) VALUES (?, ?, ?, ?)", rname, age, gen, p_id);
                    System.out.println("Resident added successfully. 🎉");
                    break;
                case 2: viewResidents(); break;
                case 3:
                    System.out.println("\n--- Current Residents ---");
                    viewResidents(); 
                    System.out.print("Enter Resident ID to update: ");
                    int rid;
                    if (sc.hasNextInt()) {
                        rid = sc.nextInt();
                    } else {
                        System.out.println("🛑 Error: Invalid Resident ID input. Returning to menu.");
                        sc.nextLine(); 
                        break;
                    }
                    sc.nextLine(); // Consume newline
                    
                    if (!residentExists(rid)) {
                        System.out.println("🛑 Error: Resident ID " + rid + " does not exist. Returning to menu.");
                        break; 
                    }
                    
                    System.out.print("New name: ");
                    String newName = sc.nextLine();
                    System.out.print("New age: ");
                    int newAge;
                    if (sc.hasNextInt()) {
                        newAge = sc.nextInt();
                    } else {
                        System.out.println("🛑 Error: Invalid age input. Returning to menu.");
                        sc.nextLine(); 
                        break;
                    }
                    sc.nextLine(); // Consume newline
                    System.out.print("New gender: ");
                    String newGender = sc.nextLine();
                    
                    System.out.println("\n--- Available Puroks ---");
                    viewPuroks();
                    System.out.print("New Purok ID (must be an existing ID): ");
                    int newPId;
                    if (sc.hasNextInt()) {
                        newPId = sc.nextInt();
                    } else {
                        System.out.println("🛑 Error: Invalid Purok ID input. Returning to menu.");
                        sc.nextLine(); 
                        break;
                    }
                    sc.nextLine(); 
                    
                    if (!purokExists(newPId)) {
                        System.out.println("🛑 Error: Cannot update resident. Purok ID " + newPId + " does not exist.");
                        break;
                    }

                    db.updateRecord("UPDATE residents SET r_name = ?, r_age = ?, r_gender = ?, r_purok_id = ? WHERE r_id = ?", newName, newAge, newGender, newPId, rid);
                    System.out.println("Resident updated successfully. ✅");
                    break;
                case 4:
                    System.out.println("\n--- Current Residents ---");
                    viewResidents(); 
                    System.out.print("Enter Resident ID to delete: ");
                    int delId;
                    if (sc.hasNextInt()) {
                        delId = sc.nextInt();
                    } else {
                        System.out.println("🛑 Error: Invalid Resident ID input. Returning to menu.");
                        sc.nextLine(); 
                        break;
                    }
                    sc.nextLine(); 
                    
                    if (!residentExists(delId)) {
                        System.out.println("🛑 Error: Resident ID " + delId + " does not exist. Returning to menu.");
                        break; 
                    }
                    
                    db.deleteRecord("DELETE FROM residents WHERE r_id = ?", delId);
                    System.out.println("Resident deleted successfully. 🗑️");
                    break;
                case 5: viewResidentsByPurok(sc); break;
                case 0: return;
                default: System.out.println("Invalid choice!");
            }
        } while (choice != 0);
    }
    
    // ===================== PUROK CRUD =========================== 
    
    private void managePuroks(Scanner sc) {
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
                case 3:
                    System.out.println("\n--- Current Puroks ---");
                    viewPuroks(); 
                    System.out.print("Enter Purok ID to update: ");
                    int pid;
                    if (sc.hasNextInt()) {
                        pid = sc.nextInt();
                    } else {
                        System.out.println("🛑 Error: Invalid Purok ID input. Returning to menu.");
                        sc.nextLine(); 
                        break;
                    }
                    sc.nextLine(); 
                    
                    if (!purokExists(pid)) {
                        System.out.println("🛑 Error: Purok ID " + pid + " does not exist. Returning to menu.");
                        break;
                    }

                    System.out.print("New name: "); 
                    String newName = sc.nextLine(); 
                    
                    db.updateRecord("UPDATE puroks SET p_name = ? WHERE p_id = ?", newName, pid);
                    System.out.println("Purok updated successfully. ✅"); 
                    break;
                case 4:
                    System.out.println("\n--- Current Puroks ---");
                    viewPuroks(); 
                    System.out.print("Enter Purok ID to delete: ");
                    int delPId;
                    if (sc.hasNextInt()) {
                        delPId = sc.nextInt();
                    } else {
                        System.out.println("🛑 Error: Invalid Purok ID input. Returning to menu.");
                        sc.nextLine(); 
                        break;
                    }
                    sc.nextLine(); 
                    
                    if (!purokExists(delPId)) {
                        System.out.println("🛑 Error: Purok ID " + delPId + " does not exist. Returning to menu.");
                        break;
                    }
                    
                    db.deleteRecord("DELETE FROM puroks WHERE p_id = ?", delPId);
                    System.out.println("Purok deleted successfully. 🗑️"); 
                    break;
                case 0: return;
                default: System.out.println("Invalid choice!");
            }
        } while (choice != 0);
    }
}