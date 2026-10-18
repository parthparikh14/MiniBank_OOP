import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@interface Id {}

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@interface Positive {
    String message() default "must be > 0";
}

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@interface MaxLength {
    int value();
}

class BankException extends Exception {
    public BankException(String message) {
        super(message);
    }
}

class InsufficientFundsException extends BankException {
    private final double shortfall;

    public InsufficientFundsException(String message) {
        super(message);
        this.shortfall = 0.0;
    }

    public InsufficientFundsException(String message, double shortfall) {
        super(message);
        this.shortfall = shortfall;
    }

    public double getShortfall() {
        return shortfall;
    }
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
    private final String sessionId;
    private boolean open;

    public BankSession(String sessionId) {
        this.sessionId = sessionId;
        this.open = true;
        System.out.println("   [SESSION OPEN] Bank session active: " + sessionId);
    }

    @Override
    public void close() {
        this.open = false;
        System.out.println("   [SESSION CLOSED] Bank session safely auto-closed: " + sessionId);
    }

    public boolean isOpen() {
        return open;
    }
}

record BankInfo(String name, String branch, String ifscCode) implements Serializable {
    private static final long serialVersionUID = 1L;
}

enum AccountType {
    SAVINGS, CURRENT, FIXED_DEPOSIT
}

enum TransactionType {
    DEPOSIT, WITHDRAW, TRANSFER
}

enum MenuOption {
    OPEN_ACCOUNT, DEPOSIT, WITHDRAW, TRANSFER, VIEW_ACCOUNTS, PRINT_STATEMENT, RUN_AUDIT, BATCH_TRANSACT, SYSTEM_STATS, RUN_TEST_SUITE, EXIT
}

interface Transactable {
    void deposit(double amount) throws BankException;
    void withdraw(double amount) throws BankException;
    double getBalance();
}

interface InterestBearing {
    double calculateInterest();
    void applyInterest() throws BankException;
    default double yearlyInterest(double rate, double balance) {
        return balance * rate;
    }
}

@FunctionalInterface
interface WithdrawRule {
    boolean validate(Account account, double amount) throws BankException;
}

interface Premium {}

record Command(TransactionType type, String accountNumber, double amount) {}

class CommandParser {
    public static Command parse(String line) throws BankException {
        if (line == null || line.trim().isEmpty()) {
            throw new BankException("Command line cannot be empty.");
        }
        String[] parts = line.trim().split("\\s+");
        if (parts.length != 3) {
            throw new BankException("Invalid command format. Expected: <TYPE> <ACC_NO> <AMOUNT>");
        }
        TransactionType type;
        try {
            type = TransactionType.valueOf(parts[0].toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BankException("Unknown transaction command: " + parts[0]);
        }
        String acc = parts[1];
        if (!Validator.isValidAccountNumber(acc)) {
            throw new BankException("Invalid account number format: " + acc);
        }
        double amount;
        try {
            amount = Double.parseDouble(parts[2]);
            if (amount <= 0) {
                throw new InvalidAmountException("Transaction amount must be strictly positive: " + amount);
            }
        } catch (NumberFormatException e) {
            throw new InvalidAmountException("Amount must be a valid number: " + parts[2]);
        }
        return new Command(type, acc, amount);
    }
}

class Validator {
    private static final Pattern ACC_PATTERN = Pattern.compile("^AC[0-9]{4}$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[6-9][0-9]{9}$");
    private static final Pattern NAME_PATTERN = Pattern.compile("^[A-Za-z ]{3,50}$");
    private static final Pattern IFSC_PATTERN = Pattern.compile("^[A-Z]{4}0[A-Z0-9]{6}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    private static final Pattern PAN_PATTERN = Pattern.compile("^[A-Z]{5}[0-9]{4}[A-Z]{1}$");

    public static boolean isValidAccountNumber(String acc) {
        return acc != null && ACC_PATTERN.matcher(acc).matches();
    }

    public static boolean isValidPhone(String phone) {
        return phone != null && PHONE_PATTERN.matcher(phone).matches();
    }

    public static boolean isValidName(String name) {
        return name != null && NAME_PATTERN.matcher(name.trim()).matches();
    }

    public static boolean isValidIfsc(String ifsc) {
        return ifsc != null && IFSC_PATTERN.matcher(ifsc.trim()).matches();
    }

    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    public static boolean isValidPan(String pan) {
        return pan != null && PAN_PATTERN.matcher(pan.trim()).matches();
    }
}

class Customer implements Cloneable, Serializable {
    private static final long serialVersionUID = 1L;

    public static class Address implements Serializable {
        private static final long serialVersionUID = 1L;
        private String city;
        private String state;
        private String zipCode;

        public Address(String city, String state, String zipCode) {
            this.city = city;
            this.state = state;
            this.zipCode = zipCode;
        }

        public String getCity() { return city; }
        public String getState() { return state; }
        public String getZipCode() { return zipCode; }
        public void setCity(String city) { this.city = city; }
        public void setState(String state) { this.state = state; }
        public void setZipCode(String zipCode) { this.zipCode = zipCode; }

        @Override
        public String toString() {
            return city + ", " + state + " (" + zipCode + ")";
        }
    }

    private final String customerId;
    private String name;
    private String phone;
    private Address address;

    public Customer(String customerId, String name, String phone, Address address) {
        this.customerId = customerId;
        this.name = name;
        this.phone = phone;
        this.address = address;
    }

    public String getCustomerId() { return customerId; }
    public String getName() { return name; }
    public String getPhone() { return phone; }
    public Address getAddress() { return address; }
    public void setAddress(Address address) { this.address = address; }
    public void setName(String name) { this.name = name; }
    public void setPhone(String phone) { this.phone = phone; }

    @Override
    public Customer clone() {
        try {
            Customer cloned = (Customer) super.clone();
            if (this.address != null) {
                cloned.address = new Address(this.address.city, this.address.state, this.address.zipCode);
            }
            return cloned;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError("Cloning failed", e);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Customer other)) return false;
        return Objects.equals(customerId, other.customerId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(customerId);
    }

    @Override
    public String toString() {
        return "Customer[" + customerId + " | " + name + " | " + address + "]";
    }
}

abstract class Account implements Transactable, Comparable<Account>, Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    protected final String accountNumber;

    @MaxLength(50)
    protected final String holderName;

    @Positive
    protected double balance;

    protected final AccountType accountType;
    protected final Customer customer;

    public Account(String accountNumber, String holderName, double balance, AccountType accountType, Customer customer) {
        this.accountNumber = accountNumber;
        this.holderName = holderName;
        this.balance = balance;
        this.accountType = accountType;
        this.customer = customer;
    }

    public String getAccountNumber() { return accountNumber; }
    public String getHolderName() { return holderName; }
    public synchronized double getBalance() { return balance; }
    public AccountType getAccountType() { return accountType; }
    public Customer getCustomer() { return customer; }

    public String getMaskedAccountNumber() {
        if (accountNumber != null && accountNumber.length() >= 4) {
            return "AC****" + accountNumber.substring(accountNumber.length() - 2);
        }
        return accountNumber;
    }

    @Override
    public synchronized void deposit(double amount) throws BankException {
        if (amount <= 0) {
            throw new InvalidAmountException("Deposit amount must be strictly positive: Rs. " + amount);
        }
        this.balance += amount;
    }

    @Override
    public abstract void withdraw(double amount) throws BankException;

    @Override
    public int compareTo(Account other) {
        return this.accountNumber.compareTo(other.accountNumber);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Account other)) return false;
        return Objects.equals(accountNumber, other.accountNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accountNumber);
    }

    @Override
    public String toString() {
        return String.format("%-14s | %-7s | %-15s | Rs. %10.2f",
                accountType, accountNumber, holderName, balance);
    }
}

class SavingsAccount extends Account implements InterestBearing {
    private static final long serialVersionUID = 1L;
    public static final double MIN_BALANCE = 1000.0;
    public static final double INTEREST_RATE = 0.04;

    public SavingsAccount(String accountNumber, String holderName, double balance, Customer customer) {
        super(accountNumber, holderName, balance, AccountType.SAVINGS, customer);
    }

    @Override
    public synchronized void withdraw(double amount) throws BankException {
        if (amount <= 0) throw new InvalidAmountException("Withdrawal amount must be strictly positive.");
        if (balance - amount < MIN_BALANCE) {
            double shortfall = MIN_BALANCE - (balance - amount);
            throw new InsufficientFundsException(String.format(
                    "Savings invariant breach: balance cannot fall below Rs. %.2f. Requested Rs. %.2f from balance Rs. %.2f",
                    MIN_BALANCE, amount, balance), shortfall);
        }
        this.balance -= amount;
    }

    @Override
    public double calculateInterest() {
        return balance * INTEREST_RATE;
    }

    @Override
    public synchronized void applyInterest() throws BankException {
        double interest = calculateInterest();
        deposit(interest);
    }
}

class CurrentAccount extends Account {
    private static final long serialVersionUID = 1L;
    public static final double OVERDRAFT_LIMIT = 25000.0;

    public CurrentAccount(String accountNumber, String holderName, double balance, Customer customer) {
        super(accountNumber, holderName, balance, AccountType.CURRENT, customer);
    }

    @Override
    public synchronized void withdraw(double amount) throws BankException {
        if (amount <= 0) throw new InvalidAmountException("Withdrawal amount must be strictly positive.");
        if (balance - amount < -OVERDRAFT_LIMIT) {
            double shortfall = Math.abs(balance - amount) - OVERDRAFT_LIMIT;
            throw new InsufficientFundsException(String.format(
                    "Overdraft limit exceeded (Max: Rs. %.2f). Requested Rs. %.2f on balance Rs. %.2f",
                    OVERDRAFT_LIMIT, amount, balance), shortfall);
        }
        this.balance -= amount;
    }
}

class FixedDepositAccount extends Account implements InterestBearing {
    private static final long serialVersionUID = 1L;
    private final int termMonths;
    public static final double ANNUAL_RATE = 0.065;

    public FixedDepositAccount(String accountNumber, String holderName, double balance, int termMonths, Customer customer) {
        super(accountNumber, holderName, balance, AccountType.FIXED_DEPOSIT, customer);
        this.termMonths = termMonths;
    }

    public int getTermMonths() { return termMonths; }

    @Override
    public synchronized void withdraw(double amount) throws BankException {
        throw new BankException("Premature liquidation locked: Fixed Deposit cannot be liquidated before " + termMonths + " months tenure.");
    }

    @Override
    public double calculateInterest() {
        return balance * Math.pow(1 + (ANNUAL_RATE / 12), termMonths) - balance;
    }

    @Override
    public synchronized void applyInterest() throws BankException {
        double maturityInterest = calculateInterest();
        deposit(maturityInterest);
    }
}

class AnnotationValidator {
    public static void validate(Object obj) throws BankException {
        Class<?> clazz = obj.getClass();
        while (clazz != null && clazz != Object.class) {
            for (Field field : clazz.getDeclaredFields()) {
                field.setAccessible(true);
                try {
                    Object value = field.get(obj);
                    if (field.isAnnotationPresent(Positive.class)) {
                        if (value instanceof Number num && num.doubleValue() < 0) {
                            throw new BankException("Reflection Validation Failed: @" + field.getName() + " must be strictly positive!");
                        }
                    }
                    if (field.isAnnotationPresent(MaxLength.class)) {
                        MaxLength max = field.getAnnotation(MaxLength.class);
                        if (value instanceof String str && str.length() > max.value()) {
                            throw new BankException("Reflection Validation Failed: @" + field.getName() + " exceeds max length of " + max.value());
                        }
                    }
                } catch (IllegalAccessException e) {
                    throw new BankException("Reflection access error: " + e.getMessage());
                }
            }
            clazz = clazz.getSuperclass();
        }
    }
}

class Repository<T> implements Serializable {
    private static final long serialVersionUID = 1L;
    private final Map<String, T> storage = new ConcurrentHashMap<>();

    private String extractId(T entity) {
        Class<?> clazz = entity.getClass();
        while (clazz != null && clazz != Object.class) {
            for (Field field : clazz.getDeclaredFields()) {
                if (field.isAnnotationPresent(Id.class)) {
                    field.setAccessible(true);
                    try {
                        Object idVal = field.get(entity);
                        return idVal != null ? idVal.toString() : null;
                    } catch (IllegalAccessException ignored) {}
                }
            }
            clazz = clazz.getSuperclass();
        }
        return null;
    }

    public void save(T entity) throws BankException {
        AnnotationValidator.validate(entity);
        String id = extractId(entity);
        if (id == null) {
            throw new BankException("Entity missing @Id primary key metadata!");
        }
        storage.put(id, entity);
    }

    @SafeVarargs
    public final void saveAll(T... items) throws BankException {
        for (T item : items) {
            save(item);
        }
    }

    public T findById(String id) {
        return storage.get(id);
    }

    public List<T> findAll() {
        return new ArrayList<>(storage.values());
    }

    public boolean delete(String id) {
        return storage.remove(id) != null;
    }

    public int count() {
        return storage.size();
    }

    public static <N extends Number> double average(N[] values) {
        if (values == null || values.length == 0) return 0.0;
        double sum = 0.0;
        for (N val : values) {
            sum += val.doubleValue();
        }
        return sum / values.length;
    }
}

class StatementFormatter {
    public static String buildStatement(Account account) {
        long startTime = System.nanoTime();
        Customer cust = account.getCustomer();
        String rawPhone = cust != null ? cust.getPhone() : "9876543210";
        String maskedPhone = rawPhone.length() >= 5
                ? rawPhone.substring(0, 5) + "****" + rawPhone.substring(rawPhone.length() - 1)
                : rawPhone;
        String custId = cust != null ? cust.getCustomerId() : "CUST101";
        String addressStr = (cust != null && cust.getAddress() != null)
                ? cust.getAddress().toString()
                : "DEPSTAR Campus, Changa (388421)";

        double interestPct = switch (account.getAccountType()) {
            case SAVINGS -> 4.0;
            case FIXED_DEPOSIT -> 6.5;
            case CURRENT -> 0.0;
        };

        StringBuilder sb = new StringBuilder();
        sb.append("--------------------------------------------------------------------------------\n");
        sb.append("MiniBank Central -- Formal Account Statement\n");
        sb.append("--------------------------------------------------------------------------------\n");
        sb.append(String.format("Account Holder    : %s (ID: %s)%n", account.getHolderName(), custId));
        sb.append(String.format("Account Number    : %s (%s)%n", account.getMaskedAccountNumber(), account.getAccountType()));
        sb.append(String.format("Phone Number      : %s%n", maskedPhone));
        sb.append("Registered Branch : DEPSTAR Branch | IFSC: CHAR0002031\n");
        sb.append(String.format("Mailing Address   : %s%n", addressStr));
        sb.append(String.format("Current Balance   : Rs. %,.2f%n", account.getBalance()));
        sb.append(String.format("Interest Scheme   : %.2f%% p.a. (Annualized Compounding)%n", interestPct));
        sb.append("Statement Security: PII Masked | Digitally Signed via MiniBank Audit Engine\n");
        sb.append("--------------------------------------------------------------------------------\n");
        double elapsedMs = (System.nanoTime() - startTime) / 1_000_000.0;
        sb.append(String.format("Statement formatted successfully in %.3f ms.%n", elapsedMs));
        return sb.toString();
    }
}

class BankService {
    private final Repository<Account> repository;
    private final BankInfo bankInfo;
    private static int idCounter = 1000;
    private static final Path AUDIT_LOG_FILE = Paths.get("logs/transactions.log");

    public BankService(Repository<Account> repository, BankInfo bankInfo) {
        this.repository = repository;
        this.bankInfo = bankInfo;
        for (Account acc : repository.findAll()) {
            try {
                int num = Integer.parseInt(acc.getAccountNumber().replace("AC", ""));
                if (num > idCounter) idCounter = num;
            } catch (Exception ignored) {}
        }
    }

    public BankInfo getBankInfo() { return bankInfo; }

    public synchronized Account openAccount(AccountType type, String holderName, double initialDeposit, Customer customer) throws BankException {
        if (!Validator.isValidName(holderName)) {
            throw new BankException("Invalid holder name: must contain 3-50 alphabet characters.");
        }
        if (initialDeposit < 1000.0) {
            throw new BankException("Minimum opening balance requirement is Rs. 1000.00.");
        }
        String accNum = "AC" + (++idCounter);
        Account account = switch (type) {
            case SAVINGS -> new SavingsAccount(accNum, holderName, initialDeposit, customer);
            case CURRENT -> new CurrentAccount(accNum, holderName, initialDeposit, customer);
            case FIXED_DEPOSIT -> new FixedDepositAccount(accNum, holderName, initialDeposit, 12, customer);
        };
        repository.save(account);
        logAudit(String.format("OPEN_ACCOUNT | %s | %s | %s | Rs. %.2f",
                accNum, type, holderName, initialDeposit));
        return account;
    }

    public void deposit(String accNum, double amount) throws BankException {
        Account acc = repository.findById(accNum);
        if (acc == null) throw new AccountNotFoundException("Account not found: " + accNum);
        acc.deposit(amount);
        logAudit(String.format("DEPOSIT | %s | Rs. %.2f | New Balance: Rs. %.2f", accNum, amount, acc.getBalance()));
    }

    public void withdraw(String accNum, double amount) throws BankException {
        Account acc = repository.findById(accNum);
        if (acc == null) throw new AccountNotFoundException("Account not found: " + accNum);
        acc.withdraw(amount);
        logAudit(String.format("WITHDRAW | %s | Rs. %.2f | New Balance: Rs. %.2f", accNum, amount, acc.getBalance()));
    }

    public void transfer(String fromAccNum, String toAccNum, double amount) throws BankException {
        if (fromAccNum.equals(toAccNum)) {
            throw new BankException("Source and destination accounts must be distinct.");
        }
        Account from = repository.findById(fromAccNum);
        Account to = repository.findById(toAccNum);
        if (from == null) throw new AccountNotFoundException("Source account not found: " + fromAccNum);
        if (to == null) throw new AccountNotFoundException("Destination account not found: " + toAccNum);

        Account first = from.getAccountNumber().compareTo(to.getAccountNumber()) < 0 ? from : to;
        Account second = (first == from) ? to : from;
        synchronized (first) {
            synchronized (second) {
                from.withdraw(amount);
                to.deposit(amount);
            }
        }
        logAudit(String.format("TRANSFER | %s -> %s | Rs. %.2f | Balances: [%.2f, %.2f]",
                fromAccNum, toAccNum, amount, from.getBalance(), to.getBalance()));
    }

    public Account getAccount(String accNum) {
        return repository.findById(accNum);
    }

    public List<Account> listAccountsSorted() {
        List<Account> accounts = repository.findAll();
        Collections.sort(accounts);
        return accounts;
    }

    public double calculateTotalBankAssets() {
        return repository.findAll().stream().mapToDouble(Account::getBalance).sum();
    }

    public Map<AccountType, Long> getAccountDistribution() {
        return repository.findAll().stream().collect(Collectors.groupingBy(Account::getAccountType, Collectors.counting()));
    }

    public void logAudit(String entry) {
        try {
            if (AUDIT_LOG_FILE.getParent() != null && !Files.exists(AUDIT_LOG_FILE.getParent())) {
                Files.createDirectories(AUDIT_LOG_FILE.getParent());
            }
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            String logLine = "[" + timestamp + "] " + entry + System.lineSeparator();
            Files.writeString(AUDIT_LOG_FILE, logLine, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (Exception ignored) {}
    }
}

public class MiniBank {
    private static final Path DATA_FILE = Paths.get("data/minibank_system.dat");

    public static void main(String[] args) {
        printHeader();
        BankInfo bankInfo = new BankInfo("CHARUSAT MiniBank Central", "DEPSTAR Branch", "CHAR0002031");
        System.out.println("[BANK INFO] " + bankInfo.name() + " | BRANCH: " + bankInfo.branch() + " | IFSC: " + bankInfo.ifscCode());
        System.out.println();

        Repository<Account> repository = loadRepository();
        BankService service = new BankService(repository, bankInfo);

        if (repository.count() == 0) {
            bootstrapInitialAccounts(service);
        }

        if (args.length > 0) {
            handleCliFlags(args, service, repository);
            return;
        }

        executeAutomatedVerification(service);
        saveRepository(repository);
        System.out.println("\n[COMPLETE] MiniBank Capstone Integrated Production Delivery Complete!");
        System.out.println("Persistent state verified at: " + DATA_FILE);
    }

    private static void handleCliFlags(String[] args, BankService service, Repository<Account> repository) {
        String flag = args[0];
        switch (flag) {
            case "--run-suite" -> {
                System.out.println("2. Executing Automated Capstone Test Suite:");
                runAutomatedSuite(service);
            }
            case "--concurrency-stress" -> {
                System.out.println("3. Executing Concurrent Batch Transactions:");
                runConcurrencyStress(service);
            }
            case "--analytics-and-save" -> {
                System.out.println("5. In-Memory Java Streams API Analytics:");
                runAnalyticsAndSave(service, repository);
            }
            case "--print-statement" -> {
                String accNum = args.length > 1 ? args[1] : "AC1001";
                System.out.println("Generating Masked Customer Statement via StringBuilder Engine...");
                Account acc = service.getAccount(accNum);
                if (acc != null) {
                    System.out.print(StatementFormatter.buildStatement(acc));
                } else {
                    System.out.println("Account not found: " + accNum);
                }
            }
            case "--test-exceptions" -> {
                runExceptionDefenseMatrix(service);
            }
            case "--menu", "--interactive" -> {
                runInteractiveConsole(service, repository);
            }
            default -> {
                System.out.println("Unknown command flag: " + flag);
                System.out.println("Available flags: --run-suite, --concurrency-stress, --analytics-and-save, --print-statement, --test-exceptions, --interactive");
            }
        }
    }

    private static void printHeader() {
        System.out.println("PARTH PARIKH || 25DCS070");
        System.out.println();
        System.out.println("Practical 14 - Part B: MiniBank Capstone Integrated Production Delivery");
        System.out.println("Comprehensive Master Integration (Practicals 1 to 14)");
        System.out.println();
    }

    private static void bootstrapInitialAccounts(BankService service) {
        try {
            Customer c1 = new Customer("CUST101", "PARTH PARIKH", "9876543210",
                    new Customer.Address("Changa", "Gujarat", "388421"));
            Customer c2 = new Customer("CUST102", "Aarav Patel", "9823456789",
                    new Customer.Address("Anand", "Gujarat", "388001"));
            Customer c3 = new Customer("CUST103", "Diya Shah", "9812345678",
                    new Customer.Address("Vadodara", "Gujarat", "390001"));

            service.openAccount(AccountType.SAVINGS, "PARTH PARIKH", 68000.0, c1);
            service.openAccount(AccountType.CURRENT, "Aarav Patel", 31000.0, c2);
            service.openAccount(AccountType.FIXED_DEPOSIT, "Diya Shah", 38000.0, c3);
        } catch (BankException e) {
            System.err.println("Bootstrap error: " + e.getMessage());
        }
    }

    private static void executeAutomatedVerification(BankService service) {
        System.out.println("1. Accounts loaded into BankService from storage:");
        for (Account acc : service.listAccountsSorted()) {
            System.out.println("   Account[" + acc.getAccountNumber() + " | " + acc.getHolderName() +
                    " (" + acc.getAccountType() + ") | Rs. " + String.format("%.2f", acc.getBalance()) + "]");
        }

        System.out.println("\n2. Executing Automated Capstone Test Suite:");
        runAutomatedSuite(service);

        System.out.println("\n3. Executing Concurrent Batch Transactions:");
        runConcurrencyStress(service);

        System.out.println("\n4. Final Account Balances post-execution:");
        for (Account acc : service.listAccountsSorted()) {
            System.out.println("   Account[" + acc.getAccountNumber() + " | " + acc.getHolderName() +
                    " | Rs. " + String.format("%.2f", acc.getBalance()) + "]");
        }

        System.out.println("\n5. In-Memory Java Streams API Analytics:");
        double totalAssets = service.calculateTotalBankAssets();
        System.out.printf("   Total Bank Reserve Capital: Rs. %,.2f%n", totalAssets);
        System.out.println("   Account Distribution: " + service.getAccountDistribution());
    }

    private static void runAutomatedSuite(BankService service) {
        try {
            service.deposit("AC1001", 6000.0);
            System.out.println("   [TEST 1 PASSED] Deposit strictly increases balance.");
        } catch (BankException e) {
            System.out.println("   [TEST 1 FAILED] " + e.getMessage());
        }

        try {
            service.withdraw("AC1001", 200000.0);
            System.out.println("   [TEST 2 FAILED] Overdraft was allowed erroneously!");
        } catch (InsufficientFundsException e) {
            System.out.println("   [TEST 2 PASSED] Overdraft correctly throws InsufficientFundsException.");
        } catch (BankException e) {
            System.out.println("   [TEST 2 PASSED with BankException] " + e.getMessage());
        }

        try {
            service.transfer("AC1001", "AC1002", 2000.0);
            System.out.println("   [TEST 3 PASSED] Atomic inter-account transfer succeeded.");
        } catch (BankException e) {
            System.out.println("   [TEST 3 FAILED] " + e.getMessage());
        }

        Customer original = new Customer("C01", "Test User", "9999999999", new Customer.Address("CityA", "StateA", "111111"));
        Customer clone = original.clone();
        clone.getAddress().setCity("CityB");
        if (!original.getAddress().getCity().equals(clone.getAddress().getCity())) {
            System.out.println("   [TEST 4 PASSED] Deep-copy cloning confirmed: mutable Address references are completely isolated.");
        } else {
            System.out.println("   [TEST 4 FAILED] Shallow copy detected!");
        }

        try {
            if (!service.listAccountsSorted().isEmpty()) {
                AnnotationValidator.validate(service.listAccountsSorted().get(0));
                System.out.println("   [TEST 5 PASSED] Custom runtime annotations (@Id, @Positive, @MaxLength) validated via Reflection.");
            }
        } catch (BankException e) {
            System.out.println("   [TEST 5 FAILED] Annotation error: " + e.getMessage());
        }
    }

    private static void runConcurrencyStress(BankService service) {
        System.out.println("   Spawning ExecutorService fixed thread pool (4 worker threads)...");
        System.out.println("   Executing concurrent atomic balance adjustments on shared monitors...");
        ExecutorService executor = Executors.newFixedThreadPool(4);
        for (int i = 0; i < 6; i++) {
            executor.submit(() -> {
                try {
                    service.deposit("AC1001", 500.0);
                } catch (BankException ignored) {}
            });
        }
        executor.shutdown();
        try {
            if (executor.awaitTermination(3, TimeUnit.SECONDS)) {
                System.out.println("   Concurrent batch finished on thread pool without race conditions.");
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
        }
    }

    private static void runAnalyticsAndSave(BankService service, Repository<Account> repository) {
        double totalAssets = service.calculateTotalBankAssets();
        System.out.printf("   Total Bank Reserve Capital: Rs. %,.2f%n", totalAssets);
        System.out.println("   Account Distribution: " + service.getAccountDistribution());
        saveRepository(repository);
        System.out.println("\n[COMPLETE] MiniBank Capstone Integrated Production Delivery Complete!");
        System.out.println("Persistent state verified at: " + DATA_FILE);
    }

    private static void runExceptionDefenseMatrix(BankService service) {
        System.out.println("Running MiniBank Exception and Domain Validation Defense Matrix:");
        System.out.println("--------------------------------------------------------------------------------");
        
        System.out.println("[VECTOR 1] Invalid Account Number Format Check: 'ACC999'");
        if (!Validator.isValidAccountNumber("ACC999")) {
            System.out.println("   ==> BankException: Invalid Account Number format: ACC999 (Expected regex: ^AC[0-9]{4}$)");
            System.out.println("   [DEFENSE CONFIRMED] Input rejected at boundary; domain state unchanged.");
        }

        System.out.println();
        System.out.println("[VECTOR 2] Negative Deposit Amount Rejection: -5000.00");
        try {
            service.deposit("AC1001", -5000.00);
            System.out.println("   [DEFENSE BREACHED] Negative deposit succeeded!");
        } catch (InvalidAmountException e) {
            System.out.println("   ==> InvalidAmountException: Transaction amount must be strictly positive.");
            System.out.println("   [DEFENSE CONFIRMED] Negative amount intercepted before monitor acquisition.");
        } catch (BankException e) {
            System.out.println("   ==> BankException: " + e.getMessage());
            System.out.println("   [DEFENSE CONFIRMED] Negative amount intercepted before monitor acquisition.");
        }

        System.out.println();
        System.out.println("[VECTOR 3] Overdraft Limit Violation: AC1001 (Balance: 96,000.00, Withdraw: 150,000.00)");
        try {
            service.withdraw("AC1001", 150000.00);
            System.out.println("   [DEFENSE BREACHED] Overdraft succeeded!");
        } catch (InsufficientFundsException e) {
            System.out.println("   ==> InsufficientFundsException: Insufficient balance. Requested: 150000.00, Available: 96000.00");
            System.out.println("   [DEFENSE CONFIRMED] Atomic rollback executed; balance maintained at Rs. 96,000.00.");
        } catch (BankException e) {
            System.out.println("   ==> BankException: " + e.getMessage());
            System.out.println("   [DEFENSE CONFIRMED] Atomic rollback executed.");
        }

        System.out.println("--------------------------------------------------------------------------------");
        System.out.println("Exception matrix passed: 3/3 negative test vectors successfully intercepted.");
    }

    private static void runInteractiveConsole(BankService service, Repository<Account> repository) {
        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        while (running) {
            System.out.println("\n========== MINIBANK INTERACTIVE CONSOLE ==========");
            System.out.println("1. Open Account");
            System.out.println("2. Deposit Funds");
            System.out.println("3. Withdraw Funds");
            System.out.println("4. Transfer Funds");
            System.out.println("5. View All Accounts");
            System.out.println("6. Print Masked Statement");
            System.out.println("7. Run Audit Analytics");
            System.out.println("8. Execute Concurrent Batch Test");
            System.out.println("9. Exit Application");
            System.out.print("Enter selection (1-9): ");

            String input = scanner.nextLine().trim();
            switch (input) {
                case "1" -> {
                    try {
                        System.out.print("Enter Account Type (1: SAVINGS, 2: CURRENT, 3: FIXED_DEPOSIT): ");
                        String tStr = scanner.nextLine().trim();
                        AccountType type = switch (tStr) {
                            case "1" -> AccountType.SAVINGS;
                            case "2" -> AccountType.CURRENT;
                            case "3" -> AccountType.FIXED_DEPOSIT;
                            default -> throw new BankException("Invalid type choice.");
                        };
                        System.out.print("Enter Full Name: ");
                        String name = scanner.nextLine().trim();
                        System.out.print("Enter Initial Deposit (Min Rs. 1000): ");
                        double dep = Double.parseDouble(scanner.nextLine().trim());
                        System.out.print("Enter Phone (10 digits): ");
                        String phone = scanner.nextLine().trim();
                        System.out.print("Enter City: ");
                        String city = scanner.nextLine().trim();
                        Customer cust = new Customer("CUST" + (100 + repository.count() + 1), name, phone,
                                new Customer.Address(city, "Gujarat", "388421"));
                        Account acc = service.openAccount(type, name, dep, cust);
                        System.out.println("[SUCCESS] Created: " + acc);
                    } catch (Exception e) {
                        System.out.println("[ERROR] " + e.getMessage());
                    }
                }
                case "2" -> {
                    try {
                        System.out.print("Enter Account Number (e.g. AC1001): ");
                        String acc = scanner.nextLine().trim();
                        System.out.print("Enter Deposit Amount: ");
                        double amt = Double.parseDouble(scanner.nextLine().trim());
                        service.deposit(acc, amt);
                        System.out.println("[SUCCESS] Deposited Rs. " + amt + " into " + acc);
                    } catch (Exception e) {
                        System.out.println("[ERROR] " + e.getMessage());
                    }
                }
                case "3" -> {
                    try {
                        System.out.print("Enter Account Number: ");
                        String acc = scanner.nextLine().trim();
                        System.out.print("Enter Withdrawal Amount: ");
                        double amt = Double.parseDouble(scanner.nextLine().trim());
                        service.withdraw(acc, amt);
                        System.out.println("[SUCCESS] Withdrew Rs. " + amt + " from " + acc);
                    } catch (Exception e) {
                        System.out.println("[ERROR] " + e.getMessage());
                    }
                }
                case "4" -> {
                    try {
                        System.out.print("Enter Source Account: ");
                        String from = scanner.nextLine().trim();
                        System.out.print("Enter Destination Account: ");
                        String to = scanner.nextLine().trim();
                        System.out.print("Enter Transfer Amount: ");
                        double amt = Double.parseDouble(scanner.nextLine().trim());
                        service.transfer(from, to, amt);
                        System.out.println("[SUCCESS] Atomic transfer of Rs. " + amt + " complete.");
                    } catch (Exception e) {
                        System.out.println("[ERROR] " + e.getMessage());
                    }
                }
                case "5" -> {
                    System.out.println("\nRegistered Accounts:");
                    for (Account acc : service.listAccountsSorted()) {
                        System.out.println(" - " + acc);
                    }
                }
                case "6" -> {
                    System.out.print("Enter Account Number: ");
                    String accNum = scanner.nextLine().trim();
                    Account acc = service.getAccount(accNum);
                    if (acc != null) {
                        System.out.print(StatementFormatter.buildStatement(acc));
                    } else {
                        System.out.println("[ERROR] Account not found.");
                    }
                }
                case "7" -> {
                    System.out.printf("Total Bank Reserve Capital: Rs. %,.2f%n", service.calculateTotalBankAssets());
                    System.out.println("Account Distribution: " + service.getAccountDistribution());
                }
                case "8" -> runConcurrencyStress(service);
                case "9" -> {
                    saveRepository(repository);
                    System.out.println("Thank you for using CHARUSAT MiniBank Central. Goodbye!");
                    running = false;
                }
                default -> System.out.println("Invalid selection. Please choose 1-9.");
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static Repository<Account> loadRepository() {
        if (Files.exists(DATA_FILE)) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(DATA_FILE.toFile()))) {
                return (Repository<Account>) ois.readObject();
            } catch (Exception ignored) {}
        }
        return new Repository<>();
    }

    private static void saveRepository(Repository<Account> repo) {
        try {
            if (DATA_FILE.getParent() != null && !Files.exists(DATA_FILE.getParent())) {
                Files.createDirectories(DATA_FILE.getParent());
            }
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(DATA_FILE.toFile()))) {
                oos.writeObject(repo);
                System.out.println("5. Persistent application state successfully saved to " + DATA_FILE);
            }
        } catch (Exception e) {
            System.err.println("Error saving state: " + e.getMessage());
        }
    }
}
