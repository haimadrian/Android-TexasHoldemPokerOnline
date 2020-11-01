package driver;

import car.Car;
import car.worker.*;

import java.util.Arrays;

/**
 * @author Haim Adrian
 * @since 01-Nov-20
 */
public class CarFactoryMain {

    public static void main(String[] args) {
        new CarFactoryMain().run();
    }

    void run() {
        int carsCount = 12;
        int threadsCount = 4;

        for (int i = 0; i < carsCount; i++) {
            System.out.println("Creating car number " + (i + 1));
            Car car = new Car(i + 1);

            Thread[] threads = new Thread[threadsCount];
            threads[0] = new Thread(new SeatWorker(Arrays.asList(car)));
            threads[1] = new Thread(new SteeringWheelWorker(Arrays.asList(car)));
            threads[2] = new Thread(new EngineWorker(Arrays.asList(car)));
            threads[3] = new Thread(new WheelWorker(Arrays.asList(car)));

            for (int j = 0; j < threadsCount; j++) {
                threads[j].start();
            }

            println("Main is waiting for threads..." + System.lineSeparator());
            for (int j = 0; j < threadsCount; j++) {
                try {
                    threads[j].join();
                } catch (InterruptedException e) {
                    println("Error has occurred: " + e.toString());
                }
            }

            System.out.println(System.lineSeparator() + car + System.lineSeparator());
        }
    }

    private static synchronized void println(String s) {
        System.out.println(s);
    }
}

