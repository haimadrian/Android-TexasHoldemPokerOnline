package driver;

import model.car.Car;
import model.common.SizeEnum;
import model.parking.ParkingLot;
import model.person.Person;
import model.person.factory.PersonFactory;
import model.person.factory.PersonInitializationException;
import model.person.impl.Employee;
import model.person.impl.Teacher;
import model.person.visitor.IsTeacherOrProfessorVisitor;

import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.*;

/**
 * @author Haim Adrian
 * @since 09-Nov-20
 */
public class Main {
    static final int SLOTS_COUNT = 250;
    static final int CARS_COUNT = 200;
    static final int THREADS_COUNT = 3;
    static final int SLICE = SLOTS_COUNT / THREADS_COUNT;

    ParkingLot parkingLot;

    Employee[] ushers;
    Person[] ppl;
    Car[] cars;

    public static void main(String[] args) {
        new Main().run();
    }

    void run() {
        initializeData();

        // Do the work. Find parking slots for the generated cars.
        ExecutorService threadPool = Executors.newFixedThreadPool(THREADS_COUNT);
        Future<?>[] futures = new Future[THREADS_COUNT];
        for (int i = 0; i < THREADS_COUNT; i++) {
            int from = i * SLICE;
            int to = i == (THREADS_COUNT - 1) ? cars.length : i * SLICE + SLICE;
            futures[i] = threadPool.submit(new WorkerJob(ushers[i], Arrays.copyOfRange(cars, from, to)));
        }

        System.out.println("Main is waiting for threads..." + System.lineSeparator());
        for (int i = 0; i < THREADS_COUNT; i++) {
            try {
                futures[i].get();
            } catch (InterruptedException | ExecutionException e) {
                System.err.println("Error has occurred: " + e.toString());
            }
        }

        threadPool.shutdown();
        System.out.println(System.lineSeparator() + "ParkingLot:" + System.lineSeparator() + parkingLot.toString());
    }

    private void initializeData() {
        SecureRandom rand = new SecureRandom();

        ushers = new Employee[THREADS_COUNT];
        for (int i = 0; i < THREADS_COUNT; i++) {
            ushers[i] = new Employee(String.valueOf(i), "Usher" + i);
        }

        ppl = new Person[SLOTS_COUNT];
        cars = new Car[SLOTS_COUNT];
        List<Car> reservedCars = new ArrayList<>();
        for (int i = 0; i < SLOTS_COUNT; i++) {
            try {
                // We use null name here so the PersonFactory will create the name for us based on the random person type.
                ppl[i] = PersonFactory.newRandomTypePerson(String.valueOf(i), null);
                cars[i] = new Car("" + i, SizeEnum.values()[rand.nextInt(3) + 1], rand.nextBoolean(), ppl[i]);

                if (ppl[i].accept(new IsTeacherOrProfessorVisitor()).booleanValue()) {
                    reservedCars.add(cars[i]);
                }
            } catch (PersonInitializationException e) {
                System.err.println("Error has occurred while trying to generate a random type person.");
                e.printStackTrace();
            }
        }

        // If there are too much reserved cars, remove teachers and leave professors only
        if (reservedCars.size() > (int)(0.4 * SLOTS_COUNT)) {
            reservedCars.removeIf(car -> (car.getOwner() instanceof Teacher));
        }

        parkingLot = new ParkingLot(SLOTS_COUNT, reservedCars.toArray(new Car[0]));
    }

    private class WorkerJob implements Runnable {
        @SuppressWarnings("unused")
        private final Employee employee;
        private final Car[] cars;

        public WorkerJob(Employee employee, Car[] cars) {
            this.employee = employee;
            this.cars = cars;
        }

        @Override
        public void run() {
            for (int i = 0; i < cars.length; i++) {
                Car car = cars[i];
                int parkingSlot = parkingLot.getParkingSlot(car);
                if (parkingSlot >= 0) {
                    System.out.println("Car no: " + car.getCarId() + ", " + car.getOwner().getName() + ", Place: " + parkingSlot +
                                       ", Size: " + parkingLot.getParkingSlotSize(parkingSlot));
                } else {
                    System.err.println("Failed to add car: " + car);
                    throw new IllegalArgumentException("No room for car X");
                }
            }
        }
    }
}

