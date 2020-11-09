package model.person.impl;

import model.person.Person;
import model.person.ProfessorIfc;
import model.person.visitor.PersonVisitorIfc;

import java.util.ArrayList;

/**
 * @author Haim Adrian
 * @since 08-Nov-20
 */
public class Professor extends Person implements ProfessorIfc {
    private Iterable<String> degrees = new ArrayList<>();

    public Professor(String id, String name) {
        super(id, name);
    }

    @Override
    public <R> R accept(PersonVisitorIfc<R> visitor) {
        return visitor.visit(this);
    }

    public Professor(String id, String name, Iterable<String> degrees) {
        super(id, name);
        this.degrees = degrees;
    }

    @Override
    public Iterable<String> getDegrees() {
        return degrees;
    }

    @Override
    public void setDegrees(Iterable<String> degrees) {
        this.degrees = degrees;
    }

    @Override
    public String professorInfo() {
        return "degrees=" + degrees.toString();
    }

    @Override
    public String toString() {
        return super.toString() + ", " + professorInfo();
    }


}

