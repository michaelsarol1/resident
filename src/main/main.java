package main;

import config.config;
import java.util.Scanner;
import services.PurokService; // NEW IMPORT
import services.ResidentService;
import services.UserService;

public class main {

    public static void main(String[] args) {
        config db = new config();
        db.connectDB();
        Scanner sc = new Scanner(System.in);
        char cont = 0;

        // --- FIX REQUIRED HERE ---
        // 1. Instantiate the database utility services first (PurokService)
        PurokService purokService = new PurokService(db);
        
        // 2. Instantiate ResidentService, passing the required dependency (PurokService)
        ResidentService residentService = new ResidentService(db, purokService);

        // 3. Instantiate UserService
        UserService userService = new UserService(db);
        // --- END OF FIX ---

        do {
            System.out.println("\n===== PUROK RESIDENCE INFORMATION SYSTEM =====");
            System.out.println("1. Register User");
            System.out.println("2. Login");
            System.out.println("0. Exit");
            System.out.print("Enter choice: ");
            int choice;

            if (sc.hasNextInt()) {
                choice = sc.nextInt();
                sc.nextLine();
            } else {
                System.out.println("🛑 Error: Invalid input. Please enter a number.");
                sc.nextLine();
                continue;
            }

            switch (choice) {
                case 1:
                    userService.registerUser(sc);
                    break;

                case 2: 
                    // Pass the correctly instantiated ResidentService instance to loginUser
                    userService.loginUser(sc, residentService); 
                    break;
                    
                case 0: 
                    System.out.println("Program Ended. Goodbye! 👋");
                    sc.close();
                    db.closeDB(); // Ensure DB connection is closed gracefully
                    System.exit(0); 
                    break;
                default: 
                    System.out.println("Invalid choice!");
            }

            System.out.print("\nDo you want to continue? (Y/N): ");
            
            // Consume the rest of the line to safely get input
            String continueInput = sc.nextLine();
            
            // Check if input is empty or just use the first character
            if (continueInput.isEmpty()) {
                cont = 'N';
            } else {
                cont = continueInput.charAt(0);
            }

        } while (cont == 'Y' || cont == 'y');

        System.out.println("Program Ended. Goodbye! 👋");
        sc.close();
        db.closeDB();
    }
}