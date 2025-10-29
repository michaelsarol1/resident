package main;

import config.config;
import java.util.Scanner;
import services.ResidentService;
import services.UserService; // FIX 1: Corrected package capitalization

public class main {

    public static void main(String[] args) {
        config db = new config();
        db.connectDB();
        Scanner sc = new Scanner(System.in);
        char cont = 0;

        // FIX 2: Removed duplicate 'new' keyword
        UserService userService = new UserService(db); 
        ResidentService residentService = new ResidentService(db);

        do {
            System.out.println("\n===== PUROK RESIDENCE INFORMATION SYSTEM =====");
            System.out.println("1. Register User");
            System.out.println("2. Login");
            System.out.println("0. Exit");
            System.out.print("Enter choice: ");
            int choice;

            if (sc.hasNextInt()) {
                choice = sc.nextInt();
                sc.nextLine(); // Consume newline
            } else {
                System.out.println("🛑 Error: Invalid input. Please enter a number.");
                sc.nextLine(); // Consume invalid input
                continue;
            }

            switch (choice) {
                case 1:
                    userService.registerUser(sc);
                    break;

                case 2: 
                    // Login returns the role and runs the appropriate menu
                    userService.loginUser(sc, residentService); 
                    break;
                    
                case 0: System.exit(0); break;
                default: System.out.println("Invalid choice!");
            }

            System.out.print("\nDo you want to continue? (Y/N): ");
            // Handle Y/N input validation
            String continueInput = sc.next();
            // FIX 3: Simplified initialization of cont (char cont = 0; is unnecessary)
            cont = continueInput.isEmpty() ? 'N' : continueInput.charAt(0); 
            sc.nextLine();

        } while (cont == 'Y' || cont == 'y');

        System.out.println("Program Ended. Goodbye! 👋");
        sc.close();
        db.closeDB(); // Assuming a closeDB method exists in config
    }
}