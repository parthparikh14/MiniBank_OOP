import java.util.Objects;

class Customer implements Cloneable {
    public static class Address {
        private String line;
        private String city;
        private String pincode;

        public Address(String line, String city, String pincode) {
            this.line = line;
            this.city = city;
            this.pincode = pincode;
        }

        public String getLine() { return line; }
        public String getCity() { return city; }
        public String getPincode() { return pincode; }
        public void setCity(String city) { this.city = city; }

        @Override
        public String toString() {
            return line + ", " + city + " (" + pincode + ")";
        }
    }

    private static long customerCounter = 100;
    private final String customerId;
    private String name;
    private String email;
    private String mobile;
    private Address address;

    public Customer(String name, String email, String mobile, Address address) {
        this.customerId = "CUST" + (++customerCounter);
        this.name = name;
        this.email = email;
        this.mobile = mobile;
        this.address = address;
    }

    public String getCustomerId() { return customerId; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getMobile() { return mobile; }
    public Address getAddress() { return address; }
    public void setAddress(Address address) { this.address = address; }

    @Override
    public Customer clone() {
        try {
            Customer cloned = (Customer) super.clone();
            if (this.address != null) {
                cloned.address = new Address(this.address.line, this.address.city, this.address.pincode);
            }
            return cloned;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError("Cloning failed", e);
        }
    }

    @Override
    public String toString() {
        return "Customer[" + customerId + " | " + name + " | " + address + "]";
    }
}

class Account {
    private final String accountNumber;
    private String ownerName;
    private long balance;

    public Account(String accountNumber, String ownerName, long balance) {
        this.accountNumber = accountNumber;
        this.ownerName = ownerName;
        this.balance = balance;
    }

    public String getAccountNumber() { return accountNumber; }
    public String getOwnerName() { return ownerName; }
    public long getBalance() { return balance; }

    @Override
    public String toString() {
        return String.format("%s | %-12s | Rs. %d", accountNumber, ownerName, balance);
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
}

public class MiniBank {
    public static void main(String[] args) {
        System.out.println("PARTH PARIKH || 25DCS070");
        System.out.println("Practical 03 - Part B: Object Contract & Deep-Copy Cloning\n");

        Customer.Address addr1 = new Customer.Address("DEPSTAR Campus", "Changa", "388421");
        Customer c1 = new Customer("Parth Parikh", "parth@charusat.edu.in", "9876543210", addr1);
        System.out.println("Original Customer: " + c1);

        Customer c1Clone = c1.clone();
        c1Clone.getAddress().setCity("Anand");
        System.out.println("Cloned Customer (Modified City): " + c1Clone);
        System.out.println("Original Customer City preserved? " + c1.getAddress().getCity().equals("Changa"));

        Account a1 = new Account("AC0001", "Parth Parikh", 5000);
        Account a2 = new Account("AC0001", "Parth Parikh Duplicate Reference", 7000);
        Account a3 = new Account("AC0002", "Aarav Patel", 3000);

        System.out.println("\nTesting toString():");
        System.out.println("a1 -> " + a1);
        System.out.println("a3 -> " + a3);

        System.out.println("\nTesting equals() & hashCode() contract:");
        System.out.println("a1.equals(a2) [same account number]: " + a1.equals(a2));
        System.out.println("a1.equals(a3) [different account number]: " + a1.equals(a3));
        System.out.println("Hashcode equality: " + (a1.hashCode() == a2.hashCode()));

        Object obj = a1;
        System.out.println("instanceof Account check: " + (obj instanceof Account));
    }
}
