package org.hit.android.haim.calc.action;

public interface ActionIfc<T> {
    ActionResponse<T> execute(ActionContext context);
}
