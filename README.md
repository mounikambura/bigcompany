# BIG COMPANY – Org Structure Analyser

A simple Java SE / Maven application that reads an employee CSV file and reports organisational issues.

## Reports produced

| Report | Rule |
|---|---|
| Managers underpaid | Earn **less than 120 %** of their direct subordinates' average salary |
| Managers overpaid | Earn **more than 150 %** of their direct subordinates' average salary |
| Reporting line too long | More than **4 managers** between employee and CEO |

## Project structure

```
src/
  main/java/com/bigcompany/
    Employee.java          – immutable data model
    CsvEmployeeParser.java – reads the CSV file
    OrgAnalyzer.java       – core business logic
    Main.java              – CLI entry point
  main/resources/
    employees.csv          – sample data from the problem statement
  test/java/com/bigcompany/
    OrgAnalyzerTest.java   – JUnit 4 unit tests
pom.xml
```

## Assumptions

- The **CEO** is identified by having an empty `managerId` column.
- Salary rules are based on **direct subordinates only** (not the whole subtree).
- "More than 4 managers between employee and CEO" means the employee's depth in the
  hierarchy is greater than 5 (CEO is depth 0; a direct report of CEO is depth 1, etc.).
  The reported `excess` is `managersAbove − 4`.

## Build & run

```bash
# Build and run all tests
mvn clean package

# Run against the sample file
java -jar target/bigcompany-1.0-SNAPSHOT.jar src/main/resources/employees.csv

# Run against your own file
java -jar target/bigcompany-1.0-SNAPSHOT.jar /path/to/your/employees.csv
```

## CSV format

```
Id,firstName,lastName,salary,managerId
123,Joe,Doe,60000,
124,Martin,Chekov,45000,123
```

- Header row is required.
- `managerId` is blank for the CEO.
- Up to 1 000 rows are supported.
