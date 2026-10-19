# MiniBank: Practicals 01 to 14 Evolutionary Roadmap
**Course**: CEUC203 - Object Oriented Programming  
**Student**: Parth Parikh (25DCS070)  
**Institution**: DEPSTAR, CHARUSAT University  

---

## Roadmap Overview

| Practical | Git Tag | Milestone Focus | Core OOP & Modern Java Concepts |
| :--- | :--- | :--- | :--- |
| **01** | `v1.0` | Console Menu Shell & Records | Java Records (`BankInfo`), typed `MenuOption` enum, arrow `switch` syntax, Scanner validation |
| **02** | `v2.0` | Account Model & Invariant Encapsulation | Private fields, static sequential counter (`AC1001`), immutable final `accountNumber`, balance encapsulation |
| **03** | `v3.0` | Object Contract & Deep-Copy Cloning | Overriding `equals()`, `hashCode()`, `toString()`; static nested `Address` class; deep copy via `Cloneable` |
| **04** | `v4.0` | Regex Sanitization & Masked Statements | Precompiled `Pattern` validation (`^AC[0-9]{4}$`, mobile, IFSC), `StringBuilder` statement assembly, PII masking |
| **05** | `v5.0` | Polymorphic Account Hierarchy | Abstract `Account`, concrete `SavingsAccount` (min balance), `CurrentAccount` (overdraft), `FixedDepositAccount` (lock) |
| **06** | `v6.0` | Interfaces & Pluggable Strategies | `Transactable`, `InterestBearing` with default methods, `@FunctionalInterface WithdrawRule`, `Premium` marker |
| **07** | `v7.0` | Annotations & Reflection Engine | Custom `@Id`, `@Positive`, `@MaxLength` runtime annotations; dynamic `AnnotationValidator` via Java Reflection |
| **08** | `v8.0` | Custom Checked Exceptions & Session | `BankException` base class, `InsufficientFundsException` with shortfall, `BankSession` with `AutoCloseable` |
| **09** | `v9.0` | Multithreaded Synchronization & Monitors | `AccountWorker` Runnable, race condition demonstration, intrinsic monitor locking via `synchronized` |
| **10** | `v10.0` | Concurrent Batch Engine & Deadlock Prevention | `ExecutorService` fixed thread pool, deterministic lexicographical lock ordering (`first/second`) |
| **11** | `v11.0` | Binary Serialization & NIO Audit Trail | `ObjectOutputStream` binary serialization (`data/minibank_system.dat`), append-only NIO audit logging (`logs/transactions.log`) |
| **12** | `v12.0` | Collections Framework & Stream Analytics | `HashMap<String, Account>`, `ConcurrentHashMap`, sorting with `Comparator` & `Comparable`, Streams API reserve capital |
| **13** | `v13.0` | Generic Repository CRUD Pattern | `Repository<T>` with dynamic `@Id` discovery via reflection, `@SafeVarargs saveAll`, bounded type method `<N extends Number>` |
| **14** | `v14.0` | Capstone Integrated Production Delivery | `BankService` domain facade, CLI flags runner, automated test suite, interactive console menu |

---

## Detailed Practical Summaries & Viva Questions

### Practical 01: Console Menu Shell & Java Records
- **Key Concepts**: Java 17+ `record` constructs, typed enums, pattern switch expressions, Scanner loop.
- **Viva Q1**: *What is the role of the JVM, and what file does javac produce?*
  - **Answer**: `javac` compiles Java source code (`.java`) into platform-independent bytecode (`.class`). The JVM (Java Virtual Machine) executes this bytecode via JIT (Just-In-Time) compilation and interpretation, managing memory and garbage collection.
- **Viva Q2**: *Why use a record for BankInfo and enum for MenuOption?*
  - **Answer**: `record` creates immutable transparent data carriers with auto-generated constructor, getters, `equals()`, `hashCode()`, and `toString()`. `enum` ensures compile-time type safety for discrete menu options over magic integers.

### Practical 02: Account Model & Invariant Encapsulation
- **Key Concepts**: Data hiding, private fields with public getters, static ID generator, constructor chaining via `this()`.
- **Viva Q1**: *What is encapsulation and how do private fields enforce it?*
  - **Answer**: Encapsulation binds data and the methods operating on that data together while restricting direct access to object internals. Private fields prevent uncontrolled mutations from outside, ensuring modifications only occur via validated business logic like `deposit()` and `withdraw()`.
- **Viva Q2**: *Why is accountNumber declared final?*
  - **Answer**: An account number represents an entity's immutable identity. Declaring it `final` prevents reassignment after constructor initialization.

### Practical 03: Object Contract & Deep-Copy Cloning
- **Key Concepts**: `Object` base methods, symmetric/transitive `equals()` contract, hashcode consistency, static nested classes, `Cloneable`.
- **Viva Q1**: *Why must equals() and hashCode() be overridden together?*
  - **Answer**: If two objects are equal according to `equals()`, they MUST produce the exact same integer from `hashCode()`. Failing this breaks hash-based collections (`HashMap`, `HashSet`), causing lookups and deletions to fail.
- **Viva Q2**: *Why is deep-copy cloning needed for Customer?*
  - **Answer**: A shallow copy duplicates primitive fields but retains shared references to mutable member objects (`Address`). A deep copy instantiates a fresh `Address` clone, preventing unintended mutations across instances.

### Practical 04: Regex Sanitization & Masked Statements
- **Key Concepts**: Compiled `Pattern` and `Matcher`, input boundaries, `StringBuilder` performance, PII data masking.
- **Viva Q1**: *What is the difference between String, StringBuilder, and StringBuffer?*
  - **Answer**: `String` is immutable; every concatenation creates new heap objects. `StringBuilder` is mutable and unsynchronized, offering maximum performance for single-threaded string construction. `StringBuffer` is synchronized and thread-safe but incurs monitor overhead.

### Practical 05: Polymorphic Account Hierarchy
- **Key Concepts**: Abstract classes, virtual method table (vtable), dynamic method dispatch, runtime polymorphism, `super()` constructor delegation.
- **Viva Q1**: *What is dynamic method dispatch?*
  - **Answer**: It is the mechanism by which a call to an overridden method is resolved at runtime rather than compile-time based on the actual runtime object type.

### Practical 06: Interfaces & Pluggable Strategies
- **Key Concepts**: Interface contracts, default interface methods, `@FunctionalInterface`, lambdas, marker interfaces (`Premium`).
- **Viva Q1**: *What is the difference between an abstract class and an interface?*
  - **Answer**: An interface defines an API contract ("can-do" capability) that supports multiple inheritance. An abstract class defines an identity hierarchy ("is-a" relationship) and can maintain mutable state.

### Practical 07: Custom Annotations & Reflection Engine
- **Key Concepts**: Meta-annotations (`@Target`, `@Retention`), `Class.getDeclaredFields()`, `field.setAccessible(true)`, dynamic constraint verification.
- **Viva Q1**: *Why is RetentionPolicy.RUNTIME required?*
  - **Answer**: By default, annotations have `CLASS` retention (discarded by JVM). Setting `RUNTIME` instructs the JVM to retain annotation metadata in bytecode, enabling dynamic discovery via Java Reflection.

### Practical 08: Custom Checked Exceptions & Session
- **Key Concepts**: Checked vs unchecked exceptions, exception hierarchies, `shortfall` reporting, `try-with-resources` with `AutoCloseable`.
- **Viva Q1**: *What is the difference between checked and unchecked exceptions?*
  - **Answer**: Checked exceptions (`extends Exception`) represent recoverable domain errors enforced at compile-time by `throws` clauses. Unchecked exceptions (`extends RuntimeException`) represent programming bugs or logic flaws.

### Practical 09: Multithreaded Synchronization & Monitors
- **Key Concepts**: `Thread`, `Runnable`, race condition demonstration, intrinsic monitor locks (`synchronized`), memory visibility.
- **Viva Q1**: *What is a race condition?*
  - **Answer**: A race condition occurs when multiple threads concurrently read and write shared mutable memory without synchronization, leading to lost updates.

### Practical 10: Concurrent Batch Engine & Deadlock Prevention
- **Key Concepts**: `ExecutorService`, thread pools, Dijkstra's dining philosophers problem, circular wait elimination via deterministic lock ordering.
- **Viva Q1**: *How does MiniBank prevent deadlocks during transfers?*
  - **Answer**: Deterministic lock ordering: accounts are sorted by `accountNumber`. Both threads always acquire locks in the exact same global order (`first` then `second`), making circular wait mathematically impossible.

### Practical 11: Binary Serialization & NIO Audit Trail
- **Key Concepts**: `Serializable`, `serialVersionUID`, `ObjectOutputStream`, Java NIO `Files.writeString`, `DirectoryStream`.
- **Viva Q1**: *Why is serialVersionUID necessary?*
  - **Answer**: It verifies that the sender and receiver of a serialized object have loaded classes for that object that are compatible with respect to serialization.

### Practical 12: Collections Framework & Stream Analytics
- **Key Concepts**: `HashMap`, `ConcurrentHashMap`, `Comparable` natural order, `Comparator` custom ordering, Java Streams API aggregation (`mapToDouble`, `sum`, `groupingBy`).

### Practical 13: Generic Repository CRUD Pattern
- **Key Concepts**: Type parameters `<T>`, bounded type parameters `<N extends Number>`, type erasure, dynamic reflection discovery of `@Id`.

### Practical 14: Capstone Integrated Production Delivery
- **Key Concepts**: Facade pattern (`BankService`), layered architecture, CLI argument dispatching, automated verification harness, runnable JAR packaging.
