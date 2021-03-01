package com.example.calc.action.impl;

import com.example.calc.action.ActionContext;
import com.example.calc.action.ActionResponse;
import com.example.calc.action.ArithmeticAction;

public class Factorial implements ArithmeticAction {
    @Override
    public ActionResponse<Double> execute(ActionContext context) {
        double result;
        if (context.getValue() < 0) {
            result = Double.NaN;
        } else {
            if (context.getValue() >= 70) {
                result = Double.POSITIVE_INFINITY;
            } else {
                result = 1;
                for (int i = 1; i <= context.getValue(); i++) {
                    result *= i;
                }
            }
        }
        return new ActionResponse<>(result);
    }
}
