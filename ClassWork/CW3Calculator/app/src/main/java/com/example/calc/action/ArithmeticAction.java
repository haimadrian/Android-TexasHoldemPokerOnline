package com.example.calc.action;

public interface ArithmeticAction extends ActionIfc<Double> {
    default double executeAsDouble(ActionContext context) {
        ActionResponse<Double> response = execute(context);
        return response.getResponse();
    }
}
