class Account {
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
}

class AccountWorker implements Runnable {
    private final Account account;
    private final int times;
    private final long amount;

    public AccountWorker(Account account, int times, long amount) {
        this.account = account;
        this.times = times;
        this.amount = amount;
    }

    @Override
    public void run() {
        System.out.println("Thread [" + Thread.currentThread().getName() + "] started processing.");
        for (int i = 0; i < times; i++) {
            account.deposit(amount);
        }
        System.out.println("Thread [" + Thread.currentThread().getName() + "] finished deposits.");
    }
}

public class MiniBank {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("PARTH PARIKH || 25DCS070");
        System.out.println("Practical 09 - Part B: Multithreaded Synchronization & Monitors\n");

        Account account = new Account("AC1001", 0);
        int threadCount = 10;
        int depositsPerThread = 1000;
        long amountPerDeposit = 1;

        System.out.println("Starting " + threadCount + " concurrent worker threads...");
        Thread[] threads = new Thread[threadCount];

        for (int i = 0; i < threadCount; i++) {
            threads[i] = new Thread(new AccountWorker(account, depositsPerThread, amountPerDeposit), "Worker-" + (i + 1));
            threads[i].start();
        }

        for (int i = 0; i < threadCount; i++) {
            threads[i].join();
        }

        System.out.println("\nAll worker threads terminated.");
        System.out.println("Expected Final Balance: Rs. " + (threadCount * depositsPerThread * amountPerDeposit));
        System.out.println("Actual Synchronized Balance: Rs. " + account.getBalance());
        System.out.println("Thread Safety Verified: " + (account.getBalance() == 10000));
    }
}
