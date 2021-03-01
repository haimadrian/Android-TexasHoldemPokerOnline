package com.example.calc.action;

public class ActionContext {
    /**
     * For operators that take two arguments, this is the number after the operator.
     */
    private final double lastValue;

    /**
     * Value before an operator. Used by both operators that take a single argument and operator that take two arguments.
     */
    private final double value;

    /**
     * Constructs a new {@link ActionContext}
     * @param lastValue For operators that take two arguments, this is the number after the operator.
     * @param value Value before an operator. Used by both operators that take a single argument and operator that take two arguments.
     */
    public ActionContext(double lastValue, double value) {
        this.lastValue = lastValue;
        this.value = value;
    }

    public double getLastValue() {
        return lastValue;
    }

    public double getValue() {
        return value;
    }
}
