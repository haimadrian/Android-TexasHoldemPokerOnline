package org.hit.android.haim.texasholdem.server.controller;

import org.hit.android.haim.texasholdem.server.model.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * All message RESTful web services are in this controller class.
 *
 * @author Haim Adrian
 * @since 14-Apr-21
 */
@RestController
@RequestMapping("/message")
public class MessageController {
    @Autowired
    private MessageService messageService;

    @PostMapping("/{channelName}/{userId}")
    public ResponseEntity<?> sendMessage(@PathVariable("channelName") String channelName, @PathVariable("userId") String userId, @RequestBody String content) {
        try {
            return ResponseEntity.ok(messageService.sendMessage(channelName, userId, content));
        } catch (Throwable t) {
            return ControllerErrorHandler.handleServerError(t);
        }
    }

    @GetMapping("/{channelName}")
    public ResponseEntity<?> getAllMessagesInChannel(@PathVariable("channelName") String channelName) {
        try {
            return ResponseEntity.ok(messageService.findByChannelName(channelName));
        } catch (Throwable t) {
            return ControllerErrorHandler.handleServerError(t);
        }
    }

    @GetMapping("/{channelName}/since/{lastMessageDateTime}")
    public ResponseEntity<?> getLatestMessagesInChannel(@PathVariable("channelName") String channelName, @PathVariable("lastMessageDateTime") String lastMessageDateTime) {
        try {
            return ResponseEntity.ok(messageService.findLatestByChannelName(channelName, LocalDateTime.parse(lastMessageDateTime)));
        } catch (Throwable t) {
            return ControllerErrorHandler.handleServerError(t);
        }
    }
}

