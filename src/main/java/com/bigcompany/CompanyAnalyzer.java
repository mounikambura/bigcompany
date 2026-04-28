package com.bigcompany;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// This class does all the analysis on the employee data.
//   1. Which managers earn less than 120% of average employee salary
//   2. Which managers earn more than 150% of average employee salary
//   3. Which employees have a reporting line that is more than 4
public class CompanyAnalyzer {

    private static final double MIN_SAL = 1.20;

    private static final double MAX_SAL = 1.50;

    private static final int REPORTING_EMP = 4;

    // looks up any employee quickly by their id
    private Map<Integer, Employee> employeeById;

    // for each manager id, the list of employees who report directly to them
    private Map<Integer, List<Employee>> directReports;

    public CompanyAnalyzer(List<Employee> employees) {
        employeeById = new HashMap<>();
        directReports = new HashMap<>();

        //put every employee into the lookup map
        for (Employee e : employees) {
            employeeById.put(e.getId(), e);
        }

        //build the manager to direct reports map
        for (Employee e : employees) {
            if (!e.isCeo()) {
                int managerId = e.getManagerId();

                // if this manager doesn't have a list yet, create one
                if (!directReports.containsKey(managerId)) {
                    directReports.put(managerId, new ArrayList<>());
                }

                // add this employee to their manager's list
                directReports.get(managerId).add(e);
            }
        }
    }

    // find managers earning less than 120% of their team's average salary
    public List<SalaryIssue> findUnderpaidManagers() {
        List<SalaryIssue> result = new ArrayList<>();

        for (int managerId : directReports.keySet()) {
            Employee manager = employeeById.get(managerId);
            if (manager == null) continue;

            List<Employee> team = directReports.get(managerId);
            double teamAverage = calculateAverage(team);
            double minimumRequired = teamAverage * MIN_SAL;

            // use a tiny buffer (0.001) to avoid floating point rounding issues
            if (manager.getSalary() < minimumRequired - 0.001) {
                double shortfall = minimumRequired - manager.getSalary();
                result.add(new SalaryIssue(manager, shortfall));
            }
        }

        return result;
    }

    // find managers earning more than 150% of their team's average salary
    public List<SalaryIssue> findOverpaidManagers() {
        List<SalaryIssue> result = new ArrayList<>();

        for (int managerId : directReports.keySet()) {
            Employee manager = employeeById.get(managerId);
            if (manager == null) continue;

            List<Employee> team = directReports.get(managerId);
            double teamAvg = calculateAverage(team);
            double maximumAllowed = teamAvg * MAX_SAL;

            if (manager.getSalary() > maximumAllowed) {
                double excess = manager.getSalary() - maximumAllowed;
                result.add(new SalaryIssue(manager, excess));
            }
        }

        return result;
    }

    // find employees who have more than 4 managers between them and the CEO
    public List<ReportingLineIssue> findReportingLineCount() {
        List<ReportingLineIssue> result = new ArrayList<>();

        for (Employee e : employeeById.values()) {
            if (e.isCeo()) continue; // skip the CEO themselves

            int managersAbove = countManagersBetweenEmployeeAndCeo(e);

            if (managersAbove > REPORTING_EMP) {
                int excess = managersAbove - REPORTING_EMP;
                result.add(new ReportingLineIssue(e, excess));
            }
        }

        return result;
    }

    // moves up chain and counts how many non-CEO managers are in between
    // example: CEO -> M1 -> M2 -> Employee  =>  returns 2
    private int countManagersBetweenEmployeeAndCeo(Employee employee) {
        int count = 0;
        Employee current = employee;

        while (!current.isCeo()) {
            Employee manager = employeeById.get(current.getManagerId());

            if (manager == null) {
                break;
            }

            // we only count managers that are not the CEO themselves
            if (!manager.isCeo()) {
                count++;
            }

            current = manager;
        }

        return count;
    }

    // calculates average salary of list of employees
    private double calculateAverage(List<Employee> employees) {
        double total = 0;
        for (Employee e : employees) {
            total += e.getSalary();
        }
        return total / employees.size();
    }

    // holds salary issue for manager, who and how far off they are
    public static class SalaryIssue {
        public Employee manager;
        public double difference;

        public SalaryIssue(Employee manager, double difference) {
            this.manager = manager;
            this.difference = difference;
        }
    }

    // holds a reporting line issue, who and how many levels too deep
    public static class ReportingLineIssue {
        public Employee employee;
        public int excess;

        public ReportingLineIssue(Employee employee, int excess) {
            this.employee = employee;
            this.excess = excess;
        }
    }
}