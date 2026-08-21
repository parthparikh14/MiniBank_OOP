import java.util.Scanner;

record BankInfo(String name, String branch) {}

enum MenuOption {
    OPEN_ACCOUNT,
    DEPOSIT,
    WITHDRAW,
    TRANSFER,
    EXIT
}

public class MiniBank {
    public static void main(String[] args) {
        System.out.println("PARTH PARIKH || 25DCS070");
        System.out.println("Practical 01 - Part B: MiniBank Console Menu Shell & Records\n");

        BankInfo bankInfo = new BankInfo("CHARUSAT MiniBank Central", "DEPSTAR Branch");
        System.out.println("==================================================");
        System.out.println("  " + bankInfo.name() + " (" + bankInfo.branch() + ")");
        System.out.println("==================================================");

        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        while (running) {
            System.out.println("\n--- MAIN MENU ---");
            System.out.println("1. Open Account");
            System.out.println("2. Deposit");
            System.out.println("3. Withdraw");
            System.out.println("4. Transfer");
            System.out.println("5. Exit");
            System.out.print("Select an option (1-5): ");

            if (!scanner.hasNextInt()) {
                System.out.println("[ERROR] Invalid choice. Please enter a valid number.");
                scanner.next();
                continue;
            }

            int choice = scanner.nextInt();
            MenuOption selectedOption = switch (choice) {
                case 1 -> MenuOption.OPEN_ACCOUNT;
                case 2 -> MenuOption.DEPOSIT;
                case 3 -> MenuOption.WITHDRAW;
                case 4 -> MenuOption.TRANSFER;
                case 5 -> MenuOption.EXIT;
                default -> null;
            };

            if (selectedOption == null) {
                System.out.println("[WARNING] Unknown option! Please select between 1 and 5.");
                continue;
            }

            switch (selectedOption) {
                case OPEN_ACCOUNT -> System.out.println(">> Open Account: Feature will be implemented in Practical 2.");
                case DEPOSIT -> System.out.println(">> Deposit: Feature will be implemented in Practical 2.");
                case WITHDRAW -> System.out.println(">> Withdraw: Feature will be implemented in Practical 2.");
                case TRANSFER -> System.out.println(">> Transfer: Feature will be implemented in Practical 2.");
                case EXIT -> {
                    System.out.println("\nThank you for choosing CHARUSAT MiniBank. Goodbye!");
                    running = false;
                }
            }
        }
    }
}
