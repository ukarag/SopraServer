package ch.uzh.ifi.seal.soprafs19.turnvalue;

import ch.uzh.ifi.seal.soprafs19.Application;
import ch.uzh.ifi.seal.soprafs19.constant.ExceptionEnum;
import ch.uzh.ifi.seal.soprafs19.constant.TurnValue;
import ch.uzh.ifi.seal.soprafs19.entity.Field;
import ch.uzh.ifi.seal.soprafs19.entity.Game;
import ch.uzh.ifi.seal.soprafs19.entity.Player;
import ch.uzh.ifi.seal.soprafs19.entity.User;
import ch.uzh.ifi.seal.soprafs19.exceptions.*;
import ch.uzh.ifi.seal.soprafs19.repository.GameRepository;
import ch.uzh.ifi.seal.soprafs19.repository.UserRepository;
import ch.uzh.ifi.seal.soprafs19.service.GameService;
import ch.uzh.ifi.seal.soprafs19.service.UserService;
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
public class SetWorkerTest {

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

    private User testUser;
    private Game testGame;

    @Before
    public void setUp() {
        try {
            testUser = new User();
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
    public void setWorkerOk() {
        try {
            testGame.getPlayer(1).turn(1, 1);
            Assert.assertNotNull(testGame.getField(1, 1).getWorker());
            Assert.assertNotNull(testGame.getPlayer(1).getWorker(11).getField());
            Assert.assertEquals(testGame.getField(1, 1).getWorker(), testGame.getPlayer(1).getWorker(11));
        } catch (SantoriniException ex) {
            throw new ResponseStatusException(
                    ex.getException(), ex.toString(), ex);
        }
    }

    @Test
    public void setWorkerNotOk() {
        try {
            testGame.getPlayer(1).turn(1, 1);
            testGame.getPlayer(1).turn(1, 1);
            Assert.fail();
        } catch (SantoriniException ex) {
            if (ex.getEnum()!= ExceptionEnum.TURN_NOT_ALLOWED) {
                throw new ResponseStatusException(
                        ex.getException(), ex.toString(), ex);
            }
        }
    }

    @Test
    public void allowedSetWorkersOk() {
        try {
            testGame.getPlayer(1).allowedFields();
            for (Field field: testGame.getBoard()) {
                Assert.assertTrue(field.getClickable()[0]);
            }

            testGame.getPlayer(1).turn(3, 2);
            testGame.getPlayer(1).allowedFields();
            gameRepository.save(testGame);
            testGame = gameRepository.findByUserToken(testUser.getToken());
            for (Field field: testGame.getBoard()) {
                if (field.getX()==3 && field.getY()==2) {
                    Assert.assertFalse(field.getClickable()[0]);
                } else {
                    Assert.assertTrue(field.getClickable()[0]);
                }
            }
        } catch (SantoriniException ex) {
            throw new ResponseStatusException(
                    ex.getException(), ex.toString(), ex);
        }
    }
}
