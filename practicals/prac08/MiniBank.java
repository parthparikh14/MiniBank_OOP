class BankException extends Exception {
    public BankException(String message) {
        super(message);
    }
}

class InsufficientFundsException extends BankException {
    private final long shortfall;

    public InsufficientFundsException(String message, long shortfall) {
        super(message);
        this.shortfall = shortfall;
    }

    public long getShortfall() { return shortfall; }
}

class AccountNotFoundException extends BankException {
    public AccountNotFoundException(String message) {
        super(message);
    }
}

class InvalidAmountException extends BankException {
    public InvalidAmountException(String message) {
        super(message);
    }
}

class BankSession implements AutoCloseable {
    private final String sessionToken;

    public BankSession(String sessionToken) {
        this.sessionToken = sessionToken;
        System.out.println("[SESSION OPENED] Active authenticated token: " + sessionToken);
    }

    @Override
    public void close() {
        System.out.println("[SESSION CLOSED] Safe teardown completed for token: " + sessionToken);
    }
}

class Account {
    private final String accountNumber;
    private final String ownerName;
    private long balance;

    public Account(String accountNumber, String ownerName, long balance) {
        this.accountNumber = accountNumber;
        this.ownerName = ownerName;
        this.balance = balance;
    }

    public void deposit(long amount) throws InvalidAmountException {
        if (amount <= 0) {
            throw new InvalidAmountException("Deposit amount must be strictly positive: " + amount);
        }
        balance += amount;
    }

    public void withdraw(long amount) throws InsufficientFundsException, InvalidAmountException {
        if (amount <= 0) {
            throw new InvalidAmountException("Withdrawal amount must be strictly positive: " + amount);
        }
        if (amount > balance) {
            long shortfall = amount - balance;
            throw new InsufficientFundsException("Insufficient funds. Shortfall: Rs. " + shortfall, shortfall);
        }
        balance -= amount;
    }

    public void transfer(Account to, long amount) throws BankException {
        if (to == null) {
            throw new AccountNotFoundException("Destination account cannot be null.");
        }
        try {
            System.out.println("Initiating transfer of Rs. " + amount + " to " + to.getAccountNumber());
            this.withdraw(amount);
            to.deposit(amount);
            System.out.println("Transfer successful!");
        } catch (InsufficientFundsException | InvalidAmountException e) {
            System.err.println("Transaction failed: " + e.getMessage());
            throw new BankException("Transfer transaction aborted: " + e.getMessage());
        } finally {
            System.out.println("[FINALLY] Transfer lifecycle stage complete.");
        }
    }

    public String getAccountNumber() { return accountNumber; }
    public String getOwnerName() { return ownerName; }
    public long getBalance() { return balance; }
}

public class MiniBank {
    public static void main(String[] args) {
        System.out.println("PARTH PARIKH || 25DCS070");
        System.out.println("Practical 08 - Part B: Custom Checked Exceptions & Session Management\n");

        Account a1 = new Account("AC1001", "Parth Parikh", 1000);
        Account a2 = new Account("AC1002", "Aarav Patel", 2000);

        System.out.println("--- Testing Try-With-Resources (BankSession) ---");
        try (BankSession session = new BankSession("AUTH_TOKEN_777")) {
            System.out.println("Session operational... Performing test withdrawal:");
            a1.withdraw(5000);
        } catch (InsufficientFundsException e) {
            System.out.println("Caught InsufficientFundsException: " + e.getMessage());
            System.out.println("Reported Shortfall: Rs. " + e.getShortfall());
        } catch (InvalidAmountException e) {
            System.out.println("Caught InvalidAmountException: " + e.getMessage());
        }

        System.out.println("\n--- Testing Exception Propagation during Inter-Account Transfer ---");
        try {
            a1.transfer(a2, 2500);
        } catch (BankException e) {
            System.out.println("Interception confirmed in caller: " + e.getMessage());
        }

        System.out.println("\nFinal verified balance of AC1001: Rs. " + a1.getBalance());
    }
}
