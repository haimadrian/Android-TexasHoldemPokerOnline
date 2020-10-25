package person;

/**
 * @author Haim Adrian
 * @since 18-Oct-20
 */
public class Student extends Person implements StudentIfc {
    private String department = "";
    private int year = 0;

    public Student(String id, String name) {
        super(id, name);
    }

    public Student(String id, String name, String department, int year) {
        super(id, name);
        this.department = department;
        this.year = year;
    }

    @Override
    public String getDepartment() {
        return department;
    }

    @Override
    public void setDepartment(String department) {
        this.department = department;
    }

    @Override
    public int getYear() {
        return year;
    }

    @Override
    public void setYear(int year) {
        this.year = year;
    }

    @Override
    public String studentInfo() {
        return "dept='" + department + "', year=" + year;
    }

    @Override
    public String toString() {
        return super.toString() + ", " + studentInfo();
    }
}

