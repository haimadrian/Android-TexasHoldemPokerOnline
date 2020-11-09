package model.person.factory;

import model.person.Person;

import java.security.SecureRandom;

/**
 * @author Haim Adrian
 * @since 08-Nov-20
 */
public class PersonFactory {
    @SuppressWarnings("unchecked")
    public static <R extends Person> R newPerson(PersonEnum type, String id, String name) throws PersonInitializationException {
        try {
            // Do it so we can generate the name based on a type when we use randomized ppl creation
            String validatedName = name == null ? (type.name() + id) : name;
            return (R)type.getClazz().getConstructor(String.class, String.class).newInstance(id, validatedName);
        } catch (Exception e) {
            throw new PersonInitializationException("Unable to initialize person of type: " + type, e);
        }
    }

    public static <R extends Person> R newRandomTypePerson(String id, String name) throws PersonInitializationException {
        SecureRandom rand = new SecureRandom();
        return newPerson(PersonEnum.values()[rand.nextInt(PersonEnum.values().length)], id, name);
    }
}

