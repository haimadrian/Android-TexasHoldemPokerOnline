package model.car;

import model.common.SizeEnum;
import model.person.Person;

import java.util.Objects;

/**
 * @author Haim Adrian
 * @since 04-Nov-20
 */
public class Car {
    private final String carId;
    private final SizeEnum carSize;
    private boolean hasDisabledBadge;
    private Person owner;

    public Car(String carId, SizeEnum carSize, boolean hasDisabledBadge, Person owner) {
        this.carId = carId;
        this.carSize = carSize;
        this.hasDisabledBadge = hasDisabledBadge;
        this.owner = owner;
    }

    public String getCarId() {
        return carId;
    }

    public SizeEnum getCarSize() {
        return carSize;
    }

    public boolean hasDisabledBadge() {
        return hasDisabledBadge;
    }

    public void setHasDisabledBadge(boolean hasDisabledBadge) {
        this.hasDisabledBadge = hasDisabledBadge;
    }

    public Person getOwner() {
        return owner;
    }

    public void setOwner(Person owner) {
        this.owner = owner;
    }

    @Override
    public int hashCode() {
        return carId.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof Car) && Objects.equals(carId, ((Car)obj).getCarId());
    }

    @Override
    public String toString() {
        return "Car{" + "carId='" + carId + '\'' + ", carSize=" + carSize + ", hasDisabledBadge=" + hasDisabledBadge + ", owner=" + owner + '}';
    }
}

