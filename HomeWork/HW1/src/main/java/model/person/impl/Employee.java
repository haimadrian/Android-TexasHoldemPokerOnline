package model.person.impl;

import model.person.EmployeeIfc;
import model.person.Person;
import model.person.visitor.PersonVisitorIfc;

/**
 * @author Haim Adrian
 * @since 18-Oct-20
 */
public class Employee extends Person implements EmployeeIfc {
    private String workplace = "";
    private String job = "";

    public Employee(String id, String name) {
        super(id, name);
    }

    public Employee(String id, String name, String workplace, String job) {
        super(id, name);
        this.workplace = workplace;
        this.job = job;
    }

    @Override
    public <R> R accept(PersonVisitorIfc<R> visitor) {
        return visitor.visit(this);
    }

    @Override
    public String getWorkplace() {
        return workplace;
    }

    @Override
    public void setWorkplace(String workplace) {
        this.workplace = workplace;
    }

    @Override
    public String getJob() {
        return job;
    }

    @Override
    public void setJob(String job) {
        this.job = job;
    }

    @Override
    public String workInfo() {
        return "workplace='" + workplace + '\'' + ", job='" + job + '\'';
    }

    @Override
    public String toString() {
        return super.toString() + ", " + workInfo();
    }
}

