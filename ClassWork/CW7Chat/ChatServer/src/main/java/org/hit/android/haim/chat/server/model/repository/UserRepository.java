package org.hit.android.haim.chat.server.model.repository;

import org.hit.android.haim.chat.server.model.bean.mongo.UserImpl;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

/**
 * Exposes CRUD operations implemented by spring.<br/>
 * Do not use a repository directly. Instead, auto wire a reference of {@link org.hit.android.haim.chat.server.model.service.UserService}
 *
 * @author Haim Adrian
 * @since 14-Apr-21
 */
public interface UserRepository extends MongoRepository<UserImpl, String> {
    Optional<UserImpl> findByIdIgnoreCase(String userId);
    boolean existsByIdIgnoreCase(String userId);
}

