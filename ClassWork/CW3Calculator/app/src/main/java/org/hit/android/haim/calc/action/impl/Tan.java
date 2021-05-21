package org.hit.android.haim.calc.action.impl;

import org.hit.android.haim.calc.action.ActionContext;
import org.hit.android.haim.calc.action.ActionResponse;
import org.hit.android.haim.calc.action.ArithmeticAction;

public class Tan implements ArithmeticAction {
    @Override
    public ActionResponse<Double> execute(ActionContext context) {
        // Avoid of checking near PI. (PI also divides by PI/2, that's why we avoid of it)
        if (Math.abs(context.getValue() % Math.PI) > (Math.PI/4.0)) {
            if (Math.abs(context.getValue() % (Math.PI / 2.0)) <= 1e-15) {
                return new ActionResponse<>(context.getValue() < 0 ? Double.POSITIVE_INFINITY : Double.NEGATIVE_INFINITY);
            }

            if (((Math.PI / 2.0) - Math.abs(context.getValue() % (Math.PI / 2.0))) <= 1e-15) {
                return new ActionResponse<>(context.getValue() < 0 ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY);
            }
        }

        return new ActionResponse<>(Math.tan(context.getValue()));
    }
}
