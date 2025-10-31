package main;

import config.config;
import java.util.Scanner;
import services.PurokService;
import services.ResidentService;
import services.UserService;

public class main {

    public static void main(String[] args) {
        config db = new config();
        db.connectDB();
        Scanner sc = new Scanner(System.in);
        char cont = 'Y';
        int choice;

        PurokService purokService = new PurokService(db);
        ResidentService residentService = new ResidentService(db, purokService);
        UserService userService = new UserService(db);

        do {
            System.out.println("\n===== PUROK RESIDENCE INFORMATION SYSTEM =====");
            System.out.println("1. Register User");
            System.out.println("2. Login");
            System.out.println("0. Exit");
            System.out.print("Enter choice: ");

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
                    userService.loginUser(sc, residentService); 
                    break;

                case 0: 
                    cont = 'N';
                    break;
                    
                default: 
                    System.out.println("Invalid choice!");
            }
            
         
            if (choice != 0) {
                System.out.print("\nDo you want to continue? (Y/N): ");
                String continueInput = sc.nextLine();
                
                if (continueInput.isEmpty()) {
                    cont = 'N';
                } else {
                    cont = continueInput.toUpperCase().charAt(0);
                }
            }
        } while (cont == 'Y');


        System.out.println("Program Ended. Goodbye! 👋");
        sc.close();
        System.exit(0);
    }
}