package main;

import config.config;
import java.util.*;

public class main {


    public static void viewResidents() {

        String query = "SELECT r.r_id, r.r_name, r.r_age, r.r_gender, p.p_name AS purok_name FROM residents r JOIN puroks p ON r.r_purok_id = p.p_id";
        String[] headers = {"ID", "Name", "Age", "Gender", "Purok Name"};
        String[] cols = {"r_id", "r_name", "r_age", "r_gender", "purok_name"};
        new config().viewRecords(query, headers, cols);
    }


    public static void viewPuroks() {

        String query = "SELECT p_id, p_name FROM puroks";

        String[] headers = {"ID", "Purok Name"};
        String[] cols = {"p_id", "p_name"};
        new config().viewRecords(query, headers, cols);
    }


    private static boolean purokExists(config db, int purokId) {
        String query = "SELECT COUNT(p_id) AS Count FROM puroks WHERE p_id = ?";
        List<Map<String, Object>> result = db.fetchRecords(query, purokId);
        
        if (!result.isEmpty()) {
            Map<String, Object> row = result.get(0);

            Object countObj = row.get("Count"); 
            if (countObj instanceof Long) {
                 return (Long) countObj > 0;
            } else if (countObj instanceof Integer) {
                 return (Integer) countObj > 0;
            }
        }
        return false;
    }



    public static void viewResidentsByPurok(config db, Scanner sc) {
        viewPuroks();
        
        System.out.print("\nEnter Purok ID to view residents: ");
        int purokId = sc.nextInt();
        sc.nextLine();

        if (!purokExists(db, purokId)) {
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



    public static void main(String[] args) {
        config db = new config();
        db.connectDB();
        Scanner sc = new Scanner(System.in);
        char cont;

        do {
            System.out.println("\n===== PUROK RESIDENCE INFORMATION SYSTEM =====");
            System.out.println("1. Register User (for System Access)");
            System.out.println("2. Login");
            System.out.println("3. View All Residents");
            System.out.println("4. View All Puroks");
            System.out.println("0. Exit");
            System.out.print("Enter choice: ");
            int choice = sc.nextInt();

            switch (choice) {
                case 1:
                    registerUser(db, sc);
                    break;
                
                case 2: 
                    loginUser(db, sc);
                    break;
                    
                case 3: viewResidents(); break;
                case 4: viewPuroks(); break;
                case 0: System.exit(0); break;
                default: System.out.println("Invalid choice!");
            }

            System.out.print("\nDo you want to continue? (Y/N): ");
            cont = sc.next().charAt(0);

        } while (cont == 'Y' || cont == 'y');

        System.out.println("Program Ended. Goodbye! 👋");
    }

    // ===================== USER FUNCTIONS (Registration/Login) ===========================

    public static void registerUser(config db, Scanner sc) {
        System.out.print("Enter name: ");
        sc.nextLine();
        String name = sc.nextLine();
        System.out.print("Enter email: ");
        String email = sc.nextLine();
        System.out.print("Enter password: ");
        String pass = sc.nextLine();
        System.out.print("Enter type (Admin/Staff/Resident): ");
        String type = sc.nextLine();

        String regSql = "INSERT INTO users(u_name, u_email, u_type, u_status, u_pass) VALUES (?, ?, ?, ?, ?)";
        db.addRecord(regSql, name, email, type, "Active", pass); 
        System.out.println("User registered successfully! 🎉");
    }

    public static void loginUser(config db, Scanner sc) {
        System.out.print("Enter email: ");
        sc.nextLine(); 
        String em = sc.nextLine();
        System.out.print("Enter password: ");
        String pw = sc.nextLine();
        String qry = "SELECT * FROM users WHERE u_email = ? AND u_pass = ?";
        List<Map<String, Object>> result = db.fetchRecords(qry, em, pw);

        if (result.isEmpty()) {
            System.out.println("Invalid credentials! 🛑");
        } else {
            Map<String, Object> user = result.get(0);
            String role = (String) user.get("u_type");
            System.out.println("Welcome, " + user.get("u_name") + " (" + role + ")! 👋");
            

            if (role.equalsIgnoreCase("Admin")) adminMenu(db, sc);
            else if (role.equalsIgnoreCase("Staff")) staffMenu(db, sc);
            else if (role.equalsIgnoreCase("Resident")) residentMenu(db, sc, em);
        }
    }
    
    // ===================== ADMIN MENU (Full Control) ===========================
    
    public static void adminMenu(config db, Scanner sc) {
        int choice;
        do {
            System.out.println("\n--- ADMIN MENU (Full Management) ---");
            System.out.println("1. Manage Residents");
            System.out.println("2. Manage Puroks"); 
            System.out.println("0. Logout");
            System.out.print("Choice: ");
            choice = sc.nextInt();

            switch (choice) {
                case 1: manageResidents(db, sc); break;
                case 2: managePuroks(db, sc); break; 
                case 0: return;
                default: System.out.println("Invalid choice!");
            }
        } while (choice != 0);
    }
    
    // ===================== STAFF MENU (View/Reports) ===========================
    
    public static void staffMenu(config db, Scanner sc) {
        int choice;
        do {
            System.out.println("\n--- STAFF MENU (View/Reports) ---");
            // New Menu Item
            System.out.println("1. View Residents of a Specific Purok");
            System.out.println("2. View All Residents");
            System.out.println("3. View All Puroks"); 
            System.out.println("0. Logout");
            System.out.print("Choice: ");
            choice = sc.nextInt();

            switch (choice) {
                case 1: viewResidentsByPurok(db, sc); break; // New function call
                case 2: viewResidents(); break;
                case 3: viewPuroks(); break; 
                case 0: return;
                default: System.out.println("Invalid choice!");
            }
        } while (choice != 0);
    }

    // ===================== RESIDENT MENU (View Own Info) ===========================
    
    public static void residentMenu(config db, Scanner sc, String email) {
        int choice;
        do {
            System.out.println("\n--- RESIDENT MENU (Own Info) ---");
            System.out.println("1. View My User Info");
            System.out.println("2. View My Purok Info");
            System.out.println("0. Logout");
            System.out.print("Choice: ");
            choice = sc.nextInt();

            switch (choice) {
                case 1:
                    String infoQuery = "SELECT u_name, u_email, u_type FROM users WHERE u_email = ?";
                    List<Map<String, Object>> infoResult = db.fetchRecords(infoQuery, email);
                    if (!infoResult.isEmpty()) {
                        Map<String, Object> userInfo = infoResult.get(0);
                        System.out.println("\n--- My Information ---");
                        System.out.println("Name   : " + userInfo.get("u_name"));
                        System.out.println("Email  : " + userInfo.get("u_email"));
                        System.out.println("Type   : " + userInfo.get("u_type"));
                    } else { System.out.println("No information found."); }
                    break;
                    
                case 2:
                    // Query updated: p_leader removed, and now shows resident's purok name
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
                    break;

                case 0: return;
                default: System.out.println("Invalid choice!");
            }
        } while (choice != 0);
    }

    // ===================== RESIDENT CRUD ===========================
    
    public static void manageResidents(config db, Scanner sc) {
        int choice;
        do {
            System.out.println("\n--- MANAGE RESIDENTS ---");
            System.out.println("1. Add Resident (Create)");
            System.out.println("2. View All Residents (Read)");
            System.out.println("3. Update Resident (Update)");
            System.out.println("4. Delete Resident (Delete)");
            System.out.println("5. View Residents by Purok");
            System.out.println("0. Back");
            System.out.print("Choice: ");
            choice = sc.nextInt();

            switch (choice) {
                case 1:
                    sc.nextLine(); 
                    System.out.print("Enter resident name: ");
                    String rname = sc.nextLine();
                    System.out.print("Enter age: ");
                    int age = sc.nextInt();
                    System.out.print("Enter gender: ");
                    sc.nextLine();
                    String gen = sc.nextLine();
                    

                    System.out.println("\n--- Available Puroks ---");
                    viewPuroks();
                    System.out.print("Enter Purok ID (must be an existing ID above): ");
                    int p_id = sc.nextInt(); 
                    
                    if (!purokExists(db, p_id)) {
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
                    // ------------------------------------------------
                    System.out.print("Enter Resident ID to update: ");
                    int rid = sc.nextInt(); sc.nextLine();
                    System.out.print("New name: ");
                    String newName = sc.nextLine();
                    System.out.print("New age: ");
                    int newAge = sc.nextInt();
                    System.out.print("New gender: ");
                    sc.nextLine();
                    String newGender = sc.nextLine();
                    

                    System.out.println("\n--- Available Puroks ---");
                    viewPuroks();
                    System.out.print("New Purok ID (must be an existing ID): ");
                    int newPId = sc.nextInt();
                    
                    if (!purokExists(db, newPId)) {
                        System.out.println("🛑 Error: Cannot update resident. Purok ID " + newPId + " does not exist.");
                        break;
                    }
                    // --- END PUROK VALIDATION ---

                    db.updateRecord("UPDATE residents SET r_name = ?, r_age = ?, r_gender = ?, r_purok_id = ? WHERE r_id = ?", newName, newAge, newGender, newPId, rid);
                    System.out.println("Resident updated successfully.");
                    break;
                case 4:
                    // --- VIEW ALL RESIDENTS BEFORE DELETE PROMPT ---
                    System.out.println("\n--- Current Residents ---");
                    viewResidents(); 
                    // ------------------------------------------------
                    System.out.print("Enter Resident ID to delete: ");
                    int delId = sc.nextInt();
                    db.deleteRecord("DELETE FROM residents WHERE r_id = ?", delId);
                    System.out.println("Resident deleted successfully.");
                    break;
                case 5: viewResidentsByPurok(db, sc); break;
                case 0: return;
                default: System.out.println("Invalid choice!");
            }
        } while (choice != 0);
    }
    
    // ===================== PUROK CRUD =========================== 
    
    public static void managePuroks(config db, Scanner sc) {
        int choice;
        do {
            System.out.println("\n--- MANAGE PUROKS ---"); 
            System.out.println("1. Add Purok (Create)"); 
            System.out.println("2. View All Puroks (Read)"); 
            System.out.println("3. Update Purok (Update)"); 
            System.out.println("4. Delete Purok (Delete)"); 
            System.out.println("0. Back");
            System.out.print("Choice: ");
            choice = sc.nextInt();

            switch (choice) {
                case 1:
                    sc.nextLine();
                    System.out.print("Enter purok name: "); 
                    String name = sc.nextLine(); 
                    

                    System.out.println("Purok added successfully."); 
                    break;
                case 2: viewPuroks(); break; 
                case 3:
                    // --- VIEW ALL PUROKS BEFORE UPDATE PROMPT ---
                    System.out.println("\n--- Current Puroks ---");
                    viewPuroks(); 
                    // ------------------------------------------
                    System.out.print("Enter Purok ID to update: ");
                    int pid = sc.nextInt(); sc.nextLine(); 
                    
                    if (!purokExists(db, pid)) {
                        System.out.println("🛑 Error: Purok ID " + pid + " does not exist.");
                        break;
                    }

                    System.out.print("New name: "); 
                    String newName = sc.nextLine(); 
                    

                    db.updateRecord("UPDATE puroks SET p_name = ? WHERE p_id = ?", newName, pid);
                    System.out.println("Purok updated successfully."); 
                    break;
                case 4:
                    // --- VIEW ALL PUROKS BEFORE DELETE PROMPT ---
                    System.out.println("\n--- Current Puroks ---");
                    viewPuroks(); 
                    // ------------------------------------------
                    System.out.print("Enter Purok ID to delete: ");
                    int delPId = sc.nextInt(); 
                    
                    db.deleteRecord("DELETE FROM puroks WHERE p_id = ?", delPId);
                    System.out.println("Purok deleted successfully."); 
                    break;
                case 0: return;
                default: System.out.println("Invalid choice!");
            }
        } while (choice != 0);
    }
}