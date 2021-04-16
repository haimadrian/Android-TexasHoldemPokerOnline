package org.hit.android.haim.chat.server.model.service;

import org.hit.android.haim.chat.server.model.bean.mongo.Channel;
import org.hit.android.haim.chat.server.model.repository.ChannelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Optional;

/**
 * Wrap the access to the repository, so we can add additional logic between controller and repository.</br>
 * For example, we use a cache for better performance, so it is done in the service, rather than repository.
 *
 * @author Haim Adrian
 * @since 14-Apr-21
 */
@Component
public class ChannelService {
    @Autowired
    private ChannelRepository channelRepository;

    /**
     * See {@link ChannelRepository#findAll()}
     */
    public Iterable<Channel> findAll() {
        return channelRepository.findAll();
    }

    /**
     * See {@link ChannelRepository#findById(Object)}
     */
    @CachePut(value = "channelCache", key = "#name", condition = "#result != null") // Cache the results of this method.
    public Optional<Channel> findById(String name) {
        return channelRepository.findById(name);
    }

    /**
     * See {@link ChannelRepository#findByUsersIgnoreCase(String)}
     */
    public Collection<Channel> findByUserId(String userId) {
        return channelRepository.findByUsersIgnoreCase(userId);
    }

    /**
     * See {@link ChannelRepository#save(Object)}
     */
    @CacheEvict(value = "channelCache", key = "#result.name") // When we save, we want to remove item from cache, in order to have the up to date item in the cache.
    public Channel save(Channel channel) {
        if ((channel == null) || (channel.getName() == null)) {
            throw new IllegalArgumentException("Channel must have a name. Was: " + channel);
        }

        Optional<Channel> existingChannel = findById(channel.getName());
        if (existingChannel.isPresent()) {
            throw new IllegalArgumentException("Channel with name " + channel.getName() + " already exists.");
        }

        return channelRepository.save(channel);
    }

    /**
     * See {@link ChannelRepository#save(Object)}
     */
    @CacheEvict(value = "channelCache", key = "#result.name") // When we save, we want to remove item from cache, in order to have the up to date item in the cache.
    public Channel update(Channel channel) {
        if ((channel == null) || (channel.getName() == null)) {
            throw new IllegalArgumentException("Channel must have a name. Was: " + channel);
        }

        Optional<Channel> existingChannel = findById(channel.getName());
        if (existingChannel.isPresent()) {
            return channelRepository.save(channel);
        } else {
            throw new IllegalArgumentException("Channel not found. Was: " + channel.getName());
        }
    }

    /**
     * See {@link ChannelRepository#deleteById(Object)}
     */
    @CacheEvict(value = "channelCache", key = "#channelName") // When we save, we want to remove item from cache, in order to have the up to date item in the cache.
    public void delete(String channelName) {
        findById(channelName).ifPresent(channel -> {
            if (channel.isDeletable()) {
                channelRepository.deleteById(channelName);
            } else {
                throw new IllegalArgumentException("Cannot delete channels that marked as non-deletable");
            }
        });
    }
}

