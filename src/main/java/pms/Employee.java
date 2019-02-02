package pms;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Employee {

    // attributes are public for easier access
    public String firstName;
    public String lastName;
    public Employee supervisor;
    public List<Project> projects;


    public Employee(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.supervisor = null;
        this.projects = new ArrayList<>(2);
    }

    public Boolean assignProject(Project project) {
        if (projects.size() >= 2)
            return false;

        projects.add(project);
        return true;
    }

    public Boolean removeProject(Project project) {
        return projects.remove(project);
    }

    @Override
    public String toString() {
        String supervisorLastName = (supervisor != null) ? supervisor.lastName : "";
        return String.format("%s %s, supervisor: %s", firstName, lastName, supervisorLastName);
    }

    public static List<Employee> importEmployees(String employeePath) throws IOException {
        // container to deal with duplicates
        Map<String, Employee> lastName2Employee = new HashMap<>();
        // container for the case that the supervisor comes after the employee
        Map<String, Employee> supervisorLastName2Employee = new HashMap<>();

        Reader reader = new FileReader(employeePath);
        Iterable<CSVRecord> records = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(reader);
        for (CSVRecord record : records) {
            String firstName = record.get("First Name").trim();
            String lastName = record.get("Last Name").trim();
            String supervisorLastName = record.get("Supervisor").trim();

            // first name is obligatory
            if (firstName.isEmpty())
                continue;
            // employee already read
            if (lastName2Employee.keySet().contains(lastName))
                continue;

            Employee e = new Employee(firstName, lastName);
            if (lastName2Employee.keySet().contains(supervisorLastName))
                e.supervisor = lastName2Employee.get(supervisorLastName);
            else
                supervisorLastName2Employee.put(supervisorLastName, e);

            lastName2Employee.put(lastName, e);
        }

        for (String supervisorLastName : supervisorLastName2Employee.keySet()) {
            // records withs non-existing supervisors are kept but without supervisor
            if (lastName2Employee.keySet().contains(supervisorLastName))
                supervisorLastName2Employee.get(supervisorLastName).supervisor = lastName2Employee.get(supervisorLastName);
        }

        return new ArrayList<>(lastName2Employee.values());
    }

}
