package model.parking;

import model.car.Car;
import model.common.SizeEnum;
import model.parking.slot.*;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 * @author Haim Adrian
 * @since 09-Nov-20
 */
public class ParkingLot {
    private final Slot[] slots;
    private final Set<String> reservedCarIds;

    // Use lockers to lock specific parts of the array, this way we avoid of non necessary total synchronization,
    // which makes it faster to work with the ParkingLot class.
    private final Lock disabledPersonLock = new ReentrantLock();
    private final Lock reservedLock = new ReentrantLock();
    private final Lock regularLock = new ReentrantLock();

    private final int disabledIndex;
    private final int reservedIndex;
    private final int regularIndex;

    public ParkingLot(int slotsCount, Car[] reservedCars) {
        slots = new Slot[slotsCount];
        reservedCarIds = Arrays.stream(reservedCars).map(Car::getCarId).collect(Collectors.toSet());

        int disabledSlotsCount = (int)(0.2 * slotsCount);
        int reservedSlotsCount = (int)(0.1 * slotsCount);
        disabledIndex = 0;
        reservedIndex = disabledSlotsCount;
        regularIndex = disabledSlotsCount + reservedSlotsCount;

        // To generate random sizes
        SecureRandom rand = new SecureRandom();

        int slotsIndex, reservedSlotsIndex = 0;
        for (slotsIndex = 0; slotsIndex < disabledSlotsCount; slotsIndex++) {
            slots[slotsIndex] = new DisabledPersonSlot(SizeEnum.Large);
        }
        for (;slotsIndex < disabledSlotsCount + reservedSlotsCount; slotsIndex++) {
            // Register reservation to slot.
            String reservedForCarId = "";
            if (reservedSlotsIndex < reservedCars.length) {
                reservedForCarId = reservedCars[reservedSlotsIndex++].getCarId();
            }
            slots[slotsIndex] = new ReservedSlot(SizeEnum.values()[rand.nextInt(3) + 1], reservedForCarId);
        }
        for (;slotsIndex < slotsCount; slotsIndex++) {
            slots[slotsIndex] = new RegularSlot(SizeEnum.values()[rand.nextInt(3) + 1], 0, 24);
        }
    }

    public int getParkingSlot(Car car) {
        int selectedIndex = -1;

        if (car.hasDisabledBadge()) {
            selectedIndex = tryGetParkingSlot(disabledIndex, reservedIndex, car, disabledPersonLock);
        }

        // A professor might have a disabled person badge, so make sure we do not add him twice.
        if ((selectedIndex < 0) && reservedCarIds.contains(car.getCarId())) {
            selectedIndex = tryGetParkingSlot(reservedIndex, regularIndex, car, reservedLock);
        }

        // If there was no place for a disabled person or professor, cover that now along with a regular car
        if (selectedIndex < 0) {
            selectedIndex = tryGetParkingSlot(regularIndex, slots.length, car, regularLock);
        }

        return selectedIndex;
    }

    private int tryGetParkingSlot(int from, int to, Car car, Lock locker) {
        int selectedIndex = -1;

        locker.lock();
        try {
            for (int i = from; i < to; i++) {
                if (slots[i].add(car)) {
                    selectedIndex = i;
                    break;
                }
            }
        } finally {
            locker.unlock();
        }

        return selectedIndex;
    }

    public SizeEnum getParkingSlotSize(int index) {
        if (index <= 0 || index >= slots.length) {
            return SizeEnum.Undefined;
        } else {
            return slots[index].getSize();
        }
    }

    @Override
    public String toString() {
        return "Slots=" + System.lineSeparator() +
               Arrays.stream(slots).map(Object::toString).collect(Collectors.joining(System.lineSeparator())) + System.lineSeparator() +
               "ReservedCarIds=" + reservedCarIds + '}';
    }
}

