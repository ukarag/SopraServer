package ch.uzh.ifi.seal.soprafs19.turnvalue;

import ch.uzh.ifi.seal.soprafs19.Application;
import ch.uzh.ifi.seal.soprafs19.constant.ExceptionEnum;
import ch.uzh.ifi.seal.soprafs19.constant.TurnValue;
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
public class DomeTest {

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
            testPlayer.setTurnValue(TurnValue.SETWORKER);

            User testUser2 = new User();
            testUser2.setUsername("testUsername2");
            testUser2.setPassword("testPassword2");
            testUser2 = userService.createUser(testUser2);

            testGame = new Game();
            testGame.setGameName("TestName");
            testGame.addPlayer(testPlayer);
            testGame.setWithGodcards(false);
            testGame = gameService.createGame(testGame);
            testGame = gameService.joinGame(testGame.getId(), testUser2.getToken());

            //setworker 4x
            testGame.getPlayer(1).turn(3, 2);
            testGame.getPlayer(1).turn(3, 3);
            testGame.getPlayer(2).turn(0, 0);
            testGame.getPlayer(2).turn(0, 1);

            testGame.getField(4,2).setLevel(4);
            testGame.getField(4,1).setLevel(3);

            testGame.getPlayer(1).setChosenWorker(11);
            testGame.getPlayer(1).setTurnValue(TurnValue.DOME);
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
    public void buildDomeOkOn0() {
        try {
            Assert.assertSame(testGame.getField(2,2).getLevel(), 0);
            testGame.getPlayer(1).turn(2, 2);
            Assert.assertSame(testGame.getField(2,2).getLevel(), 40);
            Assert.assertNull(testGame.getPlayer(1).getChosenWorker());
            Assert.assertEquals(testGame.getPlayer(1).getTurnValue(), TurnValue.END);
            Assert.assertEquals(testGame.getPlayer(2).getTurnValue(), TurnValue.MOVE);
        } catch (SantoriniException ex) {
            throw new ResponseStatusException(
                    ex.getException(), ex.toString(), ex);
        }
    }

    @Test
    public void buildDomeOkOn1() {
        try {
            testGame.getField(2,2).setLevel(1);
            testGame.getPlayer(1).turn(2, 2);
            Assert.assertSame(testGame.getField(2,2).getLevel(), 41);
            Assert.assertNull(testGame.getPlayer(1).getChosenWorker());
            Assert.assertEquals(testGame.getPlayer(1).getTurnValue(), TurnValue.END);
            Assert.assertEquals(testGame.getPlayer(2).getTurnValue(), TurnValue.MOVE);
        } catch (SantoriniException ex) {
            throw new ResponseStatusException(
                    ex.getException(), ex.toString(), ex);
        }
    }

    @Test
    public void buildDomeOkOn2() {
        try {
            testGame.getField(2,2).setLevel(2);
            testGame.getPlayer(1).turn(2, 2);
            Assert.assertSame(testGame.getField(2,2).getLevel(), 42);
            Assert.assertNull(testGame.getPlayer(1).getChosenWorker());
            Assert.assertEquals(testGame.getPlayer(1).getTurnValue(), TurnValue.END);
            Assert.assertEquals(testGame.getPlayer(2).getTurnValue(), TurnValue.MOVE);
        } catch (SantoriniException ex) {
            throw new ResponseStatusException(
                    ex.getException(), ex.toString(), ex);
        }
    }

    @Test
    public void buildDomeOkOn3() {
        try {
            Assert.assertSame(testGame.getField(4,1).getLevel(), 3);
            testGame.getPlayer(1).turn(4, 1);
            Assert.assertSame(testGame.getField(4,1).getLevel(), 4);
            Assert.assertNull(testGame.getPlayer(1).getChosenWorker());
            Assert.assertEquals(testGame.getPlayer(1).getTurnValue(), TurnValue.END);
            Assert.assertEquals(testGame.getPlayer(2).getTurnValue(), TurnValue.MOVE);
        } catch (SantoriniException ex) {
            throw new ResponseStatusException(
                    ex.getException(), ex.toString(), ex);
        }
    }

    @Test
    public void buildDomeNotOk() {
        try {
            Assert.assertSame(testGame.getField(4,2).getLevel(), 4);
            testGame.getPlayer(1).turn(4, 2);
            Assert.fail();
        } catch (SantoriniException ex) {
            if (ex.getEnum()!= ExceptionEnum.TURN_NOT_ALLOWED) {
                throw new ResponseStatusException(
                        ex.getException(), ex.toString(), ex);
            }
        }
    }
}
