interface Transactable {
    void deposit(long amount);
    boolean withdraw(long amount);
    long getBalance();
}

interface InterestBearing {
    double interestRate();
    default double yearlyInterest(long balance) {
        return balance * (interestRate() / 100.0);
    }
}

@FunctionalInterface
interface WithdrawRule {
    boolean allow(Account account, long amount);
}

interface Premium {}

abstract class Account implements Transactable {
    protected final String accountNumber;
    protected final String ownerName;
    protected long balance;

    public Account(String accountNumber, String ownerName, long balance) {
        this.accountNumber = accountNumber;
        this.ownerName = ownerName;
        this.balance = balance;
    }

    @Override
    public void deposit(long amount) {
        if (amount > 0) balance += amount;
    }

    @Override
    public long getBalance() {
        return balance;
    }

    public String getAccountNumber() { return accountNumber; }
    public String getOwnerName() { return ownerName; }
}

class SavingsAccount extends Account implements InterestBearing, Premium {
    public SavingsAccount(String accountNumber, String ownerName, long balance) {
        super(accountNumber, ownerName, balance);
    }

    @Override
    public boolean withdraw(long amount) {
        if (amount > 0 && balance - amount >= 1000) {
            balance -= amount;
            return true;
        }
        return false;
    }

    @Override
    public double interestRate() {
        return 4.0;
    }
}

public class MiniBank {
    public static void main(String[] args) {
        System.out.println("PARTH PARIKH || 25DCS070");
        System.out.println("Practical 06 - Part B: Interfaces & Pluggable Strategy Rules\n");

        SavingsAccount sa = new SavingsAccount("AC1001", "Parth Parikh", 68000);
        System.out.println("Initial Balance: Rs. " + sa.getBalance());
        System.out.println("Yearly Computed Interest (Default Method): Rs. " + sa.yearlyInterest(sa.getBalance()));

        WithdrawRule vipRuleAnonymous = new WithdrawRule() {
            @Override
            public boolean allow(Account account, long amount) {
                return amount <= 50000 && account.getBalance() >= amount;
            }
        };

        WithdrawRule vipRuleLambda = (account, amount) -> amount <= 100000 && account.getBalance() >= amount;

        System.out.println("\n--- Testing Functional Interface (WithdrawRule) ---");
        System.out.println("Anonymous Rule (Allow Rs. 40,000):  " + vipRuleAnonymous.allow(sa, 40000));
        System.out.println("Anonymous Rule (Allow Rs. 60,000):  " + vipRuleAnonymous.allow(sa, 60000));
        System.out.println("Lambda Rule    (Allow Rs. 60,000):  " + vipRuleLambda.allow(sa, 60000));

        System.out.println("\nMarker Interface Check:");
        System.out.println("Is SavingsAccount marked Premium? " + (sa instanceof Premium));
    }
}
