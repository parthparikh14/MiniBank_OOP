abstract class Account {
    protected final String accountNumber;
    protected final String ownerName;
    protected long balance;

    public Account(String accountNumber, String ownerName, long balance) {
        this.accountNumber = accountNumber;
        this.ownerName = ownerName;
        this.balance = balance;
    }

    public abstract double interestRate();
    public abstract boolean canWithdraw(long amount);

    public void deposit(long amount) {
        if (amount > 0) balance += amount;
    }

    public boolean withdraw(long amount) {
        if (canWithdraw(amount)) {
            balance -= amount;
            return true;
        }
        return false;
    }

    public String getAccountNumber() { return accountNumber; }
    public String getOwnerName() { return ownerName; }
    public long getBalance() { return balance; }
}

class SavingsAccount extends Account {
    private final long minBalance;

    public SavingsAccount(String accountNumber, String ownerName, long balance, long minBalance) {
        super(accountNumber, ownerName, balance);
        this.minBalance = minBalance;
    }

    @Override
    public double interestRate() {
        return 4.0;
    }

    @Override
    public boolean canWithdraw(long amount) {
        return amount > 0 && (balance - amount >= minBalance);
    }
}

class CurrentAccount extends Account {
    private final long overdraftLimit;

    public CurrentAccount(String accountNumber, String ownerName, long balance, long overdraftLimit) {
        super(accountNumber, ownerName, balance);
        this.overdraftLimit = overdraftLimit;
    }

    @Override
    public double interestRate() {
        return 0.0;
    }

    @Override
    public boolean canWithdraw(long amount) {
        return amount > 0 && (balance - amount >= -overdraftLimit);
    }
}

class FixedDepositAccount extends Account {
    private final int termMonths;

    public FixedDepositAccount(String accountNumber, String ownerName, long balance, int termMonths) {
        super(accountNumber, ownerName, balance);
        this.termMonths = termMonths;
    }

    public int getTermMonths() { return termMonths; }

    @Override
    public double interestRate() {
        return 7.0;
    }

    @Override
    public boolean canWithdraw(long amount) {
        return false; // locked until maturity
    }
}

public class MiniBank {
    public static void main(String[] args) {
        System.out.println("PARTH PARIKH || 25DCS070");
        System.out.println("Practical 05 - Part B: Polymorphic Account Hierarchy\n");

        Account[] accounts = new Account[]{
                new SavingsAccount("AC1001", "Parth Parikh", 68000, 1000),
                new CurrentAccount("AC1002", "Aarav Patel", 31000, 25000),
                new FixedDepositAccount("AC1003", "Diya Shah", 38000, 12)
        };

        System.out.println("--- Dynamic Method Dispatch & Runtime Polymorphism ---");
        for (Account acc : accounts) {
            System.out.printf("Account: %s | Type: %-20s | Interest Rate: %.1f%%%n",
                    acc.getAccountNumber(), acc.getClass().getSimpleName(), acc.interestRate());

            if (acc instanceof FixedDepositAccount fd) {
                System.out.printf("   -> Special FD Maturity Term: %d months (Early liquidation prohibited)%n",
                        fd.getTermMonths());
            }
        }

        System.out.println("\n--- Testing Withdrawal Invariants ---");
        System.out.println("Savings AC1001 withdraw 67500 (below min 1000): " + accounts[0].withdraw(67500));
        System.out.println("Savings AC1001 withdraw 5000 (valid):          " + accounts[0].withdraw(5000));
        System.out.println("Current AC1002 withdraw 40000 (within overdraft 25000): " + accounts[1].withdraw(40000));
        System.out.println("FixedDeposit AC1003 withdraw 1000:             " + accounts[2].withdraw(1000));
    }
}
