package ch.uzh.ifi.seal.soprafs19.godcard;

import ch.uzh.ifi.seal.soprafs19.Application;
import ch.uzh.ifi.seal.soprafs19.constant.Godcard;
import ch.uzh.ifi.seal.soprafs19.constant.TurnValue;
import ch.uzh.ifi.seal.soprafs19.entity.Field;
import ch.uzh.ifi.seal.soprafs19.entity.Game;
import ch.uzh.ifi.seal.soprafs19.entity.Player;
import ch.uzh.ifi.seal.soprafs19.entity.User;
import ch.uzh.ifi.seal.soprafs19.exceptions.*;
import ch.uzh.ifi.seal.soprafs19.repository.*;
import ch.uzh.ifi.seal.soprafs19.service.*;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.server.ResponseStatusException;

@RunWith(SpringRunner.class)
@SpringBootTest(classes= Application.class)
public class HephaestusTest {

    @Qualifier("gameRepository")
    @Autowired
    private GameRepository gameRepository;

    @Qualifier("userRepository")
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GameService gameService;

    @Autowired
    private UserService userService;

    private Game testGame;

    @Before
    public void setUp() {
        try {
            User testUser = new User();
            testUser.setUsername("testUsername");
            testUser.setPassword("testPassword");
            testUser = userService.createUser(testUser);
            Player testPlayer = new Player(testUser, 1);
            testPlayer.setGodCard(Godcard.HEPHAESTUS);
            testPlayer.setTurnValue(TurnValue.SETWORKER);

            User testUser2 = new User();
            testUser2.setUsername("testUsername2");
            testUser2.setPassword("testPassword2");
            testUser2 = userService.createUser(testUser2);

            testGame = new Game();
            testGame.setGameName("TestName");
            testGame.addPlayer(testPlayer);
            testGame.setWithGodcards(true);
            testGame = gameService.createGame(testGame);
            testGame = gameService.joinGame(testGame.getId(), testUser2.getToken());

            //setworker 4x
            testGame.getPlayer(1).turn(3, 2);
            testGame.getPlayer(1).turn(1, 2);
            testGame.getPlayer(2).turn(2, 2);
            testGame.getPlayer(2).turn(0, 1);

            testGame.getPlayer(1).setChosenWorker(11);
            testGame.getPlayer(1).setTurnValue(TurnValue.BUILD);
            testGame.getField(3,3).setLevel(3);
            testGame.getField(4,2).setLevel(2);
        } catch (SantoriniException ex) {
            throw new ResponseStatusException(
                    ex.getException(), ex.toString(), ex);
        }
    }

    @After
    public void end() {
        gameRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    public void buildOkBuildDome() {
        try {
            testGame.getPlayer(1).turn(3, 3);
            Assert.assertSame(testGame.getField(3, 3).getLevel(), 4);
            Assert.assertEquals(testGame.getPlayer(1).getTurnValue(), TurnValue.END);
        } catch (SantoriniException ex) {
            throw new ResponseStatusException(
                    ex.getException(), ex.toString(), ex);
        }
    }

    @Test
    public void buildOkBuildLevel3() {
        try {
            testGame.getPlayer(1).turn(4, 2);
            Assert.assertSame(testGame.getField(3, 3).getLevel(), 3);
            Assert.assertEquals(testGame.getPlayer(1).getTurnValue(), TurnValue.END);
        } catch (SantoriniException ex) {
            throw new ResponseStatusException(
                    ex.getException(), ex.toString(), ex);
        }
    }

    @Test
    public void buildOkBuildNotDome() {
        try {
            testGame.getPlayer(1).turn(3, 1);
            Assert.assertSame(testGame.getField(3, 1).getLevel(), 0);
            Assert.assertEquals(testGame.getPlayer(1).getTurnValue(), TurnValue.HEPHAESTUSBUILD);
        } catch (SantoriniException ex) {
            throw new ResponseStatusException(
                    ex.getException(), ex.toString(), ex);
        }
    }

    @Test
    public void hephaestusBuildOkBuildOne() {
        try {
            Player player = testGame.getPlayer(1);
            player.turn(3, 1); // get to TurnValue.HEPHAESTUSBUILD
            player.turn(0,1); // use hephaestusBuild to add 1 level
            Assert.assertSame(testGame.getField(3, 1).getLevel(), 1);
            Assert.assertEquals(testGame.getPlayer(1).getTurnValue(), TurnValue.END);
        } catch (SantoriniException ex) {
            throw new ResponseStatusException(
                    ex.getException(), ex.toString(), ex);
        }
    }

    @Test
    public void hephaestusBuildOkBuildTwice() {
        try {
            Player player = testGame.getPlayer(1);
            player.turn(3, 1); // get to TurnValue.HEPHAESTUSBUILD
            player.turn(0,2); // use hephaestusBuild to add 2 level
            Assert.assertSame(testGame.getField(3, 1).getLevel(), 2);
            Assert.assertEquals(testGame.getPlayer(1).getTurnValue(), TurnValue.END);
        } catch (SantoriniException ex) {
            throw new ResponseStatusException(
                    ex.getException(), ex.toString(), ex);
        }
    }

    @Test
    public void allowedFieldsOk() {
        testGame.getPlayer(2).setGodCard(Godcard.HEPHAESTUS);
        testGame.getPlayer(2).allowedFields();
        for (Field field : testGame.getBoard()) {
            Assert.assertFalse(field.getClickable()[1]);
        }
    }
}
