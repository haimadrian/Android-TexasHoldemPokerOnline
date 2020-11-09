package model.person.impl;

import model.person.*;
import model.person.visitor.PersonVisitorIfc;

import java.util.ArrayList;

/**
 * @author Haim Adrian
 * @since 08-Nov-20
 */
public class StudentTeacher extends Person implements TeacherIfc, StudentIfc {
    private final StudentIfc student;
    private final TeacherIfc teacher;

    public StudentTeacher(String id, String name) {
        this(id, name, new ArrayList<>(), 0, "", 0);
    }

    public StudentTeacher(String id, String name, Iterable<String> departments, int rank) {
        this(id, name, departments, rank, "", 0);
    }

    public StudentTeacher(String id, String name, String department, int year) {
        this(id, name, new ArrayList<>(), 0, department, year);
    }

    public StudentTeacher(String id, String name, Iterable<String> departments, int rank, String department, int year) {
        super(id, name);
        student = new Student(id, name, department, year);
        teacher = new Teacher(id, name, departments, rank);
    }

    @Override
    public <R> R accept(PersonVisitorIfc<R> visitor) {
        return visitor.visit(this);
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
        return super.toString() + ", " + teacherInfo() + ", " + studentInfo();
    }
}

