# MiniBank: Technical Viva Voce & Architectural Defense Guide

**Student**: Parth Parikh (25DCS070)  
**Course**: CEUC203: Object Oriented Programming  
**Institution**: DEPSTAR, Charusat University  

---

## Capstone Architectural Defense Q&A

### Q1: How does MiniBank prevent deadlocks during concurrent inter-account transfers?
**Answer**:  
MiniBank enforces **deterministic lock ordering**: whenever two accounts (`from` and `to`) participate in a transfer, the system sorts them lexicographically by their unique account numbers:
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
Regardless of whether thread A transfers `AC1001 -> AC1002` and thread B transfers `AC1002 -> AC1001`, both threads will ALWAYS acquire the monitor for `AC1001` first, and only then attempt to acquire `AC1002`. This strictly eliminates Dijkstra's circular wait condition, making deadlock mathematically impossible.

---

### Q2: Why must equals() and hashCode() be overridden together in domain entities?
**Answer**:  
Java enforces a strict Object contract: equal objects (`a.equals(b) == true`) **MUST** produce identical integer hash codes (`a.hashCode() == b.hashCode()`). If `equals()` is overridden without `hashCode()`, hash-based data structures (such as `HashMap`, `HashSet`, and `ConcurrentHashMap`) will hash identical logical objects into different internal hash buckets. This causes lookups, duplicate detection, and entity deletions to fail silently, corrupting banking integrity.

---

### Q3: Why was deep-copy cloning implemented for Customer instead of shallow copying?
**Answer**:  
A shallow copy duplicates primitive fields but simply copies object references for mutable member objects. If `Customer` underwent a shallow copy, both the original and the cloned customer would hold references to the exact same `Address` instance in JVM heap memory. Mutating the cloned customer's mailing address would inadvertently alter the original customer's address. Deep copying explicitly instantiates a new `Address` object with copied fields, guaranteeing complete reference isolation.

---

### Q4: What is the role of custom annotations and Java Reflection in MiniBank?
**Answer**:  
MiniBank defines declarative metadata annotations (`@Id`, `@Positive`, `@MaxLength`) with `RetentionPolicy.RUNTIME`. The `AnnotationValidator` reflection engine dynamically inspects object fields at runtime prior to repository persistence:
- `@Id`: Marks the primary key field used for indexing by `Repository<T>`.
- `@Positive`: Guarantees monetary fields (such as account balance) are strictly greater than zero.
- `@MaxLength`: Enforces string length boundaries on entity attributes.
This declarative paradigm decouples validation rules from entity business logic, mirroring enterprise frameworks like Spring Data and Hibernate JPA.

---

### Q5: Why use ExecutorService with a fixed thread pool rather than spawning raw Thread instances?
**Answer**:  
Creating raw `Thread` instances incurs substantial operating system overhead for kernel thread allocation and thread stack memory (typically 1MB per thread). Spawning uncontrolled threads under high transaction volume leads to thread exhaustion, CPU thrashing, and `OutOfMemoryError`. An `ExecutorService` with a fixed thread pool (4 worker threads in MiniBank) reuses existing threads, bounds system concurrency, queues surplus tasks cleanly, and optimizes CPU core utilization.

---

### Q6: How does MiniBank ensure transaction durability across system crashes or restarts?
**Answer**:  
MiniBank implements a robust **two-tier persistence architecture**:
1. **Binary State Serialization**: Using `ObjectOutputStream`, the entire serialized object graph of `Repository<Account>` is written to `data/minibank_system.dat`. On application startup, `ObjectInputStream` rehydrates the in-memory state.
2. **Append-Only Audit Trail**: Using Java NIO (`Files.writeString` with `StandardOpenOption.CREATE` and `StandardOpenOption.APPEND`), every financial mutation (account opening, deposit, withdrawal, transfer) is atomically appended to `logs/transactions.log` with ISO-8601 timestamps for non-repudiation and compliance.

---

## Core OOP Concept Reference Table

| Concept | MiniBank Implementation | Practical Reference |
| :--- | :--- | :--- |
| **Encapsulation** | `private` balance field modified strictly through `deposit()` & `withdraw()` | Practical 2 |
| **Inheritance** | `SavingsAccount`, `CurrentAccount`, `FixedDepositAccount` extend abstract `Account` | Practical 5 |
| **Polymorphism** | Dynamic method dispatch on `withdraw()` and `calculateInterest()` | Practical 5 & 6 |
| **Abstraction** | `Transactable` and `InterestBearing` interfaces define banking capabilities | Practical 6 |
| **Generics** | `Repository<T>` and bounded type `<N extends Number> double average(N[] values)` | Practical 13 |
| **Multithreading** | `synchronized` methods, `ExecutorService` thread pool, monitor locking | Practical 9 & 10 |
