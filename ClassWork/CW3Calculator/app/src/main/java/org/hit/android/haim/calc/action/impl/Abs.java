package org.hit.android.haim.calc.action.impl;

import org.hit.android.haim.calc.action.ActionContext;
import org.hit.android.haim.calc.action.ActionResponse;
import org.hit.android.haim.calc.action.ArithmeticAction;

public class Abs implements ArithmeticAction {
    @Override
    public ActionResponse<Double> execute(ActionContext context) {
        return new ActionResponse<>(Math.abs(context.getValue()));
    }
}
