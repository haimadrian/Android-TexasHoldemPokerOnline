package model.car;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Haim Adrian
 * @since 08-Nov-20
 */
public class Garage {
    private int carsCapacity;
    private final Map<String, Car> cars;

    public Garage(int carsCapacity) {
        this.carsCapacity = carsCapacity;
        cars = new HashMap<>(carsCapacity);
    }

    public boolean addCar(Car car) {
        boolean done = false;
        if (cars.size() < carsCapacity) {
            cars.put(car.getCarId(), car);
            done = true;
        }
        return done;
    }

    public boolean removeCar(String carId) {
        boolean done = false;
        if (cars.containsKey(carId)) {
            cars.remove(carId);
            done = true;
        }
        return done;
    }

    public boolean removeCar(Car car) {
        return removeCar(car.getCarId());
    }

    public int getCarsCapacity() {
        return carsCapacity;
    }

    public void setCarsCapacity(int carsCapacity) {
        this.carsCapacity = carsCapacity;
    }
}

