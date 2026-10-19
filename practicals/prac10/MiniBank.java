import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

class Account implements Comparable<Account> {
    private final String accountNumber;
    private long balance;

    public Account(String accountNumber, long balance) {
        this.accountNumber = accountNumber;
        this.balance = balance;
    }

    public synchronized void deposit(long amount) {
        this.balance += amount;
    }

    public synchronized void withdraw(long amount) {
        if (amount <= balance) {
            this.balance -= amount;
        }
    }

    public synchronized long getBalance() {
        return balance;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    @Override
    public int compareTo(Account o) {
        return this.accountNumber.compareTo(o.accountNumber);
    }
}

class TransactionProcessor {
    private final ExecutorService pool;

    public TransactionProcessor(int threads) {
        this.pool = Executors.newFixedThreadPool(threads);
    }

    public void submit(Runnable task) {
        pool.execute(task);
    }

    public void stop() {
        pool.shutdown();
        try {
            if (!pool.awaitTermination(5, TimeUnit.SECONDS)) {
                pool.shutdownNow();
            }
        } catch (InterruptedException e) {
            pool.shutdownNow();
        }
    }
}

public class MiniBank {
    public static void safeTransfer(Account from, Account to, long amount) {
        // Enforce deterministic global lock acquisition order: smaller account number first
        Account first = from.compareTo(to) < 0 ? from : to;
        Account second = (first == from) ? to : from;

        synchronized (first) {
            synchronized (second) {
                from.withdraw(amount);
                to.deposit(amount);
            }
        }
    }

    public static void main(String[] args) {
        System.out.println("PARTH PARIKH || 25DCS070");
        System.out.println("Practical 10 - Part B: Concurrent Batch Engine & Deadlock Prevention\n");

        TransactionProcessor processor = new TransactionProcessor(4);
        Account a1 = new Account("AC1001", 50000);
        Account a2 = new Account("AC1002", 50000);

        System.out.println("Submitting batch transfers concurrently...");

        // Thread 1: transfers from AC1001 to AC1002
        // Thread 2: transfers from AC1002 to AC1001
        for (int i = 0; i < 20; i++) {
            processor.submit(() -> safeTransfer(a1, a2, 100));
            processor.submit(() -> safeTransfer(a2, a1, 100));
        }

        processor.stop();
        System.out.println("All batch tasks safely completed on Thread Pool without deadlocks!");
        System.out.printf("Final Balances -> AC1001: Rs. %d | AC1002: Rs. %d%n", a1.getBalance(), a2.getBalance());
        System.out.println("Total System Solvency Maintained: Rs. " + (a1.getBalance() + a2.getBalance()));
    }
}
