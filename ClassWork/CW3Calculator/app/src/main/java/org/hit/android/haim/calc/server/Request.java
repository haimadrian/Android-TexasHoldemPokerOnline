package org.hit.android.haim.calc.server;


import org.hit.android.haim.calc.action.ActionType;

/**
 * @author Haim Adrian
 * @since 13-Apr-21
 */
public class Request {
    /**
     * What action to execute
     */
    private ActionType actionType;

    /**
     * The value to execute an action on
     */
    private Double value;

    /**
     * For operators that take two arguments, this is the right argument
     */
    private Double lastValue;

    private String dynamicValue;

    public Request() {
    }

    public Request(ActionType actionType, Double value, Double lastValue) {
        this.actionType = actionType;
        this.value = value;
        this.lastValue = lastValue;
    }

    public Request(ActionType actionType, String dynamicValue) {
        this.actionType = actionType;
        this.dynamicValue = dynamicValue;
    }

    public ActionType getActionType() {
        return actionType;
    }

    public void setActionType(ActionType actionType) {
        this.actionType = actionType;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    public Double getLastValue() {
        return lastValue;
    }

    public void setLastValue(Double lastValue) {
        this.lastValue = lastValue;
    }

    @Override
    public String toString() {
        return "Request{" +
                "actionType=" + actionType +
                ", value=" + value +
                ", lastValue=" + lastValue +
                '}';
    }
}

