class Customer {
    private static long customerCounter = 100;
    private final String customerId;
    private String name;
    private String email;
    private String mobile;

    public Customer(String name, String email, String mobile) {
        this.customerId = generateCustomerId();
        this.name = name;
        this.email = email;
        this.mobile = mobile;
    }

    private static String generateCustomerId() {
        return "CUST" + (++customerCounter);
    }

    public String getCustomerId() { return customerId; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getMobile() { return mobile; }
}

class Account {
    private static long accountCounter = 0;
    private final String accountNumber;
    private String ownerName;
    private long balance;
    private boolean active;

    public Account(String ownerName, long balance) {
        this.accountNumber = String.format("AC%04d", ++accountCounter);
        this.ownerName = ownerName;
        this.balance = Math.max(0, balance);
        this.active = true;
    }

    public Account(String ownerName) {
        this(ownerName, 0);
    }

    public void deposit(long amount) {
        if (amount > 0) {
            this.balance += amount;
            System.out.printf("[%s] Successfully deposited Rs. %d. Current Balance: Rs. %d%n", accountNumber, amount, balance);
        } else {
            System.out.println("[REJECTED] Deposit amount must be positive.");
        }
    }

    public boolean withdraw(long amount) {
        if (amount <= 0) {
            System.out.println("[REJECTED] Withdrawal amount must be positive.");
            return false;
        }
        if (amount <= balance) {
            balance -= amount;
            System.out.printf("[%s] Successfully withdrew Rs. %d. Current Balance: Rs. %d%n", accountNumber, amount, balance);
            return true;
        } else {
            System.out.printf("[%s] Insufficient funds! Attempted to withdraw Rs. %d with balance Rs. %d%n", accountNumber, amount, balance);
            return false;
        }
    }

    public String getAccountNumber() { return accountNumber; }
    public String getOwnerName() { return ownerName; }
    public long getBalance() { return balance; }
    public boolean isActive() { return active; }
}

public class MiniBank {
    public static void main(String[] args) {
        System.out.println("PARTH PARIKH || 25DCS070");
        System.out.println("Practical 02 - Part B: Account Model & Invariant Encapsulation\n");

        Customer c1 = new Customer("Parth Parikh", "parth@charusat.edu.in", "9876543210");
        System.out.println("Customer registered: " + c1.getCustomerId() + " - " + c1.getName());

        Account[] accounts = new Account[3];
        accounts[0] = new Account("Parth Parikh", 5000);
        accounts[1] = new Account("Aarav Patel", 3000);
        accounts[2] = new Account("Diya Shah");

        System.out.println("\n--- Testing Transactions ---");
        accounts[0].deposit(2000);
        accounts[0].withdraw(3000);
        boolean failed = accounts[0].withdraw(10000);
        System.out.println("Large withdrawal allowed? " + failed);

        System.out.println("\n--- Final Account Status ---");
        for (Account acc : accounts) {
            System.out.printf("Account: %s | Owner: %-15s | Balance: Rs. %d%n",
                    acc.getAccountNumber(), acc.getOwnerName(), acc.getBalance());
        }
    }
}
