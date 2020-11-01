package car.worker;

import car.Car;
import car.parts.*;

import java.util.List;

/**
 * @author Haim Adrian
 * @since 01-Nov-20
 */
public class SeatWorker extends CarPartWorker {
    public SeatWorker(List<Car> cars) {
        super(cars);
    }

    @Override
    protected int getPartsCount() {
        return 5;
    }

    @Override
    protected CarPart generatePart() {
        return new Seat();
    }

    @Override
    protected long getWorkingTimeInSeconds() {
        return 7;
    }
}

