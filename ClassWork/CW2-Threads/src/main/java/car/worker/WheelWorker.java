package car.worker;

import car.Car;
import car.parts.CarPart;
import car.parts.Wheel;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author Haim Adrian
 * @since 01-Nov-20
 */
public class WheelWorker extends CarPartWorker {
    public WheelWorker(List<Car> cars) {
        super(cars);
    }

    @Override
    protected int getPartsCount() {
        return 4;
    }

    @Override
    protected CarPart generatePart() {
        return new Wheel();
    }

    @Override
    protected long getWorkingTimeInSeconds() {
        return 5;
    }
}

