package person.factory;

import person.Person;

/**
 * @author Haim Adrian
 * @since 24-Oct-20
 */
public class PersonFactory {
    @SuppressWarnings("unchecked")
    public static <R extends Person> R newPerson(PersonEnum type, String id, String name) throws PersonInitializationException {
        try {
            return (R)type.getClazz().getConstructor(String.class, String.class).newInstance(id, name);
        } catch (Exception e) {
            throw new PersonInitializationException("Unable to initialize person of type: " + type, e);
        }
    }
}

