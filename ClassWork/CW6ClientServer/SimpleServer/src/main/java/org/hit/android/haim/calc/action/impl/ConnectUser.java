package org.hit.android.haim.calc.action.impl;

import lombok.extern.log4j.Log4j2;
import org.hit.android.haim.calc.action.ActionContext;
import org.hit.android.haim.calc.action.ActionIfc;
import org.hit.android.haim.calc.action.ActionResponse;
import org.hit.android.haim.calc.model.DbAccess;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

@Log4j2
public class ConnectUser implements ActionIfc<String> {
    @Override
    public ActionResponse<String> execute(ActionContext context) {
        String result;
        String[] args = context.getDynamicString().split("##");
        try (PreparedStatement ps = DbAccess.getInstance().preparedStatement("select * from android.user where mail like ?")) {
            ps.setString(1, args[0]);
            ps.execute();

            try (ResultSet resultSet = ps.getResultSet()) {
                if (resultSet.next()) {
                    String pwd = resultSet.getString("pwd");
                    if (args[1].equals(pwd)) {
                        result = "User " + args[0] + " connected successfully";
                    } else {
                        result = "Wrong password";
                    }
                } else {
                    result = "User " + args[0] + " does not exist";
                }
            }
        } catch (Exception e) {
            result = "Failed to connect: " + e.getMessage();
            log.error("Error has occurred while connecting user", e);
        }

        return new ActionResponse<>(result);
    }
}
