package model.person;

/**
 * @author Haim Adrian
 * @since 18-Oct-20
 */
public interface EmployeeIfc {
    String getWorkplace();
    void setWorkplace(String workplace);
    String getJob();
    void setJob(String job);
    String workInfo();
}

