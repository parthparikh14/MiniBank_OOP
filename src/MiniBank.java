import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@interface Id {}

class Account {
    @Id
    private final String accountNumber;
    private final String holderName;
    private final double balance;

    public Account(String accountNumber, String holderName, double balance) {
        this.accountNumber = accountNumber;
        this.holderName = holderName;
        this.balance = balance;
    }

    public String getAccountNumber() { return accountNumber; }
    public String getHolderName() { return holderName; }
    public double getBalance() { return balance; }

    @Override
    public String toString() {
        return "Account[" + accountNumber + " | " + holderName + " | Rs. " + balance + "]";
    }
}

class Customer {
    @Id
    private final String customerId;
    private final String name;

    public Customer(String customerId, String name) {
        this.customerId = customerId;
        this.name = name;
    }

    public String getCustomerId() { return customerId; }
    public String getName() { return name; }

    @Override
    public String toString() {
        return "Customer[" + customerId + " | " + name + "]";
    }
}

class Repository<T> {
    private final Map<String, T> storage = new HashMap<>();

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

    public T save(T entity) {
        String id = extractId(entity);
        if (id == null) {
            throw new IllegalArgumentException("Entity lacks @Id primary key!");
        }
        storage.put(id, entity);
        return entity;
    }

    @SafeVarargs
    public final void saveAll(T... items) {
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

public class MiniBank {
    public static void main(String[] args) {
        System.out.println("PARTH PARIKH || 25DCS070");
        System.out.println("Practical 13 - Part B: Generic Repository CRUD Pattern with Reflection\n");

        Repository<Account> accountRepo = new Repository<>();
        accountRepo.saveAll(
                new Account("AC1001", "Parth Parikh", 68000),
                new Account("AC1002", "Aarav Patel", 31000),
                new Account("AC1003", "Diya Shah", 38000)
        );

        System.out.println("Accounts in Generic Repository (" + accountRepo.count() + " total):");
        for (Account a : accountRepo.findAll()) {
            System.out.println("   " + a);
        }

        System.out.println("\nQuerying findById(\"AC1001\"): " + accountRepo.findById("AC1001"));

        System.out.println("\n--- Type-Safe Reusability: Customer Repository ---");
        Repository<Customer> customerRepo = new Repository<>();
        customerRepo.save(new Customer("CUST101", "Parth Parikh"));
        customerRepo.save(new Customer("CUST102", "Aarav Patel"));
        for (Customer c : customerRepo.findAll()) {
            System.out.println("   " + c);
        }

        System.out.println("\n--- Testing Bounded Generic Method (Average Calculation) ---");
        Double[] balances = new Double[]{68000.0, 31000.0, 38000.0};
        System.out.printf("Average Account Balance: Rs. %.2f%n", Repository.average(balances));
    }
}
