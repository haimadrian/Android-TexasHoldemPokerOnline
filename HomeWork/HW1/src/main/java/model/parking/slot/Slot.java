package model.parking.slot;

import model.car.Car;
import model.common.SizeEnum;

/**
 * @author Haim Adrian
 * @since 09-Nov-20
 */
public abstract class Slot {
    private SizeEnum size;
    private Car parkingCar;

    public Slot(SizeEnum size) {
        this.size = size;
    }

    public SizeEnum getSize() {
        return size;
    }

    public void setSize(SizeEnum size) {
        this.size = size;
    }

    /**
     * Adds a car to this slot.<br/>
     * A slot must be empty in order for the add to succeed, and the car must be of a relevant size.
     * In addition, any implementor can add its own verifications
     * @param car The car to add
     * @return Whether addition succeeded or failed
     */
    public boolean add(Car car) {
        if ((parkingCar == null) && car.getCarSize().fitsIn(size)) {
            parkingCar = car;
            return true;
        }

        return false;
    }

    public boolean remove(Car car) {
        if ((parkingCar != null) && parkingCar.equals(car)) {
            parkingCar = null;
            return true;
        }

        return false;
    }

    public Car getParkingCar() {
        return parkingCar;
    }

    public void setParkingCar(Car parkingCar) {
        this.parkingCar = parkingCar;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{size=" + size + ", parkingCar=" + parkingCar + '}';
    }
}

