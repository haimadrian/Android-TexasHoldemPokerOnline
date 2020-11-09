package model.parking.slot;

import model.car.Car;
import model.common.SizeEnum;

import java.time.LocalDateTime;

/**
 * @author Haim Adrian
 * @since 09-Nov-20
 */
public class RegularSlot extends Slot {
    /** A number between 0 to 23 */
    private int startHour;

    /** A number between 1 to 24 */
    private int endHour;

    public RegularSlot(SizeEnum size) {
        this(size, 0, 24);
    }

    public RegularSlot(SizeEnum size, int startHour, int endHour) {
        super(size);
        this.startHour = startHour;
        this.endHour = endHour;
    }

    @Override
    public boolean add(Car car) {
        boolean succeed = false;

        LocalDateTime dateTime = LocalDateTime.now();
        int currHour = dateTime.getHour();

        // Check the case of cycle, when start hour is bigger than end.
        if (endHour < startHour) {
            if ((currHour >= startHour) || (currHour < endHour)) {
                succeed = true;
            }
        } else if ((currHour >= startHour) && (currHour < endHour)) {
            succeed = true;
        }

        if (succeed) {
            succeed = super.add(car);
        }

        return succeed;
    }

    public int getStartHour() {
        return startHour;
    }

    public void setStartHour(int startHour) {
        this.startHour = startHour;
    }

    public int getEndHour() {
        return endHour;
    }

    public void setEndHour(int endHour) {
        this.endHour = endHour;
    }
}

