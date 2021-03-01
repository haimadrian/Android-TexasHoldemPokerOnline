package com.example.calc.action;

public interface ActionIfc<T> {
    ActionResponse<T> execute(ActionContext context);
}
