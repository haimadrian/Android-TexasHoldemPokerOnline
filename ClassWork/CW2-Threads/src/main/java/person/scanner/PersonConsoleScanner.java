package person.scanner;

import person.*;
import person.factory.*;

import java.util.Scanner;

/**
 * @author Haim Adrian
 * @since 25-Oct-20
 */
public class PersonConsoleScanner {
    public Person readPerson(PersonEnum type, Scanner scanner) throws PersonInitializationException {
        Person person = null;

        switch (type) {
            case Student:
                person = new StudentReader().read(type, scanner);
                break;
            case Employee:
                person = new EmployeeReader().read(type, scanner);
                break;
            case StudentEmployee:
                person = new StudentEmployeeReader().read(type, scanner);
                break;
        }

        return person;
    }

    private static class PersonReader {
        @SuppressWarnings("unchecked")
        public <R extends Person> R read(PersonEnum type, Scanner scanner) throws PersonInitializationException {
            System.out.println("Please enter data for " + type.name() + " below.");
            System.out.println("ID:");
            String id = scanner.nextLine();
            System.out.println("Name:");
            String name = scanner.nextLine();
            return (R)PersonFactory.newPerson(type, id, name);
        }
    }

    private static class StudentReader extends PersonReader {
        @SuppressWarnings("unchecked")
        @Override
        public Student read(PersonEnum type, Scanner scanner) throws PersonInitializationException {
            Student student = super.read(type, scanner);
            fillInData(student, scanner);
            return student;
        }

        void fillInData(StudentIfc student, Scanner scanner) {
            System.out.println("Department:");
            student.setDepartment(scanner.nextLine());
            System.out.println("Year:");
            student.setYear(scanner.nextInt());
            scanner.nextLine();
        }
    }

    private static class EmployeeReader extends PersonReader {
        @SuppressWarnings("unchecked")
        @Override
        public Employee read(PersonEnum type, Scanner scanner) throws PersonInitializationException {
            Employee employee = super.read(type, scanner);
            fillInData(employee, scanner);
            return employee;
        }

        void fillInData(EmployeeIfc employee, Scanner scanner) {
            System.out.println("Workplace:");
            employee.setWorkplace(scanner.nextLine());
            System.out.println("Job:");
            employee.setJob(scanner.nextLine());
        }
    }

    private static class StudentEmployeeReader extends PersonReader {
        @SuppressWarnings("unchecked")
        @Override
        public StudentEmployee read(PersonEnum type, Scanner scanner) throws PersonInitializationException {
            StudentEmployee studentEmployee = super.read(type, scanner);
            new StudentReader().fillInData(studentEmployee, scanner);
            new EmployeeReader().fillInData(studentEmployee, scanner);
            return studentEmployee;
        }
    }
}

