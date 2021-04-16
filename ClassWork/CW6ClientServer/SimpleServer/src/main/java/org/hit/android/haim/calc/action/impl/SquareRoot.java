package org.hit.android.haim.calc.action.impl;

import org.hit.android.haim.calc.action.ActionContext;
import org.hit.android.haim.calc.action.ActionResponse;
import org.hit.android.haim.calc.action.ArithmeticAction;

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
