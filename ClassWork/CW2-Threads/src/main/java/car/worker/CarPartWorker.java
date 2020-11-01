package car.worker;

import car.Car;
import car.parts.CarPart;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author Haim Adrian
 * @since 01-Nov-20
 */
public abstract class CarPartWorker implements Runnable {
    private final List<Car> cars;

    public CarPartWorker(List<Car> cars) {
        this.cars = cars;
    }

    protected List<Car> getCars() {
        return cars;
    }

    @Override
    public void run() {
        for (Car c : getCars()) {
            for (int i = 0; i < getPartsCount(); i++) {
                CarPart part = generatePart();
                try {
                    Thread.sleep(TimeUnit.SECONDS.toMillis(getWorkingTimeInSeconds()));
                } catch (InterruptedException e) {
                    System.out.println("Error has occurred while creating " + part.getClass().getSimpleName());
                }
                c.addPart(part);

                System.out.println(this.getClass().getSimpleName() + " finished creating a " + part.getClass().getSimpleName());
            }
        }
    }

    protected abstract int getPartsCount();

    protected abstract CarPart generatePart();

    protected abstract long getWorkingTimeInSeconds();
}

