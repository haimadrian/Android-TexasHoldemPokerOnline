package org.hit.android.haim.chat.server.model.service;

import org.hit.android.haim.chat.server.model.bean.http.User;
import org.hit.android.haim.chat.server.model.bean.mongo.Channel;
import org.hit.android.haim.chat.server.model.bean.mongo.UserImpl;
import org.hit.android.haim.chat.server.model.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Wrap the access to the repository, so we can add additional logic between controller and repository.</br>
 * For example, we use a cache for better performance, so it is done in the service, rather than repository.
 *
 * @author Haim Adrian
 * @since 14-Apr-21
 */
@Component
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ChannelService channelService;

    @Autowired
    private MessageService messageService;

    /**
     * See {@link UserRepository#findById(Object)}
     */
    @CachePut(value = "userCache", key = "#id", condition = "#result != null") // Cache the results of this method.
    public Optional<User> findById(String id) {
        Optional<UserImpl> result = userRepository.findByIdIgnoreCase(id);
        if (result.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(userImplToUser(result.get()));
    }

    public List<User> findByIds(List<String> ids) {
        List<User> result = new ArrayList<>();

        // Use findById in order to use the cache for multiple results
        ids.forEach(id -> findById(id).ifPresent(result::add));

        return result;
    }

    /**
     * See {@link UserRepository#save(Object)}<br/>
     */
    @CacheEvict(value = "userCache", key = "#user.id") // When we save, we want to remove item from cache, in order to have the up to date item in the cache.
    public User save(User user) {
        if ((user == null) || (user.getId() == null) || user.getId().isBlank()) {
            throw new IllegalArgumentException("Missing user details");
        }

        Optional<Channel> newChannel = Optional.empty();

        // When there is no channel, it means user disconnected, so there is no "new channel"
        if ((user.getChannel() != null) && (user.getChannel().getName() != null)) {
            newChannel = channelService.findById(user.getChannel().getName());
            if (newChannel.isEmpty()) {
                throw new IllegalArgumentException("Channel does not exist: " + user.getChannel().getName());
            }

            user.setChannel(newChannel.get());
        }

        // Keep old channel name, to check if there is a difference in channels so we will update when necessary
        Optional<UserImpl> existingUser = userRepository.findByIdIgnoreCase(user.getId());
        String oldChannelName = null;
        if (existingUser.isPresent()) {
            oldChannelName = existingUser.get().getChannelName();
        }

        UserImpl userEntity = userRepository.save(userToUserImpl(user));

        if ((userEntity.getChannelName() == null) || !userEntity.getChannelName().equals(oldChannelName)) {
            // Update all channels that user was previously connected to, that the user disconnected
            channelService.findByUserId(userEntity.getId()).forEach(channel -> {
                if (channel.getUsers().removeIf(userId -> userId.equalsIgnoreCase(userEntity.getId()))) {
                    channelService.update(channel);
                }
            });

            // Update the channel that the user connects to
            if (newChannel.isPresent()) {
                newChannel.get().getUsers().add(userEntity.getId());
                channelService.update(newChannel.get());
            }
        }

        return user;
    }

    /**
     * See {@link UserRepository#delete(Object)}<br/>
     */
    @CacheEvict(value = "userCache", key = "#userId", condition = "#userId != null") // When we save, we want to remove item from cache, in order to have the up to date item in the cache.
    public void delete(String userId) {
        if ((userId == null) || userId.isBlank()) {
            throw new IllegalArgumentException("Illegal user identifier: " + userId);
        }

        Optional<UserImpl> userEntity = userRepository.findById(userId);

        // In case user does not exist in our repository, return a bad request.
        if (userEntity.isEmpty()) {
            throw new IllegalArgumentException("User does not exist: " + userId);
        } else {
            userRepository.delete(userEntity.get());

            // Update all channels that user was previously connected to, that the user is deleted
            channelService.findByUserId(userId).forEach(channel -> {
                if (channel.getUsers().removeIf(userId1 -> userId1.equalsIgnoreCase(userId))) {
                    channelService.update(channel);
                }
            });
            messageService.findByUserId(userId).forEach(message -> {
                messageService.delete(message.getId());
            });
        }
    }

    private User userImplToUser(UserImpl user) {
        return User.builder()
            .id(user.getId())
            .name(user.getName())
            .dateOfBirth(user.getDateOfBirth())
            .gender(user.getGender())
            .channel(user.getChannelName() == null ? null : channelService.findById(user.getChannelName()).orElseGet(() -> Channel.builder().name(user.getChannelName()).build()))
            .build();
    }

    private UserImpl userToUserImpl(User user) {
        return UserImpl.builder()
            .id(user.getId())
            .name(user.getName())
            .dateOfBirth(user.getDateOfBirth())
            .gender(user.getGender())
            .channelName(user.getChannel() == null ? null : user.getChannel().getName())
            .build();
    }
}

