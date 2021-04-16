package org.hit.android.haim.chat.server.model.repository;

import org.hit.android.haim.chat.server.model.bean.mongo.MessageImpl;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

/**
 * Exposes CRUD operations implemented by spring.<br/>
 * Do not use a repository directly. Instead, auto wire a reference of {@link org.hit.android.haim.chat.server.model.service.MessageService}
 *
 * @author Haim Adrian
 * @since 14-Apr-21
 */
public interface MessageRepository extends MongoRepository<MessageImpl, String> {
    List<MessageImpl> findByChannelName(String channelName);
    List<MessageImpl> findByUserIdIgnoreCase(String userId);
}

