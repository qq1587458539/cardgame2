package com.mythostrike;

import com.mythostrike.account.repository.User;
import com.mythostrike.account.service.UserService;
import com.mythostrike.controller.AuthenticationController;
import com.mythostrike.controller.LobbyController;
import com.mythostrike.controller.message.authentication.UserAuthRequest;
import com.mythostrike.controller.message.lobby.ChangeModeRequest;
import com.mythostrike.controller.message.lobby.CreateLobbyRequest;
import com.mythostrike.model.LobbyData;
import com.mythostrike.model.SimplePrincipal;
import com.mythostrike.model.SimpleStompFrameHandler;
import com.mythostrike.model.lobby.Lobby;
import com.mythostrike.model.lobby.ModeList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.security.Principal;
import java.util.List;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertFalse;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class GameRunIntegrationTest {

    @LocalServerPort
    private Integer port;

    private String webSocketPath;
    private WebSocketStompClient webSocketStompClient;

    @Autowired
    private AuthenticationController authenticationController;
    @Autowired
    private LobbyController lobbyController;
    @Autowired
    private UserService userService;

    @BeforeEach
    void setup() {
        this.webSocketStompClient = new WebSocketStompClient(new SockJsClient(
            List.of(new WebSocketTransport(new StandardWebSocketClient()))));

        webSocketPath = "ws://localhost:" + port + "/updates";
    }

    /**
     * Integration tests if we can connect to the Websocket after creating a Lobby.
     * Not using rest-assuard.
     *
     * @throws Exception
     */
    @Test
    void testLobbyWebSocketConnection() throws Exception {
        webSocketStompClient.setMessageConverter(new MappingJackson2MessageConverter());

        StompSession session = webSocketStompClient
            .connectAsync(webSocketPath, new StompSessionHandlerAdapter() {
            })
            .get(1, SECONDS);


        //generate a test user and lobby to compare with.
        Principal testUserPrincipal = new SimplePrincipal("TestUser");
        try {
            authenticationController.register(new UserAuthRequest(testUserPrincipal.getName(), "TestPassword"));
        } catch (ResponseStatusException e) {
            System.out.println("User already exists, ignoring");
        }
        User testUser = userService.getUser(testUserPrincipal.getName());
        Lobby testLobby = new Lobby(1, ModeList.getModeList().getMode(5), testUser, userService);


        //subscribe to the lobby
        SimpleStompFrameHandler<LobbyData> frameHandler = new SimpleStompFrameHandler<LobbyData>(LobbyData.class);
        session.subscribe("/lobbies/1", frameHandler);

        //create the lobby and change the mode
        lobbyController.create(testUserPrincipal, new CreateLobbyRequest(1));
        lobbyController.changeMode(testUserPrincipal, new ChangeModeRequest(1, 1));

        await()
            .atMost(1, SECONDS)
            .untilAsserted(() -> assertFalse(frameHandler.getMessages().isEmpty()));
    }

    @Test
    void startGame() throws Exception {
    }
}
