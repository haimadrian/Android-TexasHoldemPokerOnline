package org.hit.android.haim.calc.action.impl;

import lombok.extern.log4j.Log4j2;
import org.hit.android.haim.calc.action.ActionContext;
import org.hit.android.haim.calc.action.ActionIfc;
import org.hit.android.haim.calc.action.ActionResponse;
import org.hit.android.haim.calc.model.DbAccess;

import java.sql.PreparedStatement;

@Log4j2
public class CreateUser implements ActionIfc<String> {
    @Override
    public ActionResponse<String> execute(ActionContext context) {
        String[] args = context.getDynamicString().split("##");
        try (PreparedStatement ps = DbAccess.getInstance().preparedStatement("insert into android.user values (?, ?, ?)")) {
            ps.setString(1, args[0]);
            ps.setString(2, args[1]);
            ps.setString(3, args[2]);
            ps.execute();
        } catch (Exception e) {
            log.error("Error has occurred while creating user", e);
        }

        return new ActionResponse<>("User " + args[0] + " created successfully");
    }
}
