package ch.uzh.ifi.seal.soprafs19.turnvalue;

import ch.uzh.ifi.seal.soprafs19.Application;
import ch.uzh.ifi.seal.soprafs19.constant.ExceptionEnum;
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
public class BuildTest {

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

            testGame.getPlayer(1).setChosenWorker(11);
            testGame.getPlayer(1).setTurnValue(TurnValue.BUILD);
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
    public void buildOk() {
        try {
            Assert.assertSame(testGame.getField(2,2).getLevel(), 0);
            testGame.getPlayer(1).turn(2, 2);
            Assert.assertSame(testGame.getField(2,2).getLevel(), 1);
            Assert.assertNull(testGame.getPlayer(1).getChosenWorker());
            Assert.assertEquals(testGame.getPlayer(1).getTurnValue(), TurnValue.END);
            Assert.assertEquals(testGame.getPlayer(2).getTurnValue(), TurnValue.MOVE);
        } catch (SantoriniException ex) {
            throw new ResponseStatusException(
                    ex.getException(), ex.toString(), ex);
        }
    }

    @Test
    public void buildNotOkLevel4() {
        try {
            testGame.getPlayer(1).turn(4, 2);
            Assert.fail();
        } catch (SantoriniException ex) {
            if (ex.getEnum()!= ExceptionEnum.TURN_NOT_ALLOWED) {
                throw new ResponseStatusException(
                        ex.getException(), ex.toString(), ex);
            }
        }
    }

    @Test
    public void buildNotOkOccupied() {
        try {
            testGame.getPlayer(1).turn(3, 3);
            Assert.fail();
        } catch (SantoriniException ex) {
            if (ex.getEnum()!= ExceptionEnum.TURN_NOT_ALLOWED) {
                throw new ResponseStatusException(
                        ex.getException(), ex.toString(), ex);
            }
        }
    }

    @Test
    public void allowedBuildOk() {
        testGame.getPlayer(1).allowedFields();
        for (Field field : testGame.getBoard()) {
            int x = field.getX();
            int y = field.getY();
            if ((x==2 && (y==1 || y==2 || y==3)) ||
                    (x==3 && y==1) ||
                    (x==4 && (y==1 || y==3))) {
                Assert.assertTrue(field.getClickable()[0]);
            } else {
                Assert.assertFalse(field.getClickable()[0]);
            }
        }
    }
}
