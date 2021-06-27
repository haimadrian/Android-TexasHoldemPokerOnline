package org.hit.android.haim.texasholdem.common.model.bean.game;

/**
 * A player action kind is one of: CHECK, CALL, RAISE, FOLD.
 * @author Haim Adrian
 * @since 27-Jun-21
 */
public enum PlayerActionKind {
    FOLD, CHECK, CALL, RAISE;

    /**
     * A method that validates if this action can come after another action.<br/>
     * We use this method to make sure that a player cannot CHECK after a CALL or RAISE.
     * @param another The action to check if this action can come after.
     * @return Legal or not
     */
    public boolean canComeAfter(PlayerActionKind another) {
        // Each action can come after another one according to their ordinal value, with the
        // exceptions of FOLD, which can come after any action, and CALL that can come after RAISE.
        return (another == null) ||
            (this.ordinal() >= another.ordinal()) ||
            ((this == CALL) && (another == RAISE)) ||
            (this == FOLD);
    }
}

