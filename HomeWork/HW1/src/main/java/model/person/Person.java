package model.person;

import model.person.visitor.PersonVisitorIfc;

/**
 * @author Haim Adrian
 * @since 08-Nov-20
 */
public abstract class Person {
    private final String id;
    private final String name;

    public Person(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void introduce() {
        System.out.println(toString());
    }

    public abstract <R> R accept(PersonVisitorIfc<R> visitor);

    @Override
    public String toString() {
        return "id='" + id + '\'' + ", name='" + name + "'";
    }
}

