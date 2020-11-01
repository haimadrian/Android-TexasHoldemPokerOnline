package driver;

import cinema.Cinema;
import person.Employee;
import person.Student;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Haim Adrian
 * @since 01-Nov-20
 */
public class CinemaMain {
    private final Cinema cinema = new Cinema(50, 10);
    private final AtomicBoolean anyFailure = new AtomicBoolean();

    public static void main(String[] args) {
        new CinemaMain().run();
    }

    void run() {
        int studCount = 300;
        int threadsCount = 3;
        int slice = studCount / threadsCount;

        Employee[] secretaries = new Employee[threadsCount];
        for (int i = 0; i < threadsCount; i++) {
            secretaries[i] = new Employee(String.valueOf(i), "Sec" + i);
        }

        Student[] students = new Student[studCount];
        for (int i = 0; i < studCount; i++) {
            students[i] = new Student(String.valueOf(i), "Stud" + i);
        }

        Thread[] threads = new Thread[threadsCount];
        for (int i = 0; i < threadsCount; i++) {
            threads[i] = new Thread(new WorkerJob(secretaries[i], Arrays.copyOfRange(students, i*slice, i*slice + slice)));
            threads[i].start();
        }

        println("Main is waiting for threads..." + System.lineSeparator());
        for (int i = 0; i < threadsCount; i++) {
            try {
                threads[i].join();
            } catch (InterruptedException e) {
                println("Error has occurred: " + e.toString());
            }
        }

        println(System.lineSeparator() + "Cinema:" + System.lineSeparator() + cinema.toString());
        if (anyFailure.get()) {
            System.err.println("!!!!! FAILURE !!!!!");
        }
    }

    private static synchronized void println(String s) {
        System.out.println(s);
    }

    private class WorkerJob implements Runnable {
        private final Employee employee;
        private final Student[] students;
        private final Random rand = new SecureRandom();

        public WorkerJob(Employee employee, Student[] students) {
            this.employee = employee;
            this.students = students;
        }

        @Override
        public void run() {
            for (int i = 0; i < students.length; i++) {
                int row = rand.nextInt(cinema.getRows());
                int col = rand.nextInt(cinema.getCols());

                boolean success = false;
                if (cinema.isSeatAvailable(row, col)) {
                    success = cinema.orderSeat(row, col, students[i]);
                    System.out.println(employee.getName() + " tried to order seat at [" + row + ", " + col + "] for " + students[i].getName() + ". Success=" + success);
                    if (!success) {
                        anyFailure.set(true);
                    }
                }

                // Try again in case of failure or unavailable seat
                if (!success) {
                    i--;
                }
            }
        }
    }
}

