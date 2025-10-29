package services;

import config.config;
import java.util.*;

public class UserService {
    private final config db;

    public UserService(config db) {
        this.db = db;
    }

    // ===================== ID VALIDATION HELPERS ===========================

    private boolean userExists(int userId) {
        String query = "SELECT COUNT(u_id) AS Count FROM users WHERE u_id = ?";
        List<Map<String, Object>> result = db.fetchRecords(query, userId);
        if (!result.isEmpty()) {
            Object countObj = result.get(0).get("Count"); 
            if (countObj instanceof Long) return (Long) countObj > 0;
            if (countObj instanceof Integer) return (Integer) countObj > 0;
        }
        return false;
    }

    private void viewAllUsers() {
        String query = "SELECT u_id, u_name, u_email, u_type, u_status FROM users";
        String[] headers = {"ID", "Name", "Email", "Type", "Status"};
        String[] cols = {"u_id", "u_name", "u_email", "u_type", "u_status"};
        
        List<Map<String, Object>> result = db.fetchRecords(query);
        
        if (result.isEmpty()) {
            System.out.println("\nNo users found.");
            return;
        }

        System.out.println("\n--- All System Users ---");
        System.out.printf("%-5s %-20s %-25s %-10s %-10s\n", headers[0], headers[1], headers[2], headers[3], headers[4]);
        System.out.println("----------------------------------------------------------------------");
        
        for (Map<String, Object> record : result) {
            System.out.printf("%-5s %-20s %-25s %-10s %-10s\n", 
                record.get(cols[0]), 
                record.get(cols[1]), 
                record.get(cols[2]), 
                record.get(cols[3]),
                record.get(cols[4]));
        }
        System.out.println("----------------------------------------------------------------------");
    }

    // ===================== USER CRUD/FLOW ===========================

    public void registerUser(Scanner sc) {
        System.out.print("Enter name: ");
        String name = sc.nextLine();
        System.out.print("Enter email: ");
        String email = sc.nextLine();
        System.out.print("Enter password: ");
        String pass = sc.nextLine();
        
        // **MODIFICATION: Prompt changed to Admin/Staff only**
        System.out.print("Enter type (Admin/Staff): ");
        String typeInput = sc.nextLine();
        String typeToStore;

        // Logic to restrict registration to only Admin or Staff
        if (typeInput.equalsIgnoreCase("Admin") || typeInput.equalsIgnoreCase("Staff")) {
            typeToStore = typeInput;
        } else {
            // **MODIFICATION: Updated error message**
            System.out.println("🛑 Registration Failed: Invalid type entered. New users must be registered as **Admin** or **Staff**.");
            return; // Exit the method without adding a record
        }

        String regSql = "INSERT INTO users(u_name, u_email, u_type, u_status, u_pass) VALUES (?, ?, ?, ?, ?)";
        db.addRecord(regSql, name, email, typeToStore, "Active", pass); 
        System.out.println("User registered successfully! 🎉 Account is **Active**.");
    }

    public void loginUser(Scanner sc, ResidentService residentService) {
        System.out.print("Enter email: ");
        String em = sc.nextLine();
        System.out.print("Enter password: ");
        String pw = sc.nextLine();
        
        String qry = "SELECT * FROM users WHERE u_email = ? AND u_pass = ?";
        List<Map<String, Object>> result = db.fetchRecords(qry, em, pw);

        if (result.isEmpty()) {
            System.out.println("Invalid credentials! 🛑");
        } else {
            Map<String, Object> user = result.get(0);
            String status = (String) user.get("u_status");

            if (status.equalsIgnoreCase("Active")) {
                String role = (String) user.get("u_type");
                
                System.out.println("Welcome, " + user.get("u_name") + " (" + role + ")! 👋");
                
                if (role.equalsIgnoreCase("Admin")) adminMenu(sc, residentService);
                else if (role.equalsIgnoreCase("Staff")) staffMenu(sc, residentService);
                else {
                    // This case should ideally not happen with the new registration logic
                    System.out.println("🛑 Error: Your account type is unsupported for login: " + role);
                }
            } else {
                System.out.println("🛑 Login Failed. Your account status is **" + status + "**.");
            }
        }
    }
    
    // ===================== MENU FUNCTIONS ===========================

    private void adminMenu(Scanner sc, ResidentService residentService) {
        int choice = 0;
        do {
            System.out.println("\n--- ADMIN MENU ---");
            System.out.println("1. Residence and Purok Management");
            System.out.println("2. Manage User Accounts");
            System.out.println("0. Logout");
            System.out.print("Choice: ");
             if (!sc.hasNextInt()) {
                 System.out.println("🛑 Error: Invalid input. Please enter a number.");
                 sc.nextLine(); 
                 continue;
             }
            choice = sc.nextInt();
            sc.nextLine();

            switch (choice) {
                case 1: residentService.dataManagementMenu(sc); break;
                case 2: manageUsers(sc); break; 
                case 0: return;
                default: System.out.println("Invalid choice!");
            }
        } while (choice != 0);
    }
    
    private void staffMenu(Scanner sc, ResidentService residentService) {
        int choice = 0;
        do {
            System.out.println("\n--- STAFF MENU (View/Reports) ---");
            System.out.println("1. View Residents of a Specific Purok");
            System.out.println("2. View All Residents");
            System.out.println("3. View All Puroks"); 
            System.out.println("0. Logout");
            System.out.print("Choice: ");
            if (!sc.hasNextInt()) {
                System.out.println("🛑 Error: Invalid input. Please enter a number.");
                sc.nextLine(); 
                continue;
            }
            choice = sc.nextInt();

            switch (choice) {
                case 1: residentService.viewResidentsByPurok(sc); break; 
                case 2: residentService.viewResidents(); break;
                case 3: residentService.viewPuroks(); break; 
                case 0: return;
                default: System.out.println("Invalid choice!");
            }
        } while (choice != 0);
    }
    
    // No dedicated resident/purokleader menu is needed as registration is restricted

    // ===================== ADMIN MANAGEMENT FUNCTIONS ===========================
    
    private void manageUsers(Scanner sc) {
        int subChoice = 0;
        do {
            System.out.println("\n--- MANAGE USER ACCOUNTS ---");
            System.out.println("1. View All Users");
            System.out.println("2. Change User Status");
            System.out.println("3. Delete User Account"); 
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
                case 1:
                    viewAllUsers();
                    break;
                case 2:
                    changeUserStatus(sc); 
                    break;
                case 3:
                    deleteUser(sc); 
                    break;
                case 0:
                    return;
                default:
                    System.out.println("Invalid choice!");
            }
        } while (subChoice != 0);
    }

    private void changeUserStatus(Scanner sc) {
        viewAllUsers();
        System.out.print("Enter User ID to update status: ");
        int userId;
        if (sc.hasNextInt()) {
            userId = sc.nextInt();
        } else {
               System.out.println("🛑 Error: Invalid input. Please enter a number.");
               sc.nextLine(); 
               return;
        }
        sc.nextLine(); 
        
        if (!userExists(userId)) {
            System.out.println("🛑 Error: User ID " + userId + " does not exist. Returning to menu.");
            return; 
        }
        
        System.out.print("Enter new status (Active, Pending, Deactivated): ");
        String newStatus = sc.nextLine();
        
        if (!newStatus.equalsIgnoreCase("Active") && !newStatus.equalsIgnoreCase("Pending") && !newStatus.equalsIgnoreCase("Deactivated")) {
            System.out.println("🛑 Error: Invalid status. Must be Active, Pending, or Deactivated.");
            return;
        }

        db.updateRecord("UPDATE users SET u_status = ? WHERE u_id = ?", newStatus, userId);
        System.out.println("User ID " + userId + " status updated to **" + newStatus + "** successfully. 🎉");
    }
    
    private void deleteUser(Scanner sc) {
        viewAllUsers();
        System.out.print("Enter User ID to **DELETE**: ");
        int userId;
        if (sc.hasNextInt()) {
            userId = sc.nextInt();
        } else {
               System.out.println("🛑 Error: Invalid input. Please enter a number.");
               sc.nextLine(); 
               return;
        }
        sc.nextLine();
        
        if (!userExists(userId)) {
            System.out.println("🛑 Error: User ID " + userId + " does not exist. Returning to menu.");
            return; 
        }
        
        System.out.print("⚠️ WARNING: Deleting a user is permanent. Confirm deletion for User ID " + userId + " (Y/N): ");
        String confirmation = sc.nextLine();
        
        if (confirmation.equalsIgnoreCase("Y")) {
             db.deleteRecord("DELETE FROM users WHERE u_id = ?", userId);
             System.out.println("User ID " + userId + " **deleted** successfully.🗑️");
        } else {
             System.out.println("Deletion cancelled.");
        }
    }
}