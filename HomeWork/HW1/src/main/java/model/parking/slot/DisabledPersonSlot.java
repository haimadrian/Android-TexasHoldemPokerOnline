package model.parking.slot;

import model.car.Car;
import model.common.SizeEnum;

/**
 * @author Haim Adrian
 * @since 09-Nov-20
 */
public class DisabledPersonSlot extends Slot {

    public DisabledPersonSlot(SizeEnum size) {
        super(size);
    }

    @Override
    public boolean add(Car car) {
        boolean succeed = false;

        if (car.hasDisabledBadge()) {
            succeed = super.add(car);
        }

        return succeed;
    }
}

