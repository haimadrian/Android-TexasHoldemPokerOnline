package model.person.visitor;

import model.person.impl.*;

/**
 * @author Haim Adrian
 * @since 09-Nov-20
 */
public class IsTeacherOrProfessorVisitor implements PersonVisitorIfc<Boolean> {

    @Override
    public Boolean visit(Student student) {
        return Boolean.FALSE;
    }

    @Override
    public Boolean visit(Teacher teacher) {
        return Boolean.TRUE;
    }

    @Override
    public Boolean visit(Professor professor) {
        return Boolean.TRUE;
    }

    @Override
    public Boolean visit(StudentTeacher studentTeacher) {
        return Boolean.TRUE;
    }

    @Override
    public Boolean visit(ProfessorTeacher professorTeacher) {
        return Boolean.TRUE;
    }

    @Override
    public Boolean visit(Visitor visitor) {
        return Boolean.FALSE;
    }

    @Override
    public Boolean visit(Employee employee) {
        return Boolean.FALSE;
    }
}

