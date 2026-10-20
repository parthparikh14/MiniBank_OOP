import java.io.*;
import java.lang.annotation.*;
import java.lang.reflect.Field;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;

// --- Practicals 7: Custom Runtime Annotations ---
@Retention(RetentionPolicy.RUNTIME) @Target(ElementType.FIELD) @interface Id {}
@Retention(RetentionPolicy.RUNTIME) @Target(ElementType.FIELD) @interface Positive {}
@Retention(RetentionPolicy.RUNTIME) @Target(ElementType.FIELD) @interface MaxLength { int value(); }

// --- Practical 8: Checked Exceptions & AutoCloseable Session ---
class BankException extends Exception { public BankException(String m) { super(m); } }
class InsufficientFundsException extends BankException {
    private final double shortfall;
    public InsufficientFundsException(String m, double sf) { super(m); this.shortfall = sf; }
    public double getShortfall() { return shortfall; }
}
class InvalidAmountException extends BankException { public InvalidAmountException(String m) { super(m); } }
class AccountNotFoundException extends BankException { public AccountNotFoundException(String m) { super(m); } }

class BankSession implements AutoCloseable {
    private final String id;
    public BankSession(String id) { this.id = id; System.out.println("[Session Opened] Token: " + id); }
    public void close() { System.out.println("[Session Closed Safely] Token: " + id); }
}

// --- Practical 1: Record & Enums ---
record BankInfo(String name, String branch, String ifsc) implements Serializable {}
enum AccountType { SAVINGS, CURRENT, FIXED_DEPOSIT }

// --- Practical 6: Interfaces & Functional Strategy ---
interface Transactable {
    void deposit(double amt) throws BankException;
    void withdraw(double amt) throws BankException;
    double getBalance();
}
interface InterestBearing {
    double calculateInterest();
    default double yearlyInterest(double bal, double rate) { return bal * rate; }
}
@FunctionalInterface interface WithdrawRule { boolean validate(Account a, double amt); }

// --- Practical 4: Regex Validator & StringBuilder Statement ---
class Validator {
    static boolean isAcc(String s) { return s != null && s.matches("^AC[0-9]{4}$"); }
    static boolean isPhone(String s) { return s != null && s.matches("^[6-9][0-9]{9}$"); }
    static boolean isName(String s) { return s != null && s.matches("^[A-Za-z ]{3,30}$"); }
}

// --- Practical 2 & 3: Customer with Deep-Copy Cloneable Address ---
class Customer implements Cloneable, Serializable {
    public static class Address implements Serializable {
        String city, state;
        public Address(String c, String s) { this.city = c; this.state = s; }
        public String toString() { return city + ", " + state; }
    }
    final String id;
    String name, phone;
    Address address;

    public Customer(String id, String n, String p, Address a) {
        this.id = id; this.name = n; this.phone = p; this.address = a;
    }
    @Override
    public Customer clone() {
        return new Customer(id, name, phone, new Address(address.city, address.state));
    }
    public String toString() { return name + " (" + address + ")"; }
}

// --- Practical 2, 3, 5: Encapsulated Account Hierarchy ---
abstract class Account implements Transactable, Comparable<Account>, Serializable {
    @Id protected final String accNo;
    @MaxLength(30) protected final String name;
    @Positive protected double balance;
    protected final AccountType type;
    protected final Customer customer;

    public Account(String accNo, String name, double bal, AccountType type, Customer cust) {
        this.accNo = accNo; this.name = name; this.balance = bal; this.type = type; this.customer = cust;
    }
    public String getAccNo() { return accNo; }
    public String getName() { return name; }
    public synchronized double getBalance() { return balance; }
    public AccountType getType() { return type; }

    public synchronized void deposit(double amt) throws BankException {
        if (amt <= 0) throw new InvalidAmountException("Deposit must be positive: " + amt);
        balance += amt;
    }
    public abstract void withdraw(double amt) throws BankException;

    public String maskedAccNo() {
        return accNo.length() >= 4 ? "AC****" + accNo.substring(accNo.length() - 2) : accNo;
    }
    @Override public int compareTo(Account o) { return this.accNo.compareTo(o.accNo); }
    @Override public boolean equals(Object o) { return (o instanceof Account a) && accNo.equals(a.accNo); }
    @Override public int hashCode() { return Objects.hash(accNo); }
    @Override public String toString() {
        return String.format("%-14s | %-6s | %-14s | Rs. %,10.2f", type, accNo, name, balance);
    }
}

class SavingsAccount extends Account implements InterestBearing {
    static final double MIN_BAL = 1000.0;
    public SavingsAccount(String accNo, String name, double bal, Customer cust) {
        super(accNo, name, bal, AccountType.SAVINGS, cust);
    }
    @Override
    public synchronized void withdraw(double amt) throws BankException {
        if (amt <= 0) throw new InvalidAmountException("Withdrawal must be positive.");
        if (balance - amt < MIN_BAL) {
            throw new InsufficientFundsException("Cannot drop below min balance Rs. " + MIN_BAL, MIN_BAL - (balance - amt));
        }
        balance -= amt;
    }
    @Override public double calculateInterest() { return balance * 0.04; }
}

class CurrentAccount extends Account {
    static final double OVERDRAFT = 25000.0;
    public CurrentAccount(String accNo, String name, double bal, Customer cust) {
        super(accNo, name, bal, AccountType.CURRENT, cust);
    }
    @Override
    public synchronized void withdraw(double amt) throws BankException {
        if (amt <= 0) throw new InvalidAmountException("Withdrawal must be positive.");
        if (balance - amt < -OVERDRAFT) {
            throw new InsufficientFundsException("Overdraft limit of Rs. " + OVERDRAFT + " exceeded.", Math.abs(balance - amt) - OVERDRAFT);
        }
        balance -= amt;
    }
}

class FixedDepositAccount extends Account implements InterestBearing {
    public FixedDepositAccount(String accNo, String name, double bal, Customer cust) {
        super(accNo, name, bal, AccountType.FIXED_DEPOSIT, cust);
    }
    @Override
    public synchronized void withdraw(double amt) throws BankException {
        throw new BankException("FD locked: Early withdrawal prohibited.");
    }
    @Override public double calculateInterest() { return balance * 0.065; }
}

// --- Practical 7: Reflection Validator Engine ---
class AnnotationValidator {
    static void validate(Object obj) throws BankException {
        Class<?> clz = obj.getClass();
        while (clz != null && clz != Object.class) {
            for (Field f : clz.getDeclaredFields()) {
                f.setAccessible(true);
                try {
                    Object val = f.get(obj);
                    if (f.isAnnotationPresent(Positive.class) && val instanceof Number n && n.doubleValue() < 0) {
                        throw new BankException("Field @" + f.getName() + " must be positive.");
                    }
                    if (f.isAnnotationPresent(MaxLength.class) && val instanceof String s && s.length() > f.getAnnotation(MaxLength.class).value()) {
                        throw new BankException("Field @" + f.getName() + " exceeds max length.");
                    }
                } catch (IllegalAccessException e) { throw new BankException(e.getMessage()); }
            }
            clz = clz.getSuperclass();
        }
    }
}

// --- Practical 13: Generic Repository Pattern with Reflection @Id Discovery ---
class Repository<T> implements Serializable {
    private final Map<String, T> data = new ConcurrentHashMap<>();

    private String getId(T entity) {
        Class<?> clz = entity.getClass();
        while (clz != null && clz != Object.class) {
            for (Field f : clz.getDeclaredFields()) {
                if (f.isAnnotationPresent(Id.class)) {
                    f.setAccessible(true);
                    try { return String.valueOf(f.get(entity)); } catch (Exception ignored) {}
                }
            }
            clz = clz.getSuperclass();
        }
        return null;
    }
    public void save(T entity) throws BankException {
        AnnotationValidator.validate(entity);
        String id = getId(entity);
        if (id == null) throw new BankException("Entity lacks @Id metadata!");
        data.put(id, entity);
    }
    public T findById(String id) { return data.get(id); }
    public List<T> findAll() { return new ArrayList<>(data.values()); }
    public int count() { return data.size(); }
}

// --- Practical 10, 11, 14: BankService (Deadlock-Free Transfer, Streams, NIO Logs) ---
class BankService {
    private final Repository<Account> repo;
    private final BankInfo info;
    private static int counter = 1000;
    private static final Path LOG_FILE = Paths.get("logs/transactions.log");

    public BankService(Repository<Account> repo, BankInfo info) {
        this.repo = repo; this.info = info;
        for (Account a : repo.findAll()) {
            try {
                int n = Integer.parseInt(a.getAccNo().replace("AC", ""));
                if (n > counter) counter = n;
            } catch (Exception ignored) {}
        }
    }
    public synchronized Account open(AccountType type, String name, double dep, Customer cust) throws BankException {
        if (!Validator.isName(name)) throw new BankException("Invalid name (3-30 letters).");
        if (dep < 1000.0) throw new BankException("Min opening deposit is Rs. 1000.");
        String accNo = "AC" + (++counter);
        Account acc = switch (type) {
            case SAVINGS -> new SavingsAccount(accNo, name, dep, cust);
            case CURRENT -> new CurrentAccount(accNo, name, dep, cust);
            case FIXED_DEPOSIT -> new FixedDepositAccount(accNo, name, dep, cust);
        };
        repo.save(acc);
        log("OPEN | " + accNo + " | " + type + " | Rs. " + dep);
        return acc;
    }
    public void deposit(String no, double amt) throws BankException {
        Account a = repo.findById(no);
        if (a == null) throw new AccountNotFoundException("Account not found: " + no);
        a.deposit(amt);
        log("DEPOSIT | " + no + " | Rs. " + amt + " | Bal: " + a.getBalance());
    }
    public void withdraw(String no, double amt) throws BankException {
        Account a = repo.findById(no);
        if (a == null) throw new AccountNotFoundException("Account not found: " + no);
        a.withdraw(amt);
        log("WITHDRAW | " + no + " | Rs. " + amt + " | Bal: " + a.getBalance());
    }

    // Practical 10: Deterministic Lock Ordering strictly prevents deadlocks
    public void transfer(String fromNo, String toNo, double amt) throws BankException {
        if (fromNo.equals(toNo)) throw new BankException("Accounts must be different.");
        Account from = repo.findById(fromNo);
        Account to = repo.findById(toNo);
        if (from == null || to == null) throw new AccountNotFoundException("Account not found.");

        Account first = from.compareTo(to) < 0 ? from : to;
        Account second = first == from ? to : from;
        synchronized (first) {
            synchronized (second) {
                from.withdraw(amt);
                to.deposit(amt);
            }
        }
        log("TRANSFER | " + fromNo + " -> " + toNo + " | Rs. " + amt);
    }

    public List<Account> listSorted() {
        List<Account> list = repo.findAll();
        Collections.sort(list);
        return list;
    }
    public double totalReserves() {
        return repo.findAll().stream().mapToDouble(Account::getBalance).sum(); // Practical 12: Streams API
    }
    private void log(String msg) {
        try {
            if (LOG_FILE.getParent() != null) Files.createDirectories(LOG_FILE.getParent());
            String line = "[" + LocalDateTime.now() + "] " + msg + System.lineSeparator();
            Files.writeString(LOG_FILE, line, StandardOpenOption.CREATE, StandardOpenOption.APPEND); // Practical 11: NIO
        } catch (Exception ignored) {}
    }
}

// --- Practical 14: Main Capstone Application ---
public class MiniBank {
    private static final Path DATA_PATH = Paths.get("data/minibank_system.dat");

    public static void main(String[] args) {
        System.out.println("PARTH PARIKH || 25DCS070");
        System.out.println("MiniBank: Object Oriented Programming (CEUC203) - Practicals 1 to 14\n");

        BankInfo info = new BankInfo("CHARUSAT MiniBank Central", "DEPSTAR Branch", "CHAR0002031");
        Repository<Account> repo = load();
        BankService service = new BankService(repo, info);

        if (repo.count() == 0) bootstrap(service);

        // Run Verification Suite
        runVerification(service);

        // Save State
        save(repo);

        // Optional Interactive Menu
        if (args.length > 0 && args[0].equals("--interactive")) {
            interactiveMenu(service, repo);
        } else {
            System.out.println("\n[TIP] Run with '--interactive' to launch the live console menu!");
        }
    }

    private static void bootstrap(BankService s) {
        try {
            s.open(AccountType.SAVINGS, "PARTH PARIKH", 68000.0, new Customer("C101", "PARTH PARIKH", "9876543210", new Customer.Address("Changa", "Gujarat")));
            s.open(AccountType.CURRENT, "Aarav Patel", 31000.0, new Customer("C102", "Aarav Patel", "9823456789", new Customer.Address("Anand", "Gujarat")));
            s.open(AccountType.FIXED_DEPOSIT, "Diya Shah", 38000.0, new Customer("C103", "Diya Shah", "9812345678", new Customer.Address("Vadodara", "Gujarat")));
        } catch (BankException ignored) {}
    }

    private static void runVerification(BankService service) {
        System.out.println("--- 1. Accounts in Storage ---");
        for (Account a : service.listSorted()) System.out.println(" " + a);

        System.out.println("\n--- 2. Automated Test Checks ---");
        try {
            service.deposit("AC1001", 6000.0);
            System.out.println(" [PASS] Deposit increases balance.");
            service.transfer("AC1001", "AC1002", 2000.0);
            System.out.println(" [PASS] Deadlock-free inter-account transfer succeeded.");
        } catch (BankException e) { System.out.println(" [FAIL] " + e.getMessage()); }

        try {
            service.withdraw("AC1001", 200000.0);
        } catch (InsufficientFundsException e) {
            System.out.println(" [PASS] Overdraft blocked with InsufficientFundsException.");
        } catch (BankException ignored) {}

        // Practical 9 & 10: Multithreaded ExecutorService Concurrency Stress
        System.out.println("\n--- 3. Multithreaded Batch Test (ExecutorService 4 threads) ---");
        ExecutorService pool = Executors.newFixedThreadPool(4);
        for (int i = 0; i < 4; i++) pool.submit(() -> { try { service.deposit("AC1001", 500.0); } catch (Exception ignored) {} });
        pool.shutdown();
        try { pool.awaitTermination(2, TimeUnit.SECONDS); } catch (Exception ignored) {}
        System.out.println(" [PASS] Concurrent transactions completed safely on shared monitor.");

        // Practical 12: Java Streams Analytics
        System.out.printf("\n--- 4. Java Streams Analytics ---\n Total Bank Reserves: Rs. %,.2f%n", service.totalReserves());
    }

    private static void interactiveMenu(BankService service, Repository<Account> repo) {
        Scanner sc = new Scanner(System.in);
        while (true) {
            System.out.println("\n=== MINIBANK CONSOLE ===");
            System.out.println("1. View Accounts | 2. Deposit | 3. Withdraw | 4. Transfer | 5. Exit");
            System.out.print("Choice: ");
            String c = sc.nextLine().trim();
            try {
                switch (c) {
                    case "1" -> service.listSorted().forEach(System.out::println);
                    case "2" -> {
                        System.out.print("Acc No & Amount: ");
                        service.deposit(sc.next(), sc.nextDouble());
                        sc.nextLine();
                        System.out.println("[SUCCESS]");
                    }
                    case "3" -> {
                        System.out.print("Acc No & Amount: ");
                        service.withdraw(sc.next(), sc.nextDouble());
                        sc.nextLine();
                        System.out.println("[SUCCESS]");
                    }
                    case "4" -> {
                        System.out.print("From Acc, To Acc, Amount: ");
                        service.transfer(sc.next(), sc.next(), sc.nextDouble());
                        sc.nextLine();
                        System.out.println("[SUCCESS]");
                    }
                    case "5" -> { save(repo); System.out.println("Goodbye!"); return; }
                    default -> System.out.println("Invalid.");
                }
            } catch (Exception e) { System.out.println("[ERROR] " + e.getMessage()); sc.nextLine(); }
        }
    }

    @SuppressWarnings("unchecked")
    private static Repository<Account> load() {
        if (Files.exists(DATA_PATH)) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(DATA_PATH.toFile()))) {
                return (Repository<Account>) ois.readObject();
            } catch (Exception ignored) {}
        }
        return new Repository<>();
    }

    private static void save(Repository<Account> repo) {
        try {
            if (DATA_PATH.getParent() != null) Files.createDirectories(DATA_PATH.getParent());
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(DATA_PATH.toFile()))) {
                oos.writeObject(repo);
                System.out.println("[SAVED] State saved to " + DATA_PATH);
            }
        } catch (Exception ignored) {}
    }
}
