package pms;


import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class ProjectManagementSystemTest {

    private ProjectManagementSystem pms;

    @Before
    public void setPms() {
        pms = new ProjectManagementSystem(
                "./src/main/resources/employees.csv",
                "./src/main/resources/projects.csv",
                "./src/main/resources/tasks.csv"
        );
    }

    @Test
    public void importOfEntitiesWorks() {
        assertEquals(10, pms.employees.size());
        assertEquals(4, pms.projects.size());
        assertEquals(9, pms.tasks.size());

        Employee parker = pms.getEmployeeByLastName("Parker");
        assertEquals("Hanne", parker.supervisor.lastName);

        Employee crescentia = pms.getEmployeeByLastName("Crescentia");
        assertNull(crescentia.supervisor);

        List<Project> systemFixProjects = pms
                .projects.stream()
                .filter(p -> p.name.equals("System Fix"))
                .collect(Collectors.toList());
        assertEquals(1, systemFixProjects.size());
        assertEquals("2015-01-01", systemFixProjects.get(0).startDate.toString());
        assertEquals("2015-01-01", systemFixProjects.get(0).endDate.toString());

        Task dataTransformation = pms.getTaskByName("Data Transformation");
        assertEquals(0.5, dataTransformation.estimatedDays, 0.0001);
    }

    @Test
    public void taskCanBeSuccessfullyAssignedToProject() {
        Project systemFix = pms.getProjectByName("System Fix");
        assertEquals(0, systemFix.tasks.size());

        Task dataTransformation = pms.getTaskByName("Data Transformation");
        Task testCoverage = pms.getTaskByName("Test Coverage");
        pms.assignTaskToProject(dataTransformation, systemFix)
                .assignTaskToProject(testCoverage, systemFix);
        assertEquals(2, systemFix.tasks.size());
        assertEquals("2015-01-01", systemFix.endDate.toString());

        Task dataCleaning = pms.getTaskByName("Data Cleaning");
        pms.assignTaskToProject(dataCleaning, systemFix);
        assertEquals(3, systemFix.tasks.size());
        assertEquals("2015-01-05", systemFix.endDate.toString());
    }

    @Test
    public void projectCanBeSuccessfullyAssignedToEmployee() {
        Project systemFix = pms.getProjectByName("System Fix");
        Project missionImpossible = pms.getProjectByName("Mission Impossible");
        Project kraken2 = pms.getProjectByName("Kraken II");
        Employee parker = pms.getEmployeeByLastName("Parker");
        assertEquals(0, parker.projects.size());

        pms.assignProjectToEmployee(systemFix, parker)
                .assignProjectToEmployee(kraken2, parker)
                .assignProjectToEmployee(missionImpossible, parker);
        assertEquals(2, parker.projects.size());
    }

    @Test
    public void taskCanBeDeletedSafely() {
        Project systemFix = pms.getProjectByName("System Fix");
        Task dataTransformation = pms.getTaskByName("Data Transformation");
        pms.assignTaskToProject(dataTransformation, systemFix);

        pms.deleteTask(dataTransformation);
        assertEquals(8, pms.tasks.size());
        assertEquals(0, systemFix.tasks.size());
        assertEquals("2015-01-01", systemFix.endDate.toString());
    }

    @Test
    public void projectsCanBeDeletedSafely() {
        Employee parker = pms.getEmployeeByLastName("Parker");
        Project systemFix = pms.getProjectByName("System Fix");
        pms.assignProjectToEmployee(systemFix, parker);

        pms.deleteProject(systemFix);
        assertEquals(3, pms.projects.size());
        assertEquals(0,parker.projects.size());
    }

    @Test
    public void totalTimeForProjectsIsCalculatedCorrectly() {
        Project systemFix = pms.getProjectByName("System Fix");
        Project missionImpossible = pms.getProjectByName("Mission Impossible");
        Task dataTransformation = pms.getTaskByName("Data Transformation");
        Task dataCleaning = pms.getTaskByName("Data Cleaning");
        pms.assignTaskToProject(dataTransformation, systemFix)
                .assignTaskToProject(dataCleaning, systemFix);

        List<Project> projects = new ArrayList<>(List.of(systemFix, missionImpossible));
        int days = pms.maximumDaysToWorkFor(projects);
        assertEquals(28, days);
    }



}
