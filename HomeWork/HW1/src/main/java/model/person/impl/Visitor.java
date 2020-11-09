package model.person.impl;

import model.person.Person;
import model.person.visitor.PersonVisitorIfc;

/**
 * @author Haim Adrian
 * @since 08-Nov-20
 */
public class Visitor extends Person {
    public Visitor(String id, String name) {
        super(id, name);
    }

    @Override
    public <R> R accept(PersonVisitorIfc<R> visitor) {
        return visitor.visit(this);
    }
}

