package person;

/**
 * @author Haim Adrian
 * @since 18-Oct-20
 */
public class StudentEmployee extends Person implements EmployeeIfc, StudentIfc {
    private final StudentIfc student;
    private final EmployeeIfc employee;

    public StudentEmployee(String id, String name) {
        this(id, name, "", "", "", 0);
    }

    public StudentEmployee(String id, String name, String workplace, String job) {
        this(id, name, workplace, job, "", 0);
    }

    public StudentEmployee(String id, String name, String department, int year) {
        this(id, name, "", "", department, year);
    }

    public StudentEmployee(String id, String name, String workplace, String job, String department, int year) {
        super(id, name);
        student = new Student(id, name, department, year);
        employee = new Employee(id, name, workplace, job);
    }

    @Override
    public String getDepartment() {
        return student.getDepartment();
    }

    @Override
    public void setDepartment(String department) {
        student.setDepartment(department);
    }

    @Override
    public int getYear() {
        return student.getYear();
    }

    @Override
    public void setYear(int year) {
        student.setYear(year);
    }

    @Override
    public String studentInfo() {
        return student.studentInfo();
    }

    @Override
    public String getWorkplace() {
        return employee.getWorkplace();
    }

    @Override
    public void setWorkplace(String workplace) {
        employee.setWorkplace(workplace);
    }

    @Override
    public String getJob() {
        return employee.getJob();
    }

    @Override
    public void setJob(String job) {
        employee.setJob(job);
    }

    @Override
    public String workInfo() {
        return employee.workInfo();
    }

    @Override
    public String toString() {
        return super.toString() + ", " + workInfo() + ", " + studentInfo();
    }
}

