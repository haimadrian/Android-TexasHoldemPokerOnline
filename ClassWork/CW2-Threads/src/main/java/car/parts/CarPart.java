package car.parts;

/**
 * @author Haim Adrian
 * @since 01-Nov-20
 */
public abstract class CarPart {
    private final String code;

    public CarPart(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    @Override
    public String toString() {
        return code;
    }
}

