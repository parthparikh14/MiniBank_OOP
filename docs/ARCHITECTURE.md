# MiniBank: System Architecture & Technical Specification

**Author**: Parth Parikh (25DCS070)  
**Affiliation**: DEPSTAR, Charusat University  

---

## 1. Architectural Overview

MiniBank is designed following a **Layered Architecture** adhering to SOLID object-oriented principles:

```
+-------------------------------------------------------+
|                PRESENTATION & CLI LAYER               |
|      (Interactive Menu, CLI Dispatcher, Runner)       |
+-------------------------------------------------------+
                           |
                           v
+-------------------------------------------------------+
|                    SERVICE LAYER                      |
|                     (BankService)                     |
|  - Deterministic Lock-Ordered Transfers               |
|  - Streams API Financial Analytics                    |
|  - Audit Logging Trigger                              |
+-------------------------------------------------------+
             |                               |
             v                               v
+------------------------+      +------------------------+
|    REPOSITORY LAYER    |      |    VALIDATION LAYER    |
|    (Generic CRUD)      |      |  (Regex & Reflection)  |
|  - Repository<T>       |      |  - Validator (Regex)   |
|  - ConcurrentHashMap   |      |  - AnnotationValidator |
+------------------------+      +------------------------+
             |
             v
+-------------------------------------------------------+
|                     DOMAIN LAYER                      |
|  - Customer, Address (Cloneable, Serializable)        |
|  - Account (Abstract, Transactable, Comparable)       |
|    * SavingsAccount (InterestBearing, Min Balance)    |
|    * CurrentAccount (Overdraft Limit)                 |
|    * FixedDepositAccount (Lock Liquidation)           |
+-------------------------------------------------------+
             |
             v
+-------------------------------------------------------+
|                   PERSISTENCE LAYER                   |
|  - Binary Serialization (data/minibank_system.dat)    |
|  - NIO Append-Only Log (logs/transactions.log)        |
+-------------------------------------------------------+
```

---

## 2. Concurrency & Deadlock Defense

### Circular Wait Prevention
When transferring funds between account $A$ and account $B$, two concurrent threads could execute simultaneously:
- Thread 1: `transfer(A, B)`
- Thread 2: `transfer(B, A)`

If Thread 1 locks $A$ and waits for $B$, while Thread 2 locks $B$ and waits for $A$, a classic circular wait deadlock occurs.

### MiniBank Solution
MiniBank orders accounts deterministically using their natural lexicographical order:
```java
Account first = from.getAccountNumber().compareTo(to.getAccountNumber()) < 0 ? from : to;
Account second = (first == from) ? to : from;
synchronized (first) {
    synchronized (second) {
        from.withdraw(amount);
        to.deposit(amount);
    }
}
```
Both threads always lock the lower account number first, ensuring no circular waiting can ever manifest.

---

## 3. Custom Annotation Reflection Engine

```
Entity Object -> AnnotationValidator.validate(obj)
                     |
                     +---> Field: @Positive -> value.doubleValue() > 0 ? Pass : Throw BankException
                     |
                     +---> Field: @MaxLength(n) -> str.length() <= n ? Pass : Throw BankException
                     |
                     +---> Field: @Id -> extracted dynamically as key for Repository<T>
```
