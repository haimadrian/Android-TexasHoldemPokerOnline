package model.person.visitor;

import model.person.impl.*;

/**
 * An interface representing the visitor pattern, and not a visitor person
 * @author Haim Adrian
 * @since 08-Nov-20
 */
public interface PersonVisitorIfc<R> {
    R visit(Student student);
    R visit(Teacher teacher);
    R visit(Professor professor);
    R visit(StudentTeacher studentTeacher);
    R visit(ProfessorTeacher professorTeacher);
    R visit(Visitor visitor);
    R visit(Employee employee);
}

