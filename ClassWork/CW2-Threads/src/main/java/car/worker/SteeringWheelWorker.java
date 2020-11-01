package car.worker;

import car.Car;
import car.parts.*;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author Haim Adrian
 * @since 01-Nov-20
 */
public class SteeringWheelWorker extends CarPartWorker {
    public SteeringWheelWorker(List<Car> cars) {
        super(cars);
    }

    @Override
    protected int getPartsCount() {
        return 1;
    }

    @Override
    protected CarPart generatePart() {
        return new SteeringWheel();
    }

    @Override
    protected long getWorkingTimeInSeconds() {
        return 4;
    }
}

