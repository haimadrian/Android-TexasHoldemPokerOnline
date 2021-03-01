package com.example.calc.action.impl;

import com.example.calc.action.ActionContext;
import com.example.calc.action.ActionResponse;
import com.example.calc.action.ArithmeticAction;

public class Ln implements ArithmeticAction {
    @Override
    public ActionResponse<Double> execute(ActionContext context) {
        return new ActionResponse<>(Math.log(context.getValue()));
    }
}
