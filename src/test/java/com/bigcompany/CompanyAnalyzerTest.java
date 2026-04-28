package com.bigcompany;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

public class CompanyAnalyzerTest {
    private static Employee emp(int id, String first, String last, double salary, Integer managerId) {
        return new Employee(id, first, last, salary, managerId);
    }
//Salary tests
    @Test
    public void managerEarningExactly20PercentMoreIsNotFlagged() {
        // avg subordinate salary = 50,000 → minimum = 60,000
        // manager earns exactly 60,000 → should NOT be flagged(inclusive boundary)
        List<Employee> employees = Arrays.asList(
                emp(1, "Alice", "CEO",     80_000, null),
                emp(2, "Bob",   "Manager", 60_000, 1),
                emp(3, "Carol", "Worker",  50_000, 2)
        );
        CompanyAnalyzer analyzer = new CompanyAnalyzer(employees);
        // Bob manages Carol (avg=50,000, min=60,000). Bob earns exactly 60,000 → OK
        assertTrue(analyzer.findUnderpaidManagers().isEmpty());
    }

    @Test
    public void managerEarningLessThan20PercentMoreIsFlaggedWithCorrectAmount() {
        // avg subordinate salary = 50,000 → minimum = 60,000; manager earns 55,000 → deficit 5,000
        List<Employee> employees = Arrays.asList(
                emp(1, "Alice", "CEO",     80_000, null),
                emp(2, "Bob",   "Manager", 55_000, 1),
                emp(3, "Carol", "Worker",  50_000, 2)
        );
        CompanyAnalyzer analyzer = new CompanyAnalyzer(employees);
        List<CompanyAnalyzer.SalaryIssue> issues = analyzer.findUnderpaidManagers();
        assertEquals(1, issues.size());
        assertEquals(2, issues.get(0).manager.getId());
        assertEquals(5_000.0, issues.get(0).difference, 0.01);
    }

    @Test
    public void managerEarningExactly50PercentMoreIsNotFlagged() {
        // avg subordinate salary = 40,000 → maximum = 60,000; manager earns 60,000 → OK
        List<Employee> employees = Arrays.asList(
                emp(1, "Alice", "CEO",     90_000, null),
                emp(2, "Bob",   "Manager", 60_000, 1),
                emp(3, "Carol", "Worker",  40_000, 2)
        );
        CompanyAnalyzer analyzer = new CompanyAnalyzer(employees);
        assertTrue(analyzer.findOverpaidManagers().isEmpty());
    }

    @Test
    public void managerEarningMoreThan50PercentIsFlaggedWithCorrectAmount() {
        // avg subordinate salary = 40,000 → maximum = 60,000; manager earns 70,000 → excess 10,000
        List<Employee> employees = Arrays.asList(
                emp(1, "Alice", "CEO",     90_000, null),
                emp(2, "Bob",   "Manager", 70_000, 1),
                emp(3, "Carol", "Worker",  40_000, 2)
        );
        CompanyAnalyzer analyzer = new CompanyAnalyzer(employees);
        List<CompanyAnalyzer.SalaryIssue> issues = analyzer.findOverpaidManagers();
        assertEquals(1, issues.size());
        assertEquals(2, issues.get(0).manager.getId());
        assertEquals(10_000.0, issues.get(0).difference, 0.01);
    }

    @Test
    public void averageIsComputedAcrossAllDirectSubordinates() {
        // manager has two direct reports: 40,000 and 60,000 → avg = 50,000 → min = 60,000
        // manager earns 59,000 → underpaid by 1,000
        List<Employee> employees = Arrays.asList(
                emp(1, "CEO",  "Boss",    100_000, null),
                emp(2, "Mgr",  "Middle",   59_000, 1),
                emp(3, "Sub1", "Alpha",    40_000, 2),
                emp(4, "Sub2", "Beta",     60_000, 2)
        );
        CompanyAnalyzer analyzer = new CompanyAnalyzer(employees);
        List<CompanyAnalyzer.SalaryIssue> issues = analyzer.findUnderpaidManagers();
        assertEquals(1, issues.size());
        assertEquals(1_000.0, issues.get(0).difference, 0.01);
    }
//Reporting tests
    @Test
    public void employeeWith4ManagersBetweenAndCeoIsNotFlagged() {
        // chain: CEO(1) → M1(2) → M2(3) → M3(4) → M4(5) → Employee(6)
        // managers between Employee and CEO=4 → exact limit
        List<Employee> employees = Arrays.asList(
                emp(1, "CEO", "Top",   100_000, null),
                emp(2, "M1",  "Mgr1",   80_000, 1),
                emp(3, "M2",  "Mgr2",   70_000, 2),
                emp(4, "M3",  "Mgr3",   60_000, 3),
                emp(5, "M4",  "Mgr4",   50_000, 4),
                emp(6, "Emp", "Worker", 40_000, 5)
        );
        CompanyAnalyzer analyzer = new CompanyAnalyzer(employees);
        assertTrue("Employee with exactly 4 managers above should not be flagged",
                analyzer.findReportingLineCount().isEmpty());
    }

    @Test
    public void employeeWith5ManagersBetweenAndCeoIsFlagged() {
        // chain: CEO(1) → M1(2) → M2(3) → M3(4) → M4(5) → M5(6) → Employee(7)
        // managers BETWEEN Employee and CEO= 5 → excess by 1
        List<Employee> employees = Arrays.asList(
                emp(1, "CEO", "Top",   100_000, null),
                emp(2, "M1",  "Mgr1",   80_000, 1),
                emp(3, "M2",  "Mgr2",   70_000, 2),
                emp(4, "M3",  "Mgr3",   60_000, 3),
                emp(5, "M4",  "Mgr4",   50_000, 4),
                emp(6, "M5",  "Mgr5",   45_000, 5),
                emp(7, "Emp", "Worker", 40_000, 6)
        );
        CompanyAnalyzer analyzer = new CompanyAnalyzer(employees);
        List<CompanyAnalyzer.ReportingLineIssue> issues = analyzer.findReportingLineCount();
        assertEquals(1, issues.size());
        assertEquals(7, issues.get(0).employee.getId());
        assertEquals(1, issues.get(0).excess);
    }

    @Test
    public void ceoIsNeverFlaggedForReportingLine() {
        List<Employee> employees = Collections.singletonList(
                emp(1, "Solo", "CEO", 100_000, null)
        );
        CompanyAnalyzer analyzer = new CompanyAnalyzer(employees);
        assertTrue(analyzer.findReportingLineCount().isEmpty());
    }

    @Test
    public void sampleDataFromProblemStatement() {
        // 123,Joe,Doe,60000,         ← CEO
        // 124,Martin,Chekov,45000,123
        // 125,Bob,Ronstad,47000,123
        // 300,Alice,Hasacat,50000,124
        // 305,Brett,Hardleaf,34000,300
        List<Employee> employees = Arrays.asList(
                emp(123, "Joe",    "Doe",       60_000, null),
                emp(124, "Martin", "Chekov",    45_000, 123),
                emp(125, "Bob",    "Ronstad",   47_000, 123),
                emp(300, "Alice",  "Hasacat",   50_000, 124),
                emp(305, "Brett",  "Hardleaf",  34_000, 300)
        );

        CompanyAnalyzer analyzer = new CompanyAnalyzer(employees);

        // Joe (CEO) manages Martin and Bob; avg = (45000+47000)/2 = 46000; min = 55200, max = 69000
        // Joe earns 60000 → within range → no salary issues for Joe as manager
        // Martin manages Alice; avg = 50000; min = 60000, max = 75000; Martin earns 45000 → underpaid
        // Alice manages Brett; avg = 34000; min = 40800, max = 51000; Alice earns 50000 → within range

        List<CompanyAnalyzer.SalaryIssue> underpaid = analyzer.findUnderpaidManagers();
        assertEquals(1, underpaid.size());
        assertEquals(124, underpaid.get(0).manager.getId()); // Martin Chekov
        assertEquals(15_000.0, underpaid.get(0).difference, 0.01);

        // No overpaid managers
        assertTrue(analyzer.findOverpaidManagers().isEmpty());

        // Reporting lines: deepest is Brett → depth = 3 (CEO→Martin→Alice→Brett) → 3 managers → OK
        assertTrue(analyzer.findReportingLineCount().isEmpty());
    }
}
