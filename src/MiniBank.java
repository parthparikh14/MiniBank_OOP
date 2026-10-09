import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

class Customer implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String name;

    public Customer(String name) {
        this.name = name;
    }

    public String getName() { return name; }
}

class Account implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String accountNumber;
    private final Customer customer;
    private long balance;

    public Account(String accountNumber, Customer customer, long balance) {
        this.accountNumber = accountNumber;
        this.customer = customer;
        this.balance = balance;
    }

    public String getAccountNumber() { return accountNumber; }
    public Customer getCustomer() { return customer; }
    public long getBalance() { return balance; }

    @Override
    public String toString() {
        return "Account[" + accountNumber + " | " + customer.getName() + " | Rs. " + balance + "]";
    }
}

class StatePersister {
    public static void save(Account[] accounts, Path file) throws IOException {
        if (file.getParent() != null && !Files.exists(file.getParent())) {
            Files.createDirectories(file.getParent());
        }
        try (ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(file))) {
            oos.writeObject(accounts);
        }
    }

    public static Account[] load(Path file) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(file))) {
            return (Account[]) ois.readObject();
        }
    }
}

class TransactionLog {
    public static void append(Path file, String line) throws IOException {
        if (file.getParent() != null && !Files.exists(file.getParent())) {
            Files.createDirectories(file.getParent());
        }
        Files.writeString(file, line + System.lineSeparator(),
                StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    }
}

class ReportGenerator {
    public static void generateReport(Path logsDir, Path reportFile) throws IOException {
        long totalDeposits = 0;
        long totalWithdrawals = 0;

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(logsDir, "*.log")) {
            for (Path entry : stream) {
                BasicFileAttributes attrs = Files.readAttributes(entry, BasicFileAttributes.class);
                System.out.println("Processing Log: " + entry.getFileName() + " (Size: " + attrs.size() + " bytes)");
                List<String> lines = Files.readAllLines(entry);
                for (String line : lines) {
                    if (line.contains("DEPOSIT")) {
                        totalDeposits += parseAmount(line);
                    } else if (line.contains("WITHDRAW")) {
                        totalWithdrawals += parseAmount(line);
                    }
                }
            }
        }

        String report = String.format("""
                =========================================
                      MINIBANK END-OF-DAY AUDIT REPORT   
                =========================================
                Total Inflow (Deposits)   : Rs. %d
                Total Outflow (Withdrawals): Rs. %d
                Net Reserve Fluctuation   : Rs. %d
                =========================================
                """, totalDeposits, totalWithdrawals, (totalDeposits - totalWithdrawals));

        Files.writeString(reportFile, report, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        System.out.println(report);
    }

    private static long parseAmount(String line) {
        try {
            String[] parts = line.split("\\|");
            for (String part : parts) {
                if (part.trim().startsWith("Rs.")) {
                    return Long.parseLong(part.trim().replace("Rs.", "").trim());
                }
            }
        } catch (Exception ignored) {}
        return 0;
    }
}

public class MiniBank {
    public static void main(String[] args) throws Exception {
        System.out.println("PARTH PARIKH || 25DCS070");
        System.out.println("Practical 11 - Part B: Persistence & NIO Audit Trail\n");

        Path dataPath = Paths.get("data/accounts.dat");
        Path logPath = Paths.get("logs/transactions.log");
        Path reportPath = Paths.get("logs/eod_report.txt");

        Account[] accounts = new Account[]{
                new Account("AC1001", new Customer("Parth Parikh"), 68000),
                new Account("AC1002", new Customer("Aarav Patel"), 31000),
                new Account("AC1003", new Customer("Diya Shah"), 38000)
        };

        System.out.println("Persisting account array to binary storage via ObjectOutputStream...");
        StatePersister.save(accounts, dataPath);

        System.out.println("Reloading accounts via ObjectInputStream...");
        Account[] reloaded = StatePersister.load(dataPath);
        for (Account a : reloaded) {
            System.out.println("   Rehydrated: " + a);
        }

        System.out.println("\nLogging financial mutations to NIO audit log...");
        TransactionLog.append(logPath, "DEPOSIT | AC1001 | Rs. 5000");
        TransactionLog.append(logPath, "WITHDRAW | AC1002 | Rs. 2000");

        System.out.println("\nGenerating End-of-Day Audit Report via NIO DirectoryStream:");
        ReportGenerator.generateReport(Paths.get("logs"), reportPath);
    }
}
