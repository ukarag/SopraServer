package ch.uzh.ifi.seal.soprafs19.godcard;

import ch.uzh.ifi.seal.soprafs19.Application;
import ch.uzh.ifi.seal.soprafs19.constant.Godcard;
import ch.uzh.ifi.seal.soprafs19.constant.TurnValue;
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
public class AthenaTest {

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
            testPlayer.setGodCard(Godcard.ATHENA);
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
            testGame.getPlayer(2).turn(0, 2);
            testGame.getPlayer(2).turn(0, 1);

            testGame.getField(2,2).setLevel(1);
            testGame.getField(4,1).setLevel(1);
            testGame.getField(0,0).setLevel(1);
            testGame.getPlayer(1).setChosenWorker(11);
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
    public void chooseWorkerOk() {
        try {
            testGame.getPlayer(1).setChosenWorker(null);
            Assert.assertEquals(testGame.getPlayer(1).getTurnValue(), TurnValue.MOVE);
            Assert.assertEquals(testGame.getPlayer(2).getTurnValue(), TurnValue.END);

            testGame.getPlayer(1).turn(3, 2);
            Assert.assertSame(testGame.getPlayer(1).getChosenWorker(), 11);
        } catch (SantoriniException ex) {
            throw new ResponseStatusException(
                    ex.getException(), ex.toString(), ex);
        }
    }

    @Test
    public void moveOkMoveUp() {
        try {
            Assert.assertEquals(testGame.getPlayer(1).getTurnValue(), TurnValue.MOVE);
            Assert.assertEquals(testGame.getPlayer(2).getTurnValue(), TurnValue.END);
            testGame.getPlayer(1).turn(2, 2);
            Assert.assertEquals(testGame.getPlayer(1).getWorker(11).getField(), testGame.getField(2,2));
            Assert.assertFalse(testGame.getPlayer(2).getMoveUp());
        } catch (SantoriniException ex) {
            throw new ResponseStatusException(
                    ex.getException(), ex.toString(), ex);
        }
    }

    @Test
    public void moveOkMoveNotUp() {
        try {
            Assert.assertEquals(testGame.getPlayer(1).getTurnValue(), TurnValue.MOVE);
            Assert.assertEquals(testGame.getPlayer(2).getTurnValue(), TurnValue.END);
            testGame.getPlayer(1).turn(4, 2);
            Assert.assertEquals(testGame.getPlayer(1).getWorker(11).getField(), testGame.getField(4,2));
            Assert.assertTrue(testGame.getPlayer(2).getMoveUp());
        } catch (SantoriniException ex) {
            throw new ResponseStatusException(
                    ex.getException(), ex.toString(), ex);
        }
    }

    @Test
    public void moveOkMoveUpPlayer2() {
        try {
            testGame.getPlayer(2).setGodCard(Godcard.ATHENA);
            testGame.getPlayer(2).setChosenWorker(22);
            testGame.getPlayer(2).setTurnValue(TurnValue.MOVE);
            testGame.getPlayer(1).setTurnValue(TurnValue.END);
            testGame.getPlayer(2).turn(0, 0);
            Assert.assertEquals(testGame.getPlayer(2).getWorker(22).getField(), testGame.getField(0,0));
            Assert.assertFalse(testGame.getPlayer(1).getMoveUp());
        } catch (SantoriniException ex) {
            throw new ResponseStatusException(
                    ex.getException(), ex.toString(), ex);
        }
    }
}
