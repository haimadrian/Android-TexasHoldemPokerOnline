package org.hit.android.haim.calc.action;

import lombok.Getter;

public class ActionContext {
    /**
     * For operators that take two arguments, this is the number after the operator.
     */
    @Getter
    private double lastValue;

    /**
     * Value before an operator. Used by both operators that take a single argument and operator that take two arguments.
     */
    @Getter
    private double value;

    @Getter
    private String dynamicString;

    /**
     * Constructs a new {@link ActionContext}
     *
     * @param lastValue For operators that take two arguments, this is the number after the operator.
     * @param value Value before an operator. Used by both operators that take a single argument and operator that take two arguments.
     */
    public ActionContext(double lastValue, double value) {
        this.lastValue = lastValue;
        this.value = value;
    }

    public ActionContext(String dynamicString) {
        this.dynamicString = dynamicString;
    }
}
