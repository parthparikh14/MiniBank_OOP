import java.util.regex.Pattern;

enum TransactionType {
    DEPOSIT, WITHDRAW, TRANSFER
}

record Command(TransactionType type, String accountNumber, long amount) {}

class Validator {
    private static final Pattern MOBILE_PATTERN = Pattern.compile("^[6-9][0-9]{9}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    private static final Pattern PAN_PATTERN = Pattern.compile("^[A-Z]{5}[0-9]{4}[A-Z]{1}$");
    private static final Pattern IFSC_PATTERN = Pattern.compile("^[A-Z]{4}0[A-Z0-9]{6}$");

    public static boolean isValidMobile(String mobile) {
        return mobile != null && MOBILE_PATTERN.matcher(mobile).matches();
    }

    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    public static boolean isValidPan(String pan) {
        return pan != null && PAN_PATTERN.matcher(pan).matches();
    }

    public static boolean isValidIfsc(String ifsc) {
        return ifsc != null && IFSC_PATTERN.matcher(ifsc).matches();
    }
}

class CommandParser {
    public static Command parse(String line) {
        if (line == null || line.trim().isEmpty()) {
            throw new IllegalArgumentException("Empty command string.");
        }
        String[] parts = line.trim().split("\\s+");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Expected exactly 3 parts: <TYPE> <ACC_NO> <AMOUNT>");
        }
        TransactionType type = TransactionType.valueOf(parts[0].toUpperCase());
        String accNo = parts[1];
        long amount = Long.parseLong(parts[2]);
        return new Command(type, accNo, amount);
    }
}

class Account {
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
}

class StatementFormatter {
    public static String buildStatement(Account account) {
        StringBuilder sb = new StringBuilder();
        sb.append("=========================================\n");
        sb.append("         MINIBANK OFFICIAL STATEMENT      \n");
        sb.append("=========================================\n");
        sb.append("Account Holder : ").append(account.getOwnerName()).append("\n");
        
        String acc = account.getAccountNumber();
        String maskedAcc = (acc.length() > 4) ? "AC****" + acc.substring(acc.length() - 2) : acc;
        sb.append("Account Number : ").append(maskedAcc).append("\n");
        sb.append("Current Balance: Rs. ").append(account.getBalance()).append("\n");
        sb.append("Status         : Active | KYC Verified\n");
        sb.append("=========================================\n");
        return sb.toString();
    }
}

public class MiniBank {
    public static void main(String[] args) {
        System.out.println("PARTH PARIKH || 25DCS070");
        System.out.println("Practical 04 - Part B: Regex & Masked Statement\n");

        System.out.println("--- Testing Regular Expression Validators ---");
        System.out.println("isValidMobile(\"9876543210\"): " + Validator.isValidMobile("9876543210"));
        System.out.println("isValidMobile(\"12345\"):      " + Validator.isValidMobile("12345"));
        System.out.println("isValidEmail(\"parth@charusat.ac.in\"): " + Validator.isValidEmail("parth@charusat.ac.in"));
        System.out.println("isValidEmail(\"invalid_email\"):         " + Validator.isValidEmail("invalid_email"));
        System.out.println("isValidPan(\"ABCDE1234F\"):  " + Validator.isValidPan("ABCDE1234F"));
        System.out.println("isValidIfsc(\"CHAR0002031\"): " + Validator.isValidIfsc("CHAR0002031"));

        System.out.println("\n--- Testing Command Parser ---");
        String inputCmd = "DEPOSIT AC0001 500";
        Command cmd = CommandParser.parse(inputCmd);
        System.out.println("Input String: " + inputCmd);
        System.out.println("Parsed Command: Type=" + cmd.type() + ", Account=" + cmd.accountNumber() + ", Amount=" + cmd.amount());

        System.out.println("\n--- Testing StatementFormatter (StringBuilder) ---");
        Account acc = new Account("AC0001", "Parth Parikh", 75000);
        System.out.println(StatementFormatter.buildStatement(acc));
    }
}
