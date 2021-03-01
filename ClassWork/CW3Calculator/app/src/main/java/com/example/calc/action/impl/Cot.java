package com.example.calc.action.impl;

import com.example.calc.action.ActionContext;
import com.example.calc.action.ActionResponse;
import com.example.calc.action.ArithmeticAction;

public class Cot implements ArithmeticAction {
    @Override
    public ActionResponse<Double> execute(ActionContext context) {
        // Near PI*k, from right
        if (Math.abs(context.getValue() % Math.PI) <= 1e-15) {
            return new ActionResponse<>(context.getValue() < 0 ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY);
        }

        // Near PI*k, from left
        if ((Math.PI - Math.abs(context.getValue() % Math.PI)) <= 1e-15) {
            return new ActionResponse<>(context.getValue() < 0 ? Double.POSITIVE_INFINITY : Double.NEGATIVE_INFINITY);
        }

        return new ActionResponse<>(1.0 / Math.tan(context.getValue()));
    }
}
