package model.person;

/**
 * @author Haim Adrian
 * @since 08-Nov-20
 */
public interface StudentIfc {
    String getDepartment();
    void setDepartment(String department);
    int getYear();
    void setYear(int year);
    String studentInfo();
}

