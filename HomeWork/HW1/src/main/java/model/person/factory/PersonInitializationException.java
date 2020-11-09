package model.person.factory;

/**
 * @author Haim Adrian
 * @since 08-Nov-20
 */
public class PersonInitializationException extends Exception {
    public PersonInitializationException() {
    }

    public PersonInitializationException(String message) {
        super(message);
    }

    public PersonInitializationException(String message, Throwable cause) {
        super(message, cause);
    }

    public PersonInitializationException(Throwable cause) {
        super(cause);
    }

    public PersonInitializationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}

