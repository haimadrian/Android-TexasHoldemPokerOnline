package model.person;

/**
 * @author Haim Adrian
 * @since 08-Nov-20
 */
public interface ProfessorIfc {
    Iterable<String> getDegrees();
    void setDegrees(Iterable<String> degrees);
    String professorInfo();
}

