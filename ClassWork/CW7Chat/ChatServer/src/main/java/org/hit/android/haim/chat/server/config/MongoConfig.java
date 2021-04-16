package org.hit.android.haim.chat.server.config;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.hit.android.haim.chat.server.model.repository.ChannelRepository;
import org.hit.android.haim.chat.server.model.repository.MessageRepository;
import org.hit.android.haim.chat.server.model.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import java.util.Collection;
import java.util.Collections;

/**
 * @author Haim Adrian
 * @since 14-Apr-21
 */
@Configuration
@EnableMongoRepositories(basePackageClasses = { ChannelRepository.class, UserRepository.class, MessageRepository.class })
public class MongoConfig extends AbstractMongoClientConfiguration {
    private static final String MONGO_DB_URL = "%s@chat.aeiil.mongodb.net";
    private static final String MONGO_DB_NAME = "chat";

    @Value("${spring.datasource.username}")
    private String mongoUser;

    @Value("${spring.datasource.password}")
    private String mongoPwd;

    @Override
    protected String getDatabaseName() {
        return MONGO_DB_NAME;
    }

    @Override
    public MongoClient mongoClient() {
        String urlWithUserAndPass = String.format(MONGO_DB_URL, mongoUser + ":" + mongoPwd);
        ConnectionString connectionString = new ConnectionString("mongodb+srv://" + urlWithUserAndPass + "/" + MONGO_DB_NAME + "?retryWrites=true&w=majority");
        MongoClientSettings mongoClientSettings = MongoClientSettings.builder().applyConnectionString(connectionString).build();
        return MongoClients.create(mongoClientSettings);
    }

    @Override
    public Collection<String> getMappingBasePackages() {
        return Collections.singleton("org.hit.android.haim.chat.server.model.bean");
    }
}

