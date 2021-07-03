package org.hit.android.haim.texasholdem.common.model.bean.game;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;
import lombok.*;
import org.hit.android.haim.texasholdem.common.model.game.Chips;
import org.hit.android.haim.texasholdem.common.util.ThreadContextMap;

/**
 * A lightweight version of a user.<br/>
 * This class is used to gather users into one game, without keeping all of their data (e.g. image)
 * in order to consume less network usage
 *
 * @author Haim Adrian
 * @since 08-May-21
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Builder
@ToString(exclude = { "hand" })
public class Player {
    /**
     * The identifier of this player is the user identifier of a registered user, so we can
     * let client to fetch user info by identifier
     */
    @EqualsAndHashCode.Include
    private String id;

    /**
     * Nickname of the user
     */
    private String name;

    /**
     * How many coins this player has in a game
     */
    private Chips chips;

    /**
     * Tells whether player is in or out.<br/>
     * Players can be out if they fold only. when a player is going all-in, he is still
     * part of the match, but we will skip him when there are bet rounds. For this we will check how many chips this player got.
     */
    private boolean isPlaying;

    /**
     * The {@link Hand} this player holds
     */
    @JsonIgnore // Hide players hand from json, to avoid of revealing their secrets
    private Hand hand;

    /**
     * The position of the player on a table.<br/>
     * We sort players by position, so the player iterator will be able to traverse players by their sitting order
     */
    private int position;

    @JsonGetter("hand")
    public Hand getHand() {
        // Return Hand of current requesting user only, while other players will hide their hand.
        String playerId = ThreadContextMap.getInstance().getUserId();
        if ((playerId != null) && (playerId.equals(id))) {
            return hand;
        }

        return null;
    }

    @JsonSetter("hand")
    public void setHand(Hand hand) {
        this.hand = hand;
    }
}

