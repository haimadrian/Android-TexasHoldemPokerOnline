package org.hit.android.haim.texasholdem.common.model.bean.game;

import org.hit.android.haim.texasholdem.common.model.game.Chips;
import org.hit.android.haim.texasholdem.common.util.JsonUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author Haim Adrian
 * @since 04-Jul-21
 */
public class PlayerActionTest {
    private static final String NAME = "Goku";
    private static final long CHIPS = 10;

    @Test
    public void testPlayerActionToJson_checkAction() {
        Throwable t = null;
        PlayerAction playerAction = null;
        try {
            PlayerAction playerAction1 = PlayerAction.builder().name(NAME).actionKind(PlayerActionKind.CHECK).build();
            String json = JsonUtils.writeValueAsString(playerAction1);
            System.out.println(json);

            playerAction = JsonUtils.readValueFromString(json, PlayerAction.class);
            System.out.println(playerAction);
        } catch (Exception e) {
            t = e;
            e.printStackTrace();
        }

        Assertions.assertNull(t, "No exception supposed to occur");
        Assertions.assertNotNull(playerAction, "Json supposed to be deserialized");
        Assertions.assertEquals(PlayerActionKind.CHECK, playerAction.getActionKind(), "We have executed CHECK");
        Assertions.assertEquals(NAME, playerAction.getName(), "Received wrong name");
    }

    @Test
    public void testPlayerActionToJson_foldAction() {
        Throwable t = null;
        PlayerAction playerAction = null;
        try {
            PlayerAction playerAction1 = PlayerAction.builder().name(NAME).actionKind(PlayerActionKind.FOLD).build();
            String json = JsonUtils.writeValueAsString(playerAction1);
            System.out.println(json);

            playerAction = JsonUtils.readValueFromString(json, PlayerAction.class);
            System.out.println(playerAction);
        } catch (Exception e) {
            t = e;
            e.printStackTrace();
        }

        Assertions.assertNull(t, "No exception supposed to occur");
        Assertions.assertNotNull(playerAction, "Json supposed to be deserialized");
        Assertions.assertEquals(PlayerActionKind.FOLD, playerAction.getActionKind(), "We have executed FOLD");
        Assertions.assertEquals(NAME, playerAction.getName(), "Received wrong name");
    }

    @Test
    public void testPlayerActionToJson_callActionWithNoChips() {
        Throwable t = null;
        PlayerAction playerAction = null;
        try {
            PlayerAction playerAction1 = PlayerAction.builder().name(NAME).actionKind(PlayerActionKind.CALL).build();
            String json = JsonUtils.writeValueAsString(playerAction1);
            System.out.println(json);

            playerAction = JsonUtils.readValueFromString(json, PlayerAction.class);
            System.out.println(playerAction);
        } catch (Exception e) {
            t = e;
            e.printStackTrace();
        }

        Assertions.assertNull(t, "No exception supposed to occur");
        Assertions.assertNotNull(playerAction, "Json supposed to be deserialized");
        Assertions.assertEquals(PlayerActionKind.CALL, playerAction.getActionKind(), "We have executed CALL");
        Assertions.assertEquals(NAME, playerAction.getName(), "Received wrong name");
        Assertions.assertNotNull(playerAction.getChips(), "Chips is never null");
        Assertions.assertEquals(0, playerAction.getChips().get(), "Received wrong chips");
    }

    @Test
    public void testPlayerActionToJson_callActionWithChips() {
        Throwable t = null;
        PlayerAction playerAction = null;
        try {
            PlayerAction playerAction1 = PlayerAction.builder().name(NAME).actionKind(PlayerActionKind.CALL).chips(new Chips(CHIPS)).build();
            String json = JsonUtils.writeValueAsString(playerAction1);
            System.out.println(json);

            playerAction = JsonUtils.readValueFromString(json, PlayerAction.class);
            System.out.println(playerAction);
        } catch (Exception e) {
            t = e;
            e.printStackTrace();
        }

        Assertions.assertNull(t, "No exception supposed to occur");
        Assertions.assertNotNull(playerAction, "Json supposed to be deserialized");
        Assertions.assertEquals(PlayerActionKind.CALL, playerAction.getActionKind(), "We have executed CALL");
        Assertions.assertEquals(NAME, playerAction.getName(), "Received wrong name");
        Assertions.assertNotNull(playerAction.getChips(), "Chips is never null");
        Assertions.assertEquals(CHIPS, playerAction.getChips().get(), "Received wrong chips");
    }

    @Test
    public void testPlayerActionToJson_raiseAction() {
        Throwable t = null;
        PlayerAction playerAction = null;
        try {
            PlayerAction playerAction1 = PlayerAction.builder().name(NAME).actionKind(PlayerActionKind.RAISE).chips(new Chips(CHIPS)).build();
            String json = JsonUtils.writeValueAsString(playerAction1);
            System.out.println(json);

            playerAction = JsonUtils.readValueFromString(json, PlayerAction.class);
            System.out.println(playerAction);
        } catch (Exception e) {
            t = e;
            e.printStackTrace();
        }

        Assertions.assertNull(t, "No exception supposed to occur");
        Assertions.assertNotNull(playerAction, "Json supposed to be deserialized");
        Assertions.assertEquals(PlayerActionKind.RAISE, playerAction.getActionKind(), "We have executed CALL");
        Assertions.assertEquals(NAME, playerAction.getName(), "Received wrong name");
        Assertions.assertNotNull(playerAction.getChips(), "There were chips as part of raise");
        Assertions.assertEquals(CHIPS, playerAction.getChips().get(), "Received wrong chips");
    }
}

