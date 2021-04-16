package org.hit.android.haim.chat.server.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hit.android.haim.chat.server.model.bean.Gender;
import org.hit.android.haim.chat.server.model.bean.http.User;
import org.hit.android.haim.chat.server.model.bean.mongo.Channel;
import org.hit.android.haim.chat.server.model.bean.mongo.MessageImpl;
import org.hit.android.haim.chat.server.model.bean.mongo.UserImpl;
import org.hit.android.haim.chat.server.model.repository.ChannelRepository;
import org.hit.android.haim.chat.server.model.repository.MessageRepository;
import org.hit.android.haim.chat.server.model.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author Haim Adrian
 * @since 14-Apr-21
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class UserControllerTest {
    private static final MediaType APPLICATION_JSON_UTF8 = new MediaType(MediaType.APPLICATION_JSON.getType(), MediaType.APPLICATION_JSON.getSubtype(), StandardCharsets.UTF_8);
    private final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ChannelRepository channelRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private MockMvc mockMvc;

    private Channel channel1;
    private Channel channel2;
    private String messageId;

    @BeforeEach
    void setUp() {
        List<String> users = new ArrayList<>();
        List<String> messages = new ArrayList<>();
        channel1 = Channel.builder().name("TestChannel1").users(users).messages(messages).build();
        channel2 = Channel.builder().name("TestChannel2").users(new ArrayList<>()).messages(new ArrayList<>()).build();

        UserImpl user1 = UserImpl.builder()
            .id("charmander@pokemon.com")
            .name("Charmander")
            .channelName(channel1.getName())
            .dateOfBirth(LocalDate.of(1995, 8, 30))
            .gender(Gender.Female).build();
        UserImpl user2 = UserImpl.builder()
            .id("charizard@pokemon.com")
            .name("Charizard")
            .channelName(channel1.getName())
            .dateOfBirth(LocalDate.of(1993, 11, 15))
            .gender(Gender.Male).build();
        users.add(user1.getId());
        users.add(user2.getId());

        messageId = UUID.randomUUID().toString();
        messageRepository.save(MessageImpl.builder().id(messageId).userId(user2.getId()).channelName(channel1.getName()).message("Ya Malik").build());
        messages.add(messageId);

        userRepository.save(user1);
        userRepository.save(user2);
        channel1 = channelRepository.save(channel1);
        channel2 = channelRepository.save(channel2);
    }

    @AfterEach
    void tearDown() {
        messageRepository.deleteById(messageId);
        userRepository.deleteById("charmander@pokemon.com");
        userRepository.deleteById("charizard@pokemon.com");
        userRepository.deleteById("effi@pokemon.com");
        channelRepository.deleteById("TestChannel1");
        channelRepository.deleteById("TestChannel2");
    }

    @Test
    void testConnect_userDoesNotExist_connectSuccess() throws Exception {
        User user = User.builder()
            .id("effi@pokemon.com")
            .name("King Profus")
            .channel(channel2)
            .dateOfBirth(LocalDate.of(1984, 1, 1))
            .gender(Gender.Male).build();
        String json = mapper.writeValueAsString(user);

        mockMvc.perform(put("/user/connect").contentType(APPLICATION_JSON_UTF8).content(json))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value("effi@pokemon.com"))
            .andExpect(jsonPath("$.name").value("King Profus"))
            .andExpect(jsonPath("$.dateOfBirth").value("1984-01-01"));

        assertThat(channelRepository.findById(channel2.getName()).get().getUsers().size()).isEqualTo(1);
    }

    @Test
    void testConnect_userMoveToOtherChannel_connectSuccess() throws Exception {
        User user = User.builder()
            .id("charmander@pokemon.com")
            .name("Charmander")
            .channel(channel2)
            .dateOfBirth(LocalDate.of(1995, 8, 30))
            .gender(Gender.Female).build();
        String json = mapper.writeValueAsString(user);

        mockMvc.perform(put("/user/connect").contentType(APPLICATION_JSON_UTF8).content(json))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value("charmander@pokemon.com"))
            .andExpect(jsonPath("$.name").value("Charmander"));

        assertThat(channelRepository.findById(channel1.getName()).get().getUsers().size()).isEqualTo(1); // Left the channel so decreased from 2 to 1
        assertThat(channelRepository.findById(channel2.getName()).get().getUsers().size()).isEqualTo(1); // Joined channel so increased from 0 to 1
    }

    @Test
    void testConnect_userAlreadyExists_connectFail() throws Exception {
        User user = User.builder()
            .id("charizard@pokemon.com")
            .name("Charizard")
            .channel(new Channel("NotExistingChannel", null, null, false))
            .dateOfBirth(LocalDate.of(1993, 11, 15))
            .gender(Gender.Male).build();
        String json = mapper.writeValueAsString(user);

        mockMvc.perform(put("/user/connect").contentType(APPLICATION_JSON_UTF8).content(json))
            .andExpect(status().isBadRequest());
    }

    @Test
    void testConnect_emptyUserId_connectFail() throws Exception {
        User user = User.builder()
            .id("")
            .name("Charizard")
            .channel(channel1)
            .dateOfBirth(LocalDate.of(1993, 11, 15))
            .gender(Gender.Male).build();
        String json = mapper.writeValueAsString(user);

        mockMvc.perform(put("/user/connect").contentType(APPLICATION_JSON_UTF8).content(json))
            .andExpect(status().isBadRequest());
    }

    @Test
    void testUserInfo_correctUserToken_success() throws Exception {
        mockMvc.perform(get("/user/info/charmander@pokemon.com"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value("charmander@pokemon.com"))
            .andExpect(jsonPath("$.name").value("Charmander"))
            .andExpect(jsonPath("$.dateOfBirth").value("1995-08-30"));
    }
}
