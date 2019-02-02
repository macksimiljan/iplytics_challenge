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

public class Task {

    public String name;
    public String description;
    // within the specification, a task is estimated in days; but within the data, a task is estimated in days
    // following the schema definition, I use days
    // however, I use float here and round to whole days when defining the endDate of a project
    public float estimatedDays;

    public Task(String name, String description, float estimatedDays) {
        this.name = name;
        this.description = description;
        this.estimatedDays = estimatedDays;
    }

    @Override
    public String toString() {
        return String.format("%s (%f days): %s", name, estimatedDays, description);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj == this) return true;
        if (!(obj instanceof Project)) return false;
        Project o = (Project) obj;
        return o.name.equals(this.name);
    }

    public static List<Task> importTasks(String taskPath) throws IOException {
        // container to deal with duplicates
        // name is assumed to be unique
        Map<String, Task> name2Task = new HashMap<>();

        Reader reader = new FileReader(taskPath);
        Iterable<CSVRecord> records = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(reader);
        for (CSVRecord record : records) {
            Task t;
            try {
                t = parseRecord(record);
                if (name2Task.keySet().contains(t.name))
                    continue;

                name2Task.put(t.name, t);
            } catch (Exception e) {
                System.out.println("Could not parse a task record:");
                System.out.println(e.getMessage());
            }
        }

        return new ArrayList<>(name2Task.values());
    }

    private static Task parseRecord(CSVRecord record) throws RuntimeException {
        String name = record.get("Name").trim();
        if (name.isEmpty())
            throw new RuntimeException("Name must not be empty");

        String description = record.get("Description").trim();
        float estimatedDays = parseEstimatedDays(record);
        return  new Task(name, description, estimatedDays);
    }

    private static float parseEstimatedDays(CSVRecord record) {
        String estimatedHoursAsStr = record.get("Estimated Hours").trim();
        int estimatedHours;
        try {
            estimatedHours = Integer.parseInt(estimatedHoursAsStr);
        } catch (NumberFormatException e) {
            // estimated hours/ days are assumed to be optional
            // when not present or ill-formed, it is set to 0
            // this would also require the possibility to update this attribute
            estimatedHours = 0;
        }
        return estimatedHours / 24.0f;
    }
}
