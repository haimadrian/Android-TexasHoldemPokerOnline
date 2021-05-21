package org.hit.android.haim.calc.action.impl;

import org.hit.android.haim.calc.action.ActionContext;
import org.hit.android.haim.calc.action.ActionResponse;
import org.hit.android.haim.calc.action.ArithmeticAction;

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
