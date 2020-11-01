package car.worker;

import car.Car;
import car.parts.CarPart;
import car.parts.Seat;

import java.util.List;

/**
 * @author Haim Adrian
 * @since 01-Nov-20
 */
public class EngineWorker extends CarPartWorker {
    public EngineWorker(List<Car> cars) {
        super(cars);
    }

    @Override
    protected int getPartsCount() {
        return 1;
    }

    @Override
    protected CarPart generatePart() {
        return new car.parts.Engine();
    }

    @Override
    protected long getWorkingTimeInSeconds() {
        return 12;
    }
}

