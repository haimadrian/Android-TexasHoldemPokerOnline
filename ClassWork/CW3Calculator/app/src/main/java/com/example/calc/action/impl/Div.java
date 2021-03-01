package com.example.calc.action.impl;

import com.example.calc.action.ActionContext;
import com.example.calc.action.ActionResponse;
import com.example.calc.action.ArithmeticAction;

public class Div implements ArithmeticAction {
    @Override
    public ActionResponse<Double> execute(ActionContext context) {
        double result;
        if (context.getLastValue() == 0) {
            result = Double.NaN;
        } else {
            result = context.getValue() / context.getLastValue();
        }
        return new ActionResponse<>(result);
    }
}
