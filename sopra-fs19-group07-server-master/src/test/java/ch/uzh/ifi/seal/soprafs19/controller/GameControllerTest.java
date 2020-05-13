package ch.uzh.ifi.seal.soprafs19.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ch.uzh.ifi.seal.soprafs19.constant.Godcard;
import ch.uzh.ifi.seal.soprafs19.constant.TurnValue;
import ch.uzh.ifi.seal.soprafs19.entity.Game;
import ch.uzh.ifi.seal.soprafs19.entity.Player;
import ch.uzh.ifi.seal.soprafs19.entity.User;
import ch.uzh.ifi.seal.soprafs19.repository.GameRepository;
import ch.uzh.ifi.seal.soprafs19.repository.UserRepository;
import ch.uzh.ifi.seal.soprafs19.service.GameService;
import ch.uzh.ifi.seal.soprafs19.service.UserService;
import net.minidev.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@EnableWebMvc
public class GameControllerTest {

    @Qualifier("gameRepository")
    @Autowired
    private GameRepository gameRepository;

    @Qualifier("userRepository")
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private GameService gameService;

    @Autowired
    private UserService userService;

    private User testUser;
    private User testUser2;
    private Game testGame;

    @Before
    public void setUp() throws Exception {
        testUser = new User();
        testUser.setUsername("testUsername");
        testUser.setPassword("testPassword");
        testUser = userService.createUser(testUser);

        testUser2 = new User();
        testUser2.setUsername("testUsername2");
        testUser2.setPassword("testPassword2");
        testUser2 = userService.createUser(testUser2);

        testGame = new Game();
        testGame.setGameName("testName");
        testGame.setWithGodcards(true);
        testGame.addPlayer(new Player(testUser, 1));
        testGame = gameService.createGame(testGame);
    }

    @After
    public void end() {
        gameRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    public void allOk() throws Exception {
        this.mockMvc.perform(
                get("/games"))
                .andExpect(status().isOk());
    }

    @Test
    public void oneOk() throws Exception {
        this.mockMvc.perform(
                get("/games/{gameId}", testGame.getId()))
                .andExpect(status().isOk());
    }

    @Test
    public void oneNotOk() throws Exception {
        this.mockMvc.perform(
                get("/games/2000"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void mineOk() throws Exception {
        this.mockMvc.perform(
                get("/games/mine")
                        .header("userToken", testUser.getToken()))
                .andExpect(status().isOk());
    }

    @Test
    public void mineNotOk() throws Exception {
        this.mockMvc.perform(
                get("/games/mine")
                        .header("userToken", "invalid-token"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void createGameOk() throws Exception {
        gameRepository.deleteAll();

        JSONObject userJson = new JSONObject();
        userJson.put("gameName", "testName");
        userJson.put("player1", testUser.getToken());
        userJson.put("withGodcards", false);

        this.mockMvc.perform(
                post("/games")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(userJson.toJSONString()))
                .andExpect(status().isCreated());
    }

    @Test
    public void createGameNotOk() throws Exception {
        JSONObject userJson = new JSONObject();
        userJson.put("gameName", "testName");
        userJson.put("player1", "invalid-token");
        userJson.put("withGodcards", false);

        this.mockMvc.perform(
                post("/games")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(userJson.toJSONString()))
                .andExpect(status().isNotFound());
    }

    @Test
    public void selectGodCardOk() throws Exception {
        gameService.joinGame(testGame.getId(), testUser2.getToken());

        JSONObject userJson = new JSONObject();
        userJson.put("godcard1", "ARTEMIS");
        userJson.put("godcard2", "APOLLO");

        this.mockMvc.perform(
                put("/games/selectGodcards")
                        .header("userToken",testUser.getToken())
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(userJson.toJSONString()))
                .andExpect(status().isNoContent());
    }

    @Test
    public void selectGodCardNotOk() throws Exception {
        JSONObject userJson = new JSONObject();
        userJson.put("godcard1", "ARTEMIS");
        userJson.put("godcard2", "APOLLO");

        this.mockMvc.perform(
                put("/games/selectGodcards")
                        .header("userToken","invalid-token")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(userJson.toJSONString()))
                .andExpect(status().isNotFound());
    }

    @Test
    public void joinGameOk() throws Exception {
        this.mockMvc.perform(
                put("/games/join/{gameId}", testGame.getId())
                        .header("userToken", testUser2.getToken()))
                .andExpect(status().isOk());
    }

    @Test
    public void joinGameNotOkConflict() throws Exception {
        gameService.joinGame(testGame.getId(), testUser2.getToken());

        User testUser3 = new User();
        testUser3.setUsername("testUsername3");
        testUser3.setPassword("testPassword3");
        testUser3 = userService.createUser(testUser3);

        this.mockMvc.perform(
                put("/games/join/{gameId}", testGame.getId())
                        .header("userToken", testUser3.getToken()))
                .andExpect(status().isConflict());
    }

    @Test
    public void joinGameNotOkGameNotFound() throws Exception {
        this.mockMvc.perform(
                put("/games/join/2000")
                        .header("userToken", testUser.getToken()))
                .andExpect(status().isNotFound());
    }

    @Test
    public void joinGameNotOkUserNotFound() throws Exception {
        this.mockMvc.perform(
                put("/games/join/{gameId}", testGame.getId())
                        .header("userToken", "invalid-token"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void joinGameNotOkForbidden() throws Exception {
        this.mockMvc.perform(
                put("/games/join/{gameId}", testGame.getId())
                        .header("userToken", testUser.getToken()))
                .andExpect(status().isForbidden());
    }

    @Test
    public void exitGameOk() throws Exception {
        this.mockMvc.perform(
                put("/games/exit/{gameId}", testGame.getId())
                        .header("userToken", testUser.getToken()))
                .andExpect(status().isNoContent());
    }

    @Test
    public void exitGameNotOk() throws Exception {
        this.mockMvc.perform(
                put("/games/exit/2000")
                        .header("userToken", testUser.getToken()))
                .andExpect(status().isNotFound());
    }

    @Test
    public void startOk() throws Exception {
        gameService.joinGame(testGame.getId(), testUser2.getToken());

        this.mockMvc.perform(
                put("/games/start/{gameId}", testGame.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    public void startNotOkNotFound() throws Exception {
        this.mockMvc.perform(
                put("/games/start/1000"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void startNotOkForbidden() throws Exception {
        this.mockMvc.perform(
                put("/games/start/{gameId}", testGame.getId()))
                .andExpect(status().isForbidden());
    }

    @Test
    public void FastForwardOk() throws Exception {
        gameService.joinGame(testGame.getId(), testUser2.getToken());

        this.mockMvc.perform(
                put("/games/fast_forward/{gameId}", testGame.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    public void fastForwardNotOkNotFound() throws Exception {
        this.mockMvc.perform(
                put("/games/fast_forward/1000"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void fastForwardNotOkForbidden() throws Exception {
        this.mockMvc.perform(
                put("/games/fast_forward/{gameId}", testGame.getId()))
                .andExpect(status().isForbidden());
    }

    @Test
    public void saveGodCardOk() throws Exception {
        gameService.joinGame(testGame.getId(), testUser2.getToken());

        JSONObject userJson = new JSONObject();
        userJson.put("godcard1", "ARTEMIS");
        userJson.put("godcard2", "APOLLO");

        this.mockMvc.perform(
                put("/games/godcards")
                        .header("userToken",testUser.getToken())
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(userJson.toJSONString()))
                .andExpect(status().isOk());
    }

    @Test
    public void saveGodCardNotOk() throws Exception {
        JSONObject userJson = new JSONObject();
        userJson.put("godcard1", "ARTEMIS");
        userJson.put("godcard2", "APOLLO");

        this.mockMvc.perform(
                put("/games/godcards")
                        .header("userToken","invalid-token")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(userJson.toJSONString()))
                .andExpect(status().isNotFound());
    }

    @Test
    public void saveStarterOk() throws Exception {
        this.mockMvc.perform(
                put("/games/starter/{startPlayer}", 1)
                        .header("userToken",testUser.getToken()))
                .andExpect(status().isNoContent());
    }

    @Test
    public void saveStarterNotOk() throws Exception {
        this.mockMvc.perform(
                put("/games/starter/1")
                        .header("userToken","invalid-token"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void turnOk() throws Exception {
        gameService.joinGame(testGame.getId(), testUser2.getToken());

        this.mockMvc.perform(
                put("/games/turn/1/1")
                        .header("userToken",testUser.getToken()))
                .andExpect(status().isOk());
    }

    @Test
    public void turnNotOkForbidden() throws Exception {
        testGame.getPlayer(1).setTurnValue(TurnValue.SETWORKER);
        gameRepository.save(testGame);
        gameService.joinGame(testGame.getId(), testUser2.getToken());
        gameService.turn(testUser.getToken(), 1, 1);

        this.mockMvc.perform(
                put("/games/turn/1/1")
                        .header("userToken",testUser.getToken()))
                .andExpect(status().isForbidden());
    }

    @Test
    public void turnNotOkNotFound() throws Exception {
        this.mockMvc.perform(
                put("/games/turn/1/1")
                        .header("userToken","invalid-token"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void nextTurnOk() throws Exception {
        gameService.joinGame(testGame.getId(), testUser2.getToken());

        JSONObject userJson = new JSONObject();
        userJson.put("turnValue", "MOVE");

        this.mockMvc.perform(
                put("/games/next")
                        .header("userToken",testUser.getToken())
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(userJson.toJSONString()))
                .andExpect(status().isOk());
    }

    @Test
    public void nextTurnNotOk() throws Exception {
        JSONObject userJson = new JSONObject();
        userJson.put("turnValue", "MOVE");

        this.mockMvc.perform(
                put("/games/next")
                        .header("userToken","invalid-token")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(userJson.toJSONString()))
                .andExpect(status().isNotFound());
    }

    @Test
    public void hephaestusTurnOk() throws Exception {
        gameService.joinGame(testGame.getId(), testUser2.getToken());
        testGame = gameRepository.findByUserToken(testUser.getToken());
        testGame.getPlayer(1).setGodCard(Godcard.HEPHAESTUS);
        testGame.getPlayer(2).setTurnValue(TurnValue.END);
        testGame.getField(2, 2).setWorker(testGame.getPlayer(1).getWorker(11));
        testGame.getField(0, 0).setWorker(testGame.getPlayer(1).getWorker(12));
        testGame.getField(0, 1).setWorker(testGame.getPlayer(2).getWorker(21));
        testGame.getField(0, 2).setWorker(testGame.getPlayer(2).getWorker(22));
        testGame.getPlayer(1).setChosenWorker(testGame.getField(2, 2).getWorker().getWorkerNr());
        testGame.getPlayer(1).setTurnValue(TurnValue.BUILD);
        testGame.getPlayer(1).turn(1,2); // get field (1,2) saved for building
        gameRepository.save(testGame);

        this.mockMvc.perform(
                put("/games/hephaestus/1")
                        .header("userToken",testUser.getToken()))
                .andExpect(status().isOk());
    }

    @Test
    public void hephaestusTurnNotOkForbidden() throws Exception {
        gameService.joinGame(testGame.getId(), testUser2.getToken());

        this.mockMvc.perform(
                put("/games/hephaestus/1")
                        .header("userToken",testUser.getToken()))
                .andExpect(status().isForbidden());
    }

    @Test
    public void hephaestusTurnNotOkNotFound() throws Exception {
        this.mockMvc.perform(
                put("/games/hephaestus/1")
                        .header("userToken","invalid-token"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void deleteGameOk() throws Exception {
        this.mockMvc.perform(
                delete("/games")
                        .header("userToken", testUser.getToken()))
                .andExpect(status().isNoContent());
    }

    @Test
    public void deleteGameNotOk() throws Exception {
        this.mockMvc.perform(
                delete("/games")
                        .header("userToken","invalid-token"))
                .andExpect(status().isNotFound());
    }
}
