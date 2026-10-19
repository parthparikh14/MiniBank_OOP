import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

class Account implements Comparable<Account> {
    private final String accountNumber;
    private final String ownerName;
    private final long balance;

    public Account(String accountNumber, String ownerName, long balance) {
        this.accountNumber = accountNumber;
        this.ownerName = ownerName;
        this.balance = balance;
    }

    public String getAccountNumber() { return accountNumber; }
    public String getOwnerName() { return ownerName; }
    public long getBalance() { return balance; }

    @Override
    public int compareTo(Account other) {
        return this.accountNumber.compareTo(other.accountNumber);
    }

    @Override
    public String toString() {
        return String.format("%s | %-12s | Rs. %,d", accountNumber, ownerName, balance);
    }
}

class AccountStore {
    private final Map<String, Account> accounts = new HashMap<>();
    private final List<String> history = new ArrayList<>();

    public void add(Account account) {
        accounts.put(account.getAccountNumber(), account);
        history.add("REGISTER | " + account.getAccountNumber() + " | " + account.getOwnerName());
    }

    public Account get(String accountNumber) {
        return accounts.get(accountNumber);
    }

    public List<Account> getAll() {
        return new ArrayList<>(accounts.values());
    }

    public List<Account> sortedByBalance() {
        List<Account> sorted = new ArrayList<>(accounts.values());
        sorted.sort(Comparator.comparingLong(Account::getBalance).reversed());
        return sorted;
    }

    public List<String> getHistory() {
        return Collections.unmodifiableList(history);
    }
}

public class MiniBank {
    public static void main(String[] args) {
        System.out.println("PARTH PARIKH || 25DCS070");
        System.out.println("Practical 12 - Part B: Concurrent Collections & Stream Analytics\n");

        AccountStore store = new AccountStore();
        store.add(new Account("AC1002", "Aarav Patel", 31000));
        store.add(new Account("AC1001", "Parth Parikh", 68000));
        store.add(new Account("AC1003", "Diya Shah", 38000));

        System.out.println("--- Lookup by Account Number ---");
        Account found = store.get("AC1001");
        System.out.println("Found: " + found);

        System.out.println("\n--- Accounts Sorted by Balance (Highest First - Comparator) ---");
        for (Account acc : store.sortedByBalance()) {
            System.out.println("   " + acc);
        }

        System.out.println("\n--- Natural Order Sorting (Comparable<Account>) ---");
        List<Account> natural = store.getAll();
        Collections.sort(natural);
        for (Account acc : natural) {
            System.out.println("   " + acc);
        }

        System.out.println("\n--- Streams API Analytics ---");
        long totalCapital = store.getAll().stream().mapToLong(Account::getBalance).sum();
        System.out.printf("Total Bank Reserve Capital: Rs. %,d%n", totalCapital);

        ConcurrentHashMap<String, Account> concurrentStore = new ConcurrentHashMap<>();
        store.getAll().forEach(a -> concurrentStore.put(a.getAccountNumber(), a));
        System.out.println("Thread-Safe ConcurrentHashMap populated: " + concurrentStore.size() + " accounts.");
    }
}
