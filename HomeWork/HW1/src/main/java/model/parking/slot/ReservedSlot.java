package model.parking.slot;

import model.car.Car;
import model.common.SizeEnum;

import java.util.Objects;

/**
 * @author Haim Adrian
 * @since 09-Nov-20
 */
public class ReservedSlot extends Slot {
    private String reservedForCarId;

    public ReservedSlot(SizeEnum size) {
        super(size);
    }

    public ReservedSlot(SizeEnum size, String reservedForCarId) {
        super(size);
        this.reservedForCarId = reservedForCarId;
    }

    @Override
    public boolean add(Car car) {
        boolean succeed = false;

        // Make sure it is the same car ID
        if (Objects.equals(car.getCarId(), reservedForCarId)) {
            succeed = super.add(car);
        }

        return succeed;
    }

    public String getReservedForCarId() {
        return reservedForCarId;
    }

    public void setReservedForCarId(String reservedForCarId) {
        this.reservedForCarId = reservedForCarId;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{size=" + getSize() + ", parkingCar=" + getParkingCar() + ", reservedForCarId=" + getReservedForCarId() + '}';
    }
}

