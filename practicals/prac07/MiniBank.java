import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

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

class Account {
    @Id
    private final String accountNumber;

    @MaxLength(15)
    private final String holderName;

    @Positive
    private final double balance;

    public Account(String accountNumber, String holderName, double balance) {
        this.accountNumber = accountNumber;
        this.holderName = holderName;
        this.balance = balance;
    }

    public String getAccountNumber() { return accountNumber; }
    public String getHolderName() { return holderName; }
    public double getBalance() { return balance; }
}

class AnnotationValidator {
    public static String[] validate(Object obj) {
        List<String> errors = new ArrayList<>();
        Class<?> clazz = obj.getClass();
        for (Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true);
            try {
                Object val = field.get(obj);
                if (field.isAnnotationPresent(Positive.class)) {
                    Positive pos = field.getAnnotation(Positive.class);
                    if (val instanceof Number num && num.doubleValue() <= 0) {
                        errors.add(field.getName() + " " + pos.message() + " (found: " + num + ")");
                    }
                }
                if (field.isAnnotationPresent(MaxLength.class)) {
                    MaxLength max = field.getAnnotation(MaxLength.class);
                    if (val instanceof String s && s.length() > max.value()) {
                        errors.add(field.getName() + " length exceeds " + max.value() + " (found: " + s.length() + ")");
                    }
                }
            } catch (IllegalAccessException e) {
                errors.add("Access error: " + e.getMessage());
            }
        }
        return errors.toArray(new String[0]);
    }
}

public class MiniBank {
    public static void main(String[] args) {
        System.out.println("PARTH PARIKH || 25DCS070");
        System.out.println("Practical 07 - Part B: Custom Annotations & Reflection Engine\n");

        System.out.println("--- Validating Compliant Account ---");
        Account validAcc = new Account("AC1001", "Parth Parikh", 68000.0);
        String[] validErrors = AnnotationValidator.validate(validAcc);
        System.out.println("Valid account errors count: " + validErrors.length);

        System.out.println("\n--- Validating Invariant-Breaching Account ---");
        Account invalidAcc = new Account("AC1002", "Parth Parikh - Very Long Holder Name Exceeding Limit", -100.0);
        String[] errors = AnnotationValidator.validate(invalidAcc);
        System.out.println("Detected validation errors via Reflection:");
        for (String err : errors) {
            System.out.println("  [VALIDATION ERROR] " + err);
        }
    }
}
