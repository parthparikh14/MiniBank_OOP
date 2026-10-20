# MiniBank - OOP Lab Project (Practicals 1 to 14)
### CEUC203: Object Oriented Programming | Semester 3 (Odd 2026-27)
**DEVANG PATEL INSTITUTE OF ADVANCE TECHNOLOGY AND RESEARCH (DEPSTAR)**  
**Charotar University of Science and Technology (CHARUSAT)**  

- **Student Name**: Parth Parikh  
- **Enrollment ID**: 25DCS070  
- **Class / Division**: B.Tech CSE | Semester 3 | Batch C  
- **Subject**: Object Oriented Programming (CEUC203)  
- **Date**: October 20, 2026  

---

## 1. Project Overview
MiniBank is a console-based banking application created in Java for the **CEUC203: Object Oriented Programming** course. This capstone project combines all the concepts practiced throughout the semester from **Practical 1 to Practical 14** into one complete working banking project.

### Core Concepts Covered:
- **Practical 01**: Records (`BankInfo`), Enums (`MenuOption`), Switch expressions, and Scanner input menu.
- **Practical 02**: Encapsulation, private fields, static ID generator (`AC1001`), and `deposit`/`withdraw` logic.
- **Practical 03**: Overriding `toString()`, `equals()`, and `hashCode()`; static nested `Address` class; deep copy with `clone()`.
- **Practical 04**: Regular expressions for phone, account number, and IFSC validation; `StringBuilder` for masked statements.
- **Practical 05**: Polymorphic account hierarchy with abstract `Account`, `SavingsAccount`, `CurrentAccount`, and `FixedDepositAccount`.
- **Practical 06**: Interfaces (`Transactable`, `InterestBearing`), default methods, and lambda expressions (`WithdrawRule`).
- **Practical 07**: Custom annotations (`@Id`, `@Positive`, `@MaxLength`) and validation using Java Reflection.
- **Practical 08**: Custom checked exceptions (`BankException`, `InsufficientFundsException`), and `try-with-resources` (`BankSession`).
- **Practical 09**: Multithreading with `Runnable`, `Thread.join()`, and `synchronized` balance updates.
- **Practical 10**: Thread pool (`ExecutorService`), batch transactions, and deadlock-free transfers using lock ordering.
- **Practical 11**: File handling, Object Serialization (`data/minibank_system.dat`), and NIO transaction logging (`logs/transactions.log`).
- **Practical 12**: Java Collections (`HashMap`, `ArrayList`), `Comparable` / `Comparator` sorting, and Streams API.
- **Practical 13**: Generic `Repository<T>` pattern with reflection-based `@Id` discovery and bounded type methods.
- **Practical 14**: Final `BankService` integration, automated verification test runner, and interactive menu.

---

## 2. Practical Roadmap & Git Milestones

| Practical | Git Tag | Topic | Key Concepts |
| :---: | :---: | :--- | :--- |
| **01** | `v1.0` | Console Menu Shell | Java Record, MenuOption enum, switch expression, Scanner |
| **02** | `v2.0` | Encapsulation & IDs | Private fields, getters, static counter, final accountNumber |
| **03** | `v3.0` | Object Methods & Cloning | equals, hashCode, toString, static nested Address, deep clone |
| **04** | `v4.0` | Regex & Masked Statements | Regex pattern matching, StringBuilder, PII data masking |
| **05** | `v5.0` | Inheritance & Polymorphism | Abstract Account, Savings, Current (overdraft), Fixed Deposit |
| **06** | `v6.0` | Interfaces & Lambdas | Transactable, InterestBearing, WithdrawRule lambda |
| **07** | `v7.0` | Annotations & Reflection | Custom annotations (@Id, @Positive), Java Reflection validator |
| **08** | `v8.0` | Custom Exceptions | BankException, InsufficientFundsException, AutoCloseable session |
| **09** | `v9.0` | Multithreading & Monitors | Runnable, thread synchronization, race condition fix |
| **10** | `v10.0` | Thread Pool & Concurrency | ExecutorService, batch processing, deadlock prevention |
| **11** | `v11.0` | Serialization & NIO Logs | ObjectOutputStream/InputStream, NIO Files audit log |
| **12** | `v12.0` | Collections & Streams | HashMap, ArrayList, Comparable sorting, Streams API |
| **13** | `v13.0` | Generic Repository | Generic class Repository<T>, dynamic ID extraction |
| **14** | `v14.0` | Capstone Integration | BankService, test suite runner, interactive menu |

---

## 3. Project Structure

```
MiniBank/
├── data/
│   └── minibank_system.dat              # Saved account data (Java Serialization)
├── docs/
│   ├── ARCHITECTURE.md                  # Project architecture notes
│   ├── PRACTICAL_01_TO_14_ROADMAP.md   # Practical roadmap & syllabus
│   └── VIVA_VOCE_GUIDE.md               # Viva preparation questions & answers
├── logs/
│   └── transactions.log                 # Transaction log file (Java NIO)
├── practicals/                          # Standalone code for each practical (Prac 01 to 14)
│   ├── prac01/MiniBank.java
│   ├── prac02/MiniBank.java
│   ├── prac03/MiniBank.java
│   └── ...
├── src/
│   └── MiniBank.java                    # Combined final project code
└── README.md                            # Project documentation
```

---

## 4. How to Compile and Run

### Step 1: Compile
```bash
javac -d bin src/MiniBank.java
```

### Step 2: Run
```bash
# Run the full project (default test walkthrough):
java -cp bin MiniBank

# Run the automated test suite:
java -cp bin MiniBank --run-suite

# Run the multithreading test:
java -cp bin MiniBank --concurrency-stress

# Print masked statement for an account:
java -cp bin MiniBank --print-statement AC1001

# Test exception handling:
java -cp bin MiniBank --test-exceptions

# Open the interactive console menu:
java -cp bin MiniBank --interactive
```

---

## 5. Author Information
- **Name**: Parth Parikh
- **Enrollment No**: 25DCS070
- **College**: DEPSTAR, CHARUSAT University
- **Branch / Sem**: B.Tech CSE, Semester 3
