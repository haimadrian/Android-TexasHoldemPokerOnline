package car;

import car.parts.CarPart;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Haim Adrian
 * @since 01-Nov-20
 */
public class Car {
    private final List<? super CarPart> parts;
    private final int id;

    public Car(int id) {
        this.id = id;
        this.parts = new ArrayList<>(11);
    }

    public <T extends CarPart> void addPart(T part) {
        synchronized (parts) {
            if (parts.size() == 11) {
                throw new IllegalStateException("Car is already full. Cannot add part: " + part);
            }

            parts.add(part);
        }
    }

    @Override
    public String toString() {
        return "Car number " + id + System.lineSeparator() +
               "Parts number for car:" + System.lineSeparator() +
               parts.stream().map(Object::toString).collect(Collectors.joining(System.lineSeparator()));
    }
}

