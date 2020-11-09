package model.person.impl;

import model.person.Person;
import model.person.TeacherIfc;
import model.person.visitor.PersonVisitorIfc;

import java.util.ArrayList;

/**
 * @author Haim Adrian
 * @since 08-Nov-20
 */
public class Teacher extends Person implements TeacherIfc {
    private Iterable<String> departments = new ArrayList<>();
    private int rank = 0;

    public Teacher(String id, String name) {
        super(id, name);
    }

    public Teacher(String id, String name, Iterable<String> departments, int rank) {
        super(id, name);
        this.departments = departments;
        this.rank = rank;
    }

    @Override
    public <R> R accept(PersonVisitorIfc<R> visitor) {
        return visitor.visit(this);
    }

    @Override
    public int getRank() {
        return rank;
    }

    @Override
    public void setRank(int rank) {
        this.rank = rank;
    }

    @Override
    public Iterable<String> getDepartments() {
        return departments;
    }

    @Override
    public void setDepartments(Iterable<String> departments) {
        this.departments = departments;
    }

    @Override
    public String teacherInfo() {
        return "depts=" + departments.toString() + ", rank=" + rank;
    }

    @Override
    public String toString() {
        return super.toString() + ", " + teacherInfo();
    }
}

