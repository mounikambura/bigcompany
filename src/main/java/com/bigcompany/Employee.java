package com.bigcompany;

// This class represents one employee from the CSV file.
// It just holds their data - id, name, salary, and who their manager is.
public class Employee {

    private int id;
    private String firstName;
    private String lastName;
    private double salary;
    private Integer managerId; // this is null for the CEO since they have no manager

    public Employee(int id, String firstName, String lastName, double salary, Integer managerId) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.salary = salary;
        this.managerId = managerId;
    }

    public int getId() {
        return id;
    }

    public double getSalary() {
        return salary;
    }

    public Integer getManagerId() {
        return managerId;
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public boolean isCeo() {
        return managerId == null;
    }
}