package pms;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class ProjectManagementSystem {

    // attributes are public for easier access
    public List<Employee> employees;
    public List<Project> projects;
    public List<Task> tasks;

    public ProjectManagementSystem(String employeePath, String projectPath, String taskPath) {
        try {
            employees = Employee.importEmployees(employeePath);
            projects = Project.importProjects(projectPath);
            tasks = Task.importTasks(taskPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ProjectManagementSystem assignTaskToProject(Task task, Project project) {
        project.assignTask(task);
        return this;
    }

    public ProjectManagementSystem assignProjectToEmployee(Project project, Employee employee) {
        boolean successful = employee.assignProject(project);
        if (!successful)
            System.out.println("pms.Employee has already two projects!");
        return this;
    }

    public ProjectManagementSystem deleteTask(Task task) {
        tasks.remove(task);
        // I have to call this method on each project
        // when there is a large number of projects it might be faster when the task knows the project
        // which is associated with the task. then only one removeTask call is necessary
        projects.forEach(project -> project.removeTask(task));
        return this;
    }

    public ProjectManagementSystem deleteProject(Project project) {
        projects.remove(project);
        employees.forEach(employee -> employee.removeProject(project));
        return this;
    }

    public ProjectManagementSystem viewAllEmployees() {
        System.out.println(String.format("Employees (%d)", employees.size()));
        employees.forEach(System.out::println);
        return this;
    }

    public ProjectManagementSystem viewAllTasksFor(Project project) {
        System.out.println(String.format("\nTasks (%d) for %s", project.tasks.size(), project.name));
        project.tasks.forEach(System.out::println);
        return this;
    }

    public Integer maximumDaysToWorkFor(List<Project> projects) {
        return projects.stream().mapToInt(Project::daysNeeded).sum();
    }

    public Employee getEmployeeByLastName(String lastName) {
        return employees.stream()
                .filter(e -> e.lastName.equals(lastName))
                .collect(Collectors.toList())
                .get(0);
    }

    public Project getProjectByName(String name) {
        return projects.stream()
                .filter(p -> p.name.equals(name))
                .collect(Collectors.toList())
                .get(0);
    }

    public Task getTaskByName(String name) {
        return tasks.stream()
                .filter(t -> t.name.equals(name))
                .collect(Collectors.toList())
                .get(0);
    }

}
