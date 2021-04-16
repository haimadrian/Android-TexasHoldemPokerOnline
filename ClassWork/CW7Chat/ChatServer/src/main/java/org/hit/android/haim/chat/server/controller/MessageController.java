package org.hit.android.haim.chat.server.controller;

import org.hit.android.haim.chat.server.model.bean.http.Message;
import org.hit.android.haim.chat.server.model.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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

    @GetMapping("/info/{messageId}")
    public ResponseEntity<?> messageInfo(@PathVariable String messageId) {
        try {
            if ((messageId == null) || messageId.isBlank()) {
                return ResponseEntity.notFound().build();
            }

            Optional<Message> messageEntity = messageService.findById(messageId);

            // In case user does not exist in our repository, return a bad request.
            if (messageEntity.isEmpty()) {
                return ResponseEntity.badRequest().body("User does not exist");
            } else {
                return ResponseEntity.ok(messageEntity.get());
            }
        } catch (Throwable t) {
            return ControllerErrorHandler.returnError(t);
        }
    }

    @GetMapping("/info")
    public ResponseEntity<?> messagesInfo(@RequestBody List<String> messagesId) {
        try {
            if (messagesId == null) {
                return ResponseEntity.notFound().build();
            }

            List<Message> messages = messageService.findByIds(messagesId);
            return ResponseEntity.ok(messages);
        } catch (Throwable t) {
            return ControllerErrorHandler.returnError(t);
        }
    }

    @PostMapping("/{channelName}/{userId}")
    public ResponseEntity<?> sendMessage(@PathVariable("channelName") String channelName, @PathVariable("userId") String userId, @RequestBody String content) {
        try {
            // The response will contain message identifier
            return ResponseEntity.ok(messageService.save(channelName, userId, content));
        } catch (Throwable t) {
            return ControllerErrorHandler.returnError(t);
        }
    }

    @GetMapping("/{channelName}")
    public ResponseEntity<?> getAllMessagesInChannel(@PathVariable("channelName") String channelName) {
        try {
            return ResponseEntity.ok(messageService.findByChannelName(channelName));
        } catch (Throwable t) {
            return ControllerErrorHandler.returnError(t);
        }
    }

    @GetMapping("/{channelName}/since/{lastMessageDateTime}")
    public ResponseEntity<?> getLatestMessagesInChannel(@PathVariable("channelName") String channelName, @PathVariable("lastMessageDateTime") String lastMessageDateTime) {
        try {
            return ResponseEntity.ok(messageService.findLatestByChannelName(channelName, LocalDateTime.parse(lastMessageDateTime)));
        } catch (Throwable t) {
            return ControllerErrorHandler.returnError(t);
        }
    }
}

