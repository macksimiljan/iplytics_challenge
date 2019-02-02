package pms;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.time.temporal.ChronoUnit.DAYS;

public class Project {

    // public attributes for easier access
    public String name;
    public LocalDate startDate;
    public Integer buffer;
    public LocalDate endDate;
    public List<Task> tasks;

    public Project(String name, LocalDate startDate, Integer buffer) {
        this.name = name;
        this.startDate = startDate;
        this.buffer = buffer;
        this.tasks = new ArrayList<>();
        updateEndDate();
    }

    public Project assignTask(Task task) {
        tasks.add(task);
        updateEndDate();
        return this;
    }

    public Project removeTask(Task task) {
        tasks.remove(task);
        updateEndDate();
        return this;
    }

    public int daysNeeded() {
        return (int) DAYS.between(startDate, endDate);
    }

    private void updateEndDate() {
        float delta = buffer;
        for (Task task : tasks) {
            delta += task.estimatedDays;
        }
        delta = (float) Math.ceil(delta);
        endDate = startDate.plusDays((long) delta);
    }

    @Override
    public String toString() {
        return String.format("%s, %s to %s, buffer: %d", name, startDate, endDate, buffer);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj == this) return true;
        if (!(obj instanceof Project)) return false;
        Project o = (Project) obj;
        return o.name.equals(this.name);
    }

    public static List<Project> importProjects(String projectPath) throws IOException {
        // container to deal with duplicates:
        // name is assumed to be unique, second duplicates are just ignored
        // alternative: name + start_date unique (not implemented)
        Map<String, Project> name2Project = new HashMap<>();

        Reader reader = new FileReader(projectPath);
        Iterable<CSVRecord> records = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(reader);
        for (CSVRecord record : records) {
            Project p;
            try {
                p = parseRecord(record);
                if (name2Project.keySet().contains(p.name))
                    continue;

                name2Project.put(p.name, p);
            } catch (Exception e) {
                System.out.println("Could not parse a project record:");
                System.out.println(e.getMessage());
            }
        }

        return new ArrayList<>(name2Project.values());
    }

    private static Project parseRecord(CSVRecord record) throws RuntimeException {
        String name = record.get("Name").trim();
        if (name.isEmpty())
            throw new RuntimeException("Name must not be empty");
        LocalDate startDate = parseStartDate(record);
        Integer buffer = parseBuffer(record);
        return new Project(name, startDate, buffer);
    }

    private static LocalDate parseStartDate(CSVRecord record) throws RuntimeException {
        String startDateAsStr = record.get("Start Date").trim();
        LocalDate startDate;
        // within the data, are three types of date:
        // yyyy-mm-dd, dd.mm.yyyy, yyyy
        // the latter will set to yyyy-01-01 to be strict
        if (startDateAsStr.matches("^\\d{4}-\\d{2}-\\d{2}$"))
            startDate = LocalDate.parse(startDateAsStr);
        else if (startDateAsStr.matches("^\\d{2}[.]\\d{2}[.]\\d{4}$"))
            startDate = LocalDate.parse(startDateAsStr, DateTimeFormatter.ofPattern("dd.MM.yyyy"));
        else if (startDateAsStr.matches("^\\d{4}$"))
            startDate = LocalDate.parse(startDateAsStr+"-01-01");
        else
            throw new RuntimeException("Unknown date format");

        return startDate;
    }

    private static Integer parseBuffer(CSVRecord record) {
        String bufferAsStr = record.get("Buffer").trim();
        int buffer;
        try {
            buffer = Integer.parseInt(bufferAsStr);
        } catch (NumberFormatException e) {
            // buffer is assumed to be optional
            // when not present or ill-formed, it is set to 0
            // this would also require the possibility to update this attribute
            buffer = 0;
        }
        return buffer;
    }

}
