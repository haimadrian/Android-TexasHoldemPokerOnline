package person.factory;

import person.*;

/**
 * @author Haim Adrian
 * @since 24-Oct-20
 */
public enum PersonEnum {
    Student(Student.class),
    Employee(Employee.class),
    StudentEmployee(StudentEmployee.class);

    private final Class<? extends Person> cls;

    PersonEnum(Class<? extends Person> cls) {
        this.cls = cls;
    }

    public Class<? extends Person> getClazz() {
        return cls;
    }
}

