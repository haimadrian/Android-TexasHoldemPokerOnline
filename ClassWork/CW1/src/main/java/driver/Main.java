package driver;

import person.*;
import person.factory.*;
import person.scanner.PersonConsoleScanner;

import java.util.*;

/**
 * @author Haim Adrian
 * @since 24-Oct-20
 */
public class Main {
    private final List<Person> persons = new ArrayList<>();

    public static void main(String[] args) {
        new Main().run();
    }

    void run() {
        try (Scanner scanner = new Scanner(System.in)) {
            boolean running = true;

            while (running) {
                PersonEnum type = menu(scanner);

                if (type != null) {
                    try {
                        persons.add(readPerson(scanner, type));
                    } catch (Exception e) {
                        System.out.println("Unexpected error has occurred: " + e.getMessage());
                        e.printStackTrace();
                        running = false;
                    }
                } else {
                    running = false;
                }
            }

            isEmployee(persons);
        }
    }

    /**
     * Prints amount of employees
     * @param persons List of persons to scan
     */
    private void isEmployee(List<Person> persons) {
        long amountOfEmployees = persons.stream().filter(person -> person instanceof EmployeeIfc).count();
        String summary = amountOfEmployees == 0 ? "is no employee" : (amountOfEmployees == 1 ? "is one employee" : "are " + amountOfEmployees + " employees");
        System.out.println("There " + summary);
    }

    private Person readPerson(Scanner scanner, PersonEnum type) throws PersonInitializationException {
        return new PersonConsoleScanner().readPerson(type, scanner);
    }

    private PersonEnum menu(Scanner in) {
        PersonEnum result = null;

        while (result == null) {
            try {
                for (PersonEnum type : PersonEnum.values()) {
                    System.out.println("[" + (type.ordinal() + 1) + "]-" + type.name());
                }
                System.out.println("[0]-Exit");

                int choice = in.nextInt();
                in.nextLine();
                if (choice < 0 || choice > PersonEnum.values().length) {
                    throw new IllegalInputException("Wrong input. Input was: " + choice);
                }

                if (choice == 0) {
                    break;
                } else {
                    result = PersonEnum.values()[choice - 1];
                }
            } catch (IllegalInputException e) {
                System.out.println("Error: " + e.getMessage());
            } catch (Exception e) {
                System.out.println("Unexpected error has occurred: " + e.getMessage());
                e.printStackTrace();
            }
        }

        return result;
    }

    private static class IllegalInputException extends Exception {
        public IllegalInputException(String message) {
            super(message);
        }
    }
}

