package com.example.calc.action.impl;

import com.example.calc.action.ActionContext;
import com.example.calc.action.ActionResponse;
import com.example.calc.action.ArithmeticAction;

public class SquareRoot implements ArithmeticAction {
    @Override
    public ActionResponse<Double> execute(ActionContext context) {
        double result;
        if (context.getValue() < 0) {
            result = Double.NaN;
        } else {
            result = Math.sqrt(context.getValue());
        }
        return new ActionResponse<>(result);
    }
}
