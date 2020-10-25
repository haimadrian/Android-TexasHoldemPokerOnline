package person;

/**
 * @author Haim Adrian
 * @since 18-Oct-20
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

    @Override
    public String toString() {
        return "id='" + id + '\'' + ", name='" + name + "'";
    }
}

