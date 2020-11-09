package model.person;

/**
 * @author Haim Adrian
 * @since 08-Nov-20
 */
public interface TeacherIfc {
    int getRank();
    void setRank(int rank);
    Iterable<String> getDepartments();
    void setDepartments(Iterable<String> departments);
    String teacherInfo();
}

