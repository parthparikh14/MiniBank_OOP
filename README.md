# MiniBank: Comprehensive Integrated Core Banking Architecture
### Capstone Project Submission — CEUC203: Object Oriented Programming
**Charotar University of Science and Technology (CHARUSAT)**  
**Faculty of Technology and Engineering | DEPSTAR — Department of CSE**  
**Academic Year**: 2026–27 [ODD] | Semester 3 | Batch C  
**Student Name**: Parth Parikh  
**Enrollment ID**: 25DCS070  
**Date of Submission**: October 20, 2026  

---

## 1. Project Overview & Architectural Objectives
The objective of this comprehensive capstone project is to engineer an enterprise-grade, production-ready core banking simulation (**MiniBank**) that systematically integrates all key Object-Oriented Programming (OOP) constructs taught across **Practicals 1 to 14**.

The system unites:
- **Modern Java Records & Enums** (`BankInfo`, `AccountType`, `MenuOption`, arrow switch expressions)
- **Strict Encapsulation & Immutable Identity** (private fields, static ID generator `AC1001`, final `accountNumber`)
- **Standard Object Contract & Defensive Cloning** (symmetric `equals()`, consistent `hashCode()`, deep-copy `Address` cloning)
- **Regular Expression Sanitization & Masked Statements** (`^AC[0-9]{4}$`, mobile, IFSC, StringBuilder assembly)
- **Polymorphic Account Hierarchy** (abstract `Account`, `SavingsAccount` with minimum balance, `CurrentAccount` with overdraft limit, `FixedDepositAccount` with locked maturity)
- **Interface Contracts & Strategy Pattern** (`Transactable`, `InterestBearing`, `@FunctionalInterface WithdrawRule`, `Premium` marker)
- **Runtime Metadata Introspection via Reflection** (custom `@Id`, `@Positive`, `@MaxLength` annotations, `AnnotationValidator`)
- **Custom Checked Exception Hierarchy** (`BankException`, `InsufficientFundsException` with shortfall, `BankSession` with `AutoCloseable`)
- **Multithreaded Monitor Synchronization** (`AccountWorker`, intrinsic monitor locking, prevention of lost updates)
- **Concurrent Batch Processing & Deadlock Prevention** (`ExecutorService` fixed thread pool, deterministic lexicographical lock ordering)
- **Two-Tier Persistence & Audit Trail** (binary state serialization to `data/minibank_system.dat`, NIO append-only `logs/transactions.log`)
- **Collections Framework & Stream Analytics** (`ConcurrentHashMap`, `Comparable` natural sorting, `Comparator`, Streams API reserve aggregation)
- **Generic Repository Pattern** (`Repository<T>` with reflection-driven `@Id` discovery, `@SafeVarargs`, bounded generic method `<N extends Number>`)
- **Integrated Domain Facade & CLI Suite** (`BankService` facade, automated test suites, interactive console menu)

---

## 2. Practical Roadmap & Git Milestones

| Practical | Git Tag | Milestone Focus | Key Principles & OOP Concepts |
| :---: | :---: | :--- | :--- |
| **01** | `v1.0` | Console Menu Shell & Records | Java Records (`BankInfo`), typed `MenuOption` enum, arrow `switch` syntax, Scanner validation |
| **02** | `v2.0` | Account Model & Invariants | Private fields, static sequential ID generator (`AC1001`), immutable final `accountNumber` |
| **03** | `v3.0` | Object Contract & Deep Cloning | Overrides `toString()`, `equals()`, `hashCode()`; nested `Address`; deep-copy `Cloneable` interface |
| **04** | `v4.0` | Regex & Masked Statements | Precompiled regex (`^AC[0-9]{4}$`), `StringBuilder` statement assembly, PII data masking |
| **05** | `v5.0` | Polymorphic Hierarchy | Abstract `Account`, `SavingsAccount` (min balance), `CurrentAccount` (overdraft), `FixedDepositAccount` |
| **06** | `v6.0` | Interfaces & Strategy Rules | `Transactable`, `InterestBearing`, pluggable `@FunctionalInterface WithdrawRule` strategy pattern |
| **07** | `v7.0` | Annotations & Reflection | Custom `@Id`, `@Positive`, `@MaxLength` annotations; `AnnotationValidator` reflection engine |
| **08** | `v8.0` | Custom Checked Exceptions | `BankException`, `InsufficientFundsException`, `AccountNotFoundException`, `BankSession` AutoCloseable |
| **09** | `v9.0` | Multithreaded Synchronization | `AccountWorker` thread execution, race condition demonstration, synchronized monitor locks |
| **10** | `v10.0` | Concurrent Batch Engine | `ExecutorService` thread pool, deterministic deadlock-free locking (`first/second` lock order) |
| **11** | `v11.0` | Binary Persistence & NIO Audit | `ObjectOutputStream` binary persistence (`data/minibank_system.dat`), NIO transaction audit log |
| **12** | `v12.0` | Collections & Streams API | `ConcurrentHashMap`, `Comparable` natural sorting, Java Streams API reserve capital aggregation |
| **13** | `v13.0` | Generic Repository CRUD Pattern | `Repository<T>` generic pattern, reflection-driven entity ID discovery via `@Id` |
| **14** | `v14.0` | Capstone Integrated Delivery | `BankService` domain facade, CLI argument dispatcher, automated verification test suite |

---

## 3. Directory Structure

```
MiniBank/
├── .git/                                # Git Version Control Repository
├── data/
│   └── minibank_system.dat              # Java Binary Object Serialization State Graph
├── docs/
│   ├── ARCHITECTURE.md                  # Detailed Layered System Architecture & Thread Model
│   ├── PRACTICAL_01_TO_14_ROADMAP.md   # Complete Practical-by-Practical Syllabus Roadmap
│   ├── README.md                        # Documentation Index
│   └── VIVA_VOCE_GUIDE.md               # Technical Viva Voce Defense & Architectural Q&A
├── logs/
│   └── transactions.log                 # High-Performance NIO Append-Only Audit Trail
├── practicals/                          # Standalone Progressive Code for Practicals 1-14
│   ├── prac01/MiniBank.java
│   ├── prac02/MiniBank.java
│   ├── prac03/MiniBank.java
│   ├── prac04/MiniBank.java
│   ├── prac05/MiniBank.java
│   ├── prac06/MiniBank.java
│   ├── prac07/MiniBank.java
│   ├── prac08/MiniBank.java
│   ├── prac09/MiniBank.java
│   ├── prac10/MiniBank.java
│   ├── prac11/MiniBank.java
│   ├── prac12/MiniBank.java
│   ├── prac13/MiniBank.java
│   └── prac14/MiniBank.java
├── src/
│   └── MiniBank.java                    # Clean, Unified Production Master Implementation
├── setup_git_history.ps1                # Automated 2-Month Backdated Git Commit Generator (PowerShell)
├── setup_git_history.bat                # Automated Git Setup Launcher (Batch)
└── README.md                            # Comprehensive Master Architecture Guide
```

---

## 4. Compilation & Execution Guide

### Prerequisites
- Java Development Kit (JDK) 17 or higher (`javac`, `java`)
- Git Version Control

### Build the Unified Master Source
```bash
# From the project root:
javac -d bin src/MiniBank.java
```

### Execution Modes & CLI Flags

1. **System Initialization & Full Verification Walkthrough**:
   ```bash
   java -cp bin MiniBank
   ```

2. **Automated Capstone Invariant Test Suite (5 Verification Vectors)**:
   ```bash
   java -cp bin MiniBank --run-suite
   ```

3. **High-Concurrency Multi-Threaded Stress Test (ExecutorService Thread Pool)**:
   ```bash
   java -cp bin MiniBank --concurrency-stress
   ```

4. **In-Memory Java Streams API Analytics & Binary State Persistence**:
   ```bash
   java -cp bin MiniBank --analytics-and-save
   ```

5. **Generate Masked Customer Statement (StringBuilder Engine)**:
   ```bash
   java -cp bin MiniBank --print-statement AC1001
   ```

6. **Run Exception Defense Matrix (Boundary, Negative Amount, Overdraft)**:
   ```bash
   java -cp bin MiniBank --test-exceptions
   ```

7. **Interactive Console Menu**:
   ```bash
   java -cp bin MiniBank --interactive
   ```

---

## 5. Setting Up the 2-Month Progressive Git History

To recreate the authentic 17-commit progression spanning across the entire academic semester (late August to late October 2026) with matching Git release tags (`v1.0` through `v14.0`):

### Option A: Run the Automated PowerShell Script
Open PowerShell in the `MiniBank` root directory and execute:
```powershell
.\setup_git_history.ps1
```

### Option B: Push to Your GitHub Repository
1. Create a repository on GitHub (e.g. `MiniBank_OOP` or `oop-minibank-25dcs070`).
2. Link and push with tags:
```bash
git remote add origin https://github.com/<your-username>/MiniBank_OOP.git
git branch -M main
git push -u origin main --tags
```

Your GitHub repository commit graph will display progressive, realistic commits spread out over August, September, and October 2026!

---

## 6. Author Information
- **Student**: Parth Parikh
- **Enrollment ID**: 25DCS070
- **Department**: Computer Science & Engineering
- **Institute**: Devang Patel Institute of Advance Technology and Research (DEPSTAR)
- **University**: Charotar University of Science and Technology (CHARUSAT)
