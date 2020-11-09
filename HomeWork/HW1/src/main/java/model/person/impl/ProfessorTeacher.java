package model.person.impl;

import model.person.*;
import model.person.visitor.PersonVisitorIfc;

import java.util.ArrayList;

/**
 * @author Haim Adrian
 * @since 08-Nov-20
 */
public class ProfessorTeacher extends Person implements TeacherIfc, ProfessorIfc {
    private final ProfessorIfc professor;
    private final TeacherIfc teacher;

    public ProfessorTeacher(String id, String name) {
        this(id, name, new ArrayList<>(),0, new ArrayList<>());
    }

    public ProfessorTeacher(String id, String name, Iterable<String> departments, int rank) {
        this(id, name, departments, rank, new ArrayList<>());
    }

    public ProfessorTeacher(String id, String name, Iterable<String> degrees) {
        this(id, name, new ArrayList<>(), 0, degrees);
    }

    public ProfessorTeacher(String id, String name, Iterable<String> departments, int rank, Iterable<String> degrees) {
        super(id, name);
        professor = new Professor(id, name, degrees);
        teacher = new Teacher(id, name, departments, rank);
    }

    @Override
    public <R> R accept(PersonVisitorIfc<R> visitor) {
        return visitor.visit(this);
    }

    @Override
    public Iterable<String> getDegrees() {
        return professor.getDegrees();
    }

    @Override
    public void setDegrees(Iterable<String> degrees) {
        professor.setDegrees(degrees);
    }

    @Override
    public String professorInfo() {
        return professor.professorInfo();
    }

    @Override
    public int getRank() {
        return teacher.getRank();
    }

    @Override
    public void setRank(int rank) {
        teacher.setRank(rank);
    }

    @Override
    public Iterable<String> getDepartments() {
        return teacher.getDepartments();
    }

    @Override
    public void setDepartments(Iterable<String> departments) {
        teacher.setDepartments(departments);
    }

    @Override
    public String teacherInfo() {
        return teacher.teacherInfo();
    }

    @Override
    public String toString() {
        return super.toString() + ", " + teacherInfo() + ", " + professorInfo();
    }
}

