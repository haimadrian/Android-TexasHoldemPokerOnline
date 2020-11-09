package model.person.factory;

import model.person.Person;
import model.person.impl.*;

/**
 * @author Haim Adrian
 * @since 08-Nov-20
 */
public enum PersonEnum {
    Professor(Professor.class),
    Student(Student.class),
    Teacher(Teacher.class),
    Visitor(Visitor.class);

    private final Class<? extends Person> cls;

    PersonEnum(Class<? extends Person> cls) {
        this.cls = cls;
    }

    public Class<? extends Person> getClazz() {
        return cls;
    }
}

