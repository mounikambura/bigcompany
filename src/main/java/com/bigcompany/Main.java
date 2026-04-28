package com.bigcompany;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

// This is the starting point of the program reads the CSV file, runs the analysis
public class Main {

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Please provide the path to the employees CSV file.");
            System.out.println("Example: java -jar bigcompany.jar employees.csv");
            System.exit(1);
        }

        String filePath = args[0];
        List<Employee> employees;

        try {
            employees = new CsvEmployee().parse(filePath);
        } catch (IOException e) {
            System.out.println("Could not read the file: " + e.getMessage());
            System.exit(1);
            return;
        }

        if (employees.isEmpty()) {
            System.out.println("The file had no employees in it.");
            return;
        }

        CompanyAnalyzer analyzer = new CompanyAnalyzer(employees);
        printReport(analyzer);
    }

    static void printReport(CompanyAnalyzer analyzer) {
        // we use this to format numbers with commas
        NumberFormat money = NumberFormat.getNumberInstance(Locale.US);
        money.setMinimumFractionDigits(2);
        money.setMaximumFractionDigits(2);

        //managers earning less than they should
        System.out.println("Managers who earn less than they should:");
        List<CompanyAnalyzer.SalaryIssue> underpaid = analyzer.findUnderpaidManagers();
        if (underpaid.isEmpty()) {
            System.out.println("  None found.");
        } else {
            for (CompanyAnalyzer.SalaryIssue issue : underpaid) {
                System.out.println("  " + issue.manager.getFullName()
                        + " (ID " + issue.manager.getId() + ")"
                        + " is earning $" + money.format(issue.difference)
                        + " less than the minimum they should earn.");
            }
        }

        System.out.println();

        //managers earning more than they should
        System.out.println("Managers who earn more than they should:");
        List<CompanyAnalyzer.SalaryIssue> overpaid = analyzer.findOverpaidManagers();
        if (overpaid.isEmpty()) {
            System.out.println("  None found.");
        } else {
            for (CompanyAnalyzer.SalaryIssue issue : overpaid) {
                System.out.println("  " + issue.manager.getFullName()
                        + " (ID " + issue.manager.getId() + ")"
                        + " is earning $" + money.format(issue.difference)
                        + " more than the maximum they should earn.");
            }
        }

        System.out.println();

        //employees with too many managers above them
        System.out.println("Employees with a reporting line that is too long:");
        List<CompanyAnalyzer.ReportingLineIssue> longLines = analyzer.findReportingLineCount();
        if (longLines.isEmpty()) {
            System.out.println("  None found.");
        } else {
            for (CompanyAnalyzer.ReportingLineIssue issue : longLines) {
                System.out.println("  " + issue.employee.getFullName()
                        + " (ID " + issue.employee.getId() + ")"
                        + " has " + issue.excess + " more manager(s) above them than allowed.");
            }
        }
    }
}