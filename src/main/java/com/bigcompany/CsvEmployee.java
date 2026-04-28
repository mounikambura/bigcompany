package com.bigcompany;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

// This class reads the CSV file and turns each row into an Employee object.
// Expected format: Id,firstName,lastName,salary,managerId
public class CsvEmployee {

    public List<Employee> parse(String filePath) throws IOException {
        List<Employee> employees = new ArrayList<>();

        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        String line = reader.readLine();

        if (line == null) {
            reader.close();
            return employees; // file was empty
        }

        int lineNumber = 1;

        while ((line = reader.readLine()) != null) {
            lineNumber++;
            line = line.trim();

            if (line.isEmpty()) {
                continue; // skip any blank lines
            }

            // split the line into parts by comma
            // we use -1 for trailing empty values (like the CEO's blank managerId)
            String[] parts = line.split(",", -1);

            if (parts.length < 4) {
                reader.close();
                throw new IllegalArgumentException("Bad data at line " + lineNumber + ": " + line);
            }

            int id           = Integer.parseInt(parts[0].trim());
            String firstName = parts[1].trim();
            String lastName  = parts[2].trim();
            double salary    = Double.parseDouble(parts[3].trim());

            // if the 5th column exists and isn't blank, it's the manager's id
            // if it's blank, this person is the CEO and managerId stays null
            Integer managerId = null;
            if (parts.length > 4 && !parts[4].trim().isEmpty()) {
                managerId = Integer.parseInt(parts[4].trim());
            }

            employees.add(new Employee(id, firstName, lastName, salary, managerId));
        }

        reader.close();
        return employees;
    }
}