package org.hit.android.haim.chat.server.controller;

import com.fasterxml.jackson.databind.node.TextNode;
import org.hit.android.haim.chat.server.model.bean.http.User;
import org.hit.android.haim.chat.server.model.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * All user RESTful web services are in this controller class.
 *
 * @author Haim Adrian
 * @since 14-Apr-21
 */
@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;

    @PutMapping("/connect")
    public ResponseEntity<?> connect(@RequestBody User user) {
        try {
            return ResponseEntity.ok(userService.save(user));
        } catch (Throwable t) {
            return ControllerErrorHandler.returnError(t);
        }
    }

    @PutMapping("/disconnect/{userId}")
    public ResponseEntity<?> disconnect(@PathVariable String userId) {
        try {
            userService.findById(userId).ifPresent(user -> {
                user.setChannel(null);
                userService.save(user);
            });

            return ResponseEntity.ok(new TextNode("Good Bye"));
        } catch (Throwable t) {
            return ControllerErrorHandler.returnError(t);
        }
    }

    @GetMapping("/info/{userId}")
    public ResponseEntity<?> userInfo(@PathVariable String userId) {
        try {
            if ((userId == null) || userId.isBlank()) {
                return ResponseEntity.notFound().build();
            }

            Optional<User> userEntity = userService.findById(userId);

            // In case user does not exist in our repository, return a bad request.
            if (userEntity.isEmpty()) {
                return ResponseEntity.badRequest().body("User does not exist");
            } else {
                return ResponseEntity.ok(userEntity.get());
            }
        } catch (Throwable t) {
            return ControllerErrorHandler.returnError(t);
        }
    }

    @GetMapping("/info")
    public ResponseEntity<?> usersInfo(@RequestBody List<String> usersId) {
        try {
            if (usersId == null) {
                return ResponseEntity.notFound().build();
            }

            List<User> users = userService.findByIds(usersId);
            return ResponseEntity.ok(users);
        } catch (Throwable t) {
            return ControllerErrorHandler.returnError(t);
        }
    }
}

