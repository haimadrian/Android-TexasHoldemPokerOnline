package org.hit.android.haim.chat.server.controller;

import com.fasterxml.jackson.databind.node.TextNode;
import org.hit.android.haim.chat.server.model.bean.mongo.Channel;
import org.hit.android.haim.chat.server.model.service.ChannelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * All channel RESTful web services are in this controller class.
 *
 * @author Haim Adrian
 * @since 14-Apr-21
 */
@RestController
@RequestMapping("/channel")
public class ChannelController {
    @Autowired
    private ChannelService channelService;

    @GetMapping
    public ResponseEntity<?> getAllChannels() {
        try {
            return ResponseEntity.ok(channelService.findAll());
        } catch (Throwable t) {
            return ControllerErrorHandler.returnError(t);
        }
    }

    @PostMapping
    public ResponseEntity<?> createChannel(@RequestBody Channel channel) {
        try {
            // The response will contain channel identifier
            return ResponseEntity.ok(channelService.save(channel));
        } catch (Throwable t) {
            return ControllerErrorHandler.returnError(t);
        }
    }

    @DeleteMapping("/{channelName}")
    public ResponseEntity<?> deleteChannel(@PathVariable String channelName) {
        try {
            channelService.delete(channelName);
            return ResponseEntity.ok(new TextNode(channelName));
        } catch (Throwable t) {
            return ControllerErrorHandler.returnError(t);
        }
    }

    @GetMapping("/{channelName}")
    public ResponseEntity<?> getChannel(@PathVariable String channelName) {
        try {
            if (channelName == null) {
                return ResponseEntity.badRequest().body("Channel identifier is mandatory for finding a channel");
            }

            Optional<Channel> channel = channelService.findById(channelName);
            if (channel.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok(channel.get());
        } catch (Throwable t) {
            return ControllerErrorHandler.returnError(t);
        }
    }
}

