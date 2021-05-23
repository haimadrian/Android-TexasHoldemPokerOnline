package org.hit.android.haim.texasholdem.server.model.bean.game;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * A lightweight version of {@link org.hit.android.haim.texasholdem.server.model.bean.user.User}<br/>
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
    private long chips;

    /**
     * Tells whether player is in or out
     * Players can be out if they fold or went all-in. In case player went all-in, he is still
     * part of the match, but we will skip him when there are bet rounds.
     */
    private boolean isPlaying;

    /**
     * The {@link Hand} this player holds
     */
    private Hand hand;

    /**
     * The position of the player on a table.<br/>
     * We sort players by position, so the player iterator will be able to traverse players by their sitting order
     */
    private int position;
}

