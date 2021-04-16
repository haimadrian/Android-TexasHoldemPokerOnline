package org.hit.android.haim.chat.server.model.repository;

import org.hit.android.haim.chat.server.model.bean.mongo.Channel;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

/**
 * Exposes CRUD operations implemented by spring.<br/>
 * Do not use a repository directly. Instead, auto wire a reference of {@link org.hit.android.haim.chat.server.model.service.ChannelService}
 *
 * @author Haim Adrian
 * @since 14-Apr-21
 */
public interface ChannelRepository extends MongoRepository<Channel, String> {
    List<Channel> findByUsersIgnoreCase(String userId);
}

