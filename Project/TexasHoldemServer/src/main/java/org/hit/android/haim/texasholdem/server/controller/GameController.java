package org.hit.android.haim.texasholdem.server.controller;

import com.fasterxml.jackson.databind.node.TextNode;
import org.hit.android.haim.texasholdem.common.model.bean.game.GameSettings;
import org.hit.android.haim.texasholdem.common.model.bean.game.Player;
import org.hit.android.haim.texasholdem.common.model.bean.game.PlayerAction;
import org.hit.android.haim.texasholdem.common.model.game.GameEngine;
import org.hit.android.haim.texasholdem.server.model.bean.user.User;
import org.hit.android.haim.texasholdem.server.model.service.GameService;
import org.hit.android.haim.texasholdem.server.security.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

import static org.hit.android.haim.texasholdem.server.config.JwtAuthenticationFilter.AUTHORIZATION_HEADER;

/**
 * @author Haim Adrian
 * @since 25-Jun-21
 */
@RestController
@RequestMapping("/game")
public class GameController {
    @Autowired
    private GameService gameService;

    @Autowired
    private JwtUtils jwtUtils;

    @PostMapping("/new")
    public ResponseEntity<?> createNewGame(@RequestHeader(AUTHORIZATION_HEADER) String jwtToken, @RequestBody GameSettings content) {
        try {
            User user = jwtUtils.parseToken(jwtToken);
            content.setCreatorId(user.getId());
            GameEngine game = gameService.createNewGame(content);
            return ResponseEntity.ok(new TextNode(game.getGameHash()));
        } catch (Throwable t) {
            return ControllerErrorHandler.handleServerError(t);
        }
    }

    @GetMapping("/mygame")
    public ResponseEntity<?> getMyGame(@RequestHeader(AUTHORIZATION_HEADER) String jwtToken) {
        try {
            User user = jwtUtils.parseToken(jwtToken);
            Optional<GameEngine> game = gameService.findByCreatorId(user.getId());
            if (game.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok(new TextNode(game.get().getGameHash()));
        } catch (Throwable t) {
            return ControllerErrorHandler.handleServerError(t);
        }
    }

    @PutMapping("/{gameHash}/start")
    public ResponseEntity<?> startGame(@RequestHeader(AUTHORIZATION_HEADER) String jwtToken, @PathVariable String gameHash) {
        try {
            User user = jwtUtils.parseToken(jwtToken);
            Optional<GameEngine> game = gameService.findByCreatorId(user.getId());
            if (game.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            if (!game.get().getGameHash().equals(gameHash)) {
                return ResponseEntity.badRequest().body("Game " + gameHash + " differs from user's game");
            }

            // Start the game
            gameService.startGame(gameHash);
            return ResponseEntity.ok().build();
        } catch (Throwable t) {
            return ControllerErrorHandler.handleServerError(t);
        }
    }

    @DeleteMapping("/{gameHash}/stop")
    public ResponseEntity<?> stopGame(@RequestHeader(AUTHORIZATION_HEADER) String jwtToken, @PathVariable String gameHash) {
        try {
            User user = jwtUtils.parseToken(jwtToken);
            Optional<GameEngine> game = gameService.findByCreatorId(user.getId());
            if (game.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            if (!game.get().getGameHash().equals(gameHash)) {
                return ResponseEntity.badRequest().body("Game " + gameHash + " differs from user's game");
            }

            // Stop the game
            gameService.stopGame(gameHash);
            return ResponseEntity.ok().build();
        } catch (Throwable t) {
            return ControllerErrorHandler.handleServerError(t);
        }
    }

    @PutMapping("/{gameHash}/join")
    public ResponseEntity<?> joinGame(@RequestHeader(AUTHORIZATION_HEADER) String jwtToken, @PathVariable String gameHash, @RequestBody Player player) {
        try {
            if (player == null) {
                return ResponseEntity.badRequest().body(new TextNode("Missing request body (Player)"));
            }

            // Make sure user id and name are set, based on the registered user details
            User user = jwtUtils.parseToken(jwtToken);
            player.setId(user.getId());
            player.setName(user.getName());

            // In case the game is active or full, an illegal argument exception will be thrown and return to client as BAD_REQUEST
            gameService.joinGame(gameHash, player);
            return ResponseEntity.ok(player); // Return player with updated position
        } catch (Throwable t) {
            return ControllerErrorHandler.handleServerError(t);
        }
    }

    @PutMapping("/{gameHash}/leave")
    public ResponseEntity<?> leaveGame(@RequestHeader(AUTHORIZATION_HEADER) String jwtToken, @PathVariable String gameHash) {
        try {
            // Make sure user id and name are set, based on the registered user details
            User user = jwtUtils.parseToken(jwtToken);
            gameService.leaveGame(gameHash, user.getId());
            return ResponseEntity.ok().build();
        } catch (Throwable t) {
            return ControllerErrorHandler.handleServerError(t);
        }
    }

    /**
     * Get the players of a game, to show them when new players joins and want to select a seat.<br/>
     * This way the user sees who sits where, and can select an available seat.
     */
    @GetMapping("/{gameHash}/players")
    public ResponseEntity<?> getPlayers(@PathVariable String gameHash) {
        try {
            Optional<GameEngine> game = gameService.findById(gameHash);
            if (game.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok(game.get().getPlayers().getPlayers());
        } catch (Throwable t) {
            return ControllerErrorHandler.handleServerError(t);
        }
    }

    @GetMapping("/{gameHash}/info")
    public ResponseEntity<?> getGameInfo(@RequestHeader(AUTHORIZATION_HEADER) String jwtToken, @PathVariable String gameHash) {
        try {
            Optional<GameEngine> game = gameService.findById(gameHash);
            if (game.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            // Make sure that only players can retrieve game info.
            User user = jwtUtils.parseToken(jwtToken);
            Player player = game.get().getPlayers().getPlayerById(user.getId());
            if (player == null) {
                return ResponseEntity.badRequest().body(new TextNode("User " + user.getId() + " is not part of this game."));
            }

            // Return game engine which contains all of the info, except sensitive data like player/deck cards.
            return ResponseEntity.ok(game.get());
        } catch (Throwable t) {
            return ControllerErrorHandler.handleServerError(t);
        }
    }

    @PutMapping("/{gameHash}/action")
    public ResponseEntity<?> executePlayerAction(@RequestHeader(AUTHORIZATION_HEADER) String jwtToken, @PathVariable String gameHash, @RequestBody PlayerAction playerAction) {
        try {
            // Make sure user id and name are set, based on the registered user details
            User user = jwtUtils.parseToken(jwtToken);
            gameService.executePlayerAction(gameHash, user.getId(), playerAction);
            return ResponseEntity.ok().build();
        } catch (Throwable t) {
            return ControllerErrorHandler.handleServerError(t);
        }
    }
}

