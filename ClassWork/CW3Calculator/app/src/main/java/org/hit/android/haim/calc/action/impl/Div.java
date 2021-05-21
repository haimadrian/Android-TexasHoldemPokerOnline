package org.hit.android.haim.calc.action.impl;

import org.hit.android.haim.calc.action.ActionContext;
import org.hit.android.haim.calc.action.ActionResponse;
import org.hit.android.haim.calc.action.ArithmeticAction;

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
