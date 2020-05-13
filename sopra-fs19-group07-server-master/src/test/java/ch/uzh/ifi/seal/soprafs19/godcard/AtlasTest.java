package ch.uzh.ifi.seal.soprafs19.godcard;

import ch.uzh.ifi.seal.soprafs19.Application;
import ch.uzh.ifi.seal.soprafs19.constant.Godcard;
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
public class AtlasTest {

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
            testPlayer.setGodCard(Godcard.ATLAS);
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
            testGame.getField(4, 2).setLevel(3);
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
    public void moveOk() {
        try {
            testGame.getPlayer(1).setTurnValue(TurnValue.MOVE);
            testGame.getPlayer(1).turn(3,1);
            Assert.assertEquals(testGame.getPlayer(1).getTurnValue(), TurnValue.BUILDDOME);
        } catch (SantoriniException ex) {
            throw new ResponseStatusException(
                    ex.getException(), ex.toString(), ex);
        }
    }

    @Test
    public void buildOkUnder3() {
        try {
            //build dome at level 0
            testGame.getPlayer(1).setTurnValue(TurnValue.DOME);
            testGame.getPlayer(1).turn(3, 1);
            Assert.assertSame(testGame.getField(3,1).getLevel(), 40);
        } catch (SantoriniException ex) {
            throw new ResponseStatusException(
                    ex.getException(), ex.toString(), ex);
        }
    }

    @Test
    public void buildOkIs3() {
        try {
            //build dome at level 3
            testGame.getPlayer(1).setTurnValue(TurnValue.DOME);
            testGame.getPlayer(1).turn(4, 2);
            Assert.assertSame(testGame.getField(4,2).getLevel(), 4);
        } catch (SantoriniException ex) {
            throw new ResponseStatusException(
                    ex.getException(), ex.toString(), ex);
        }
    }

    @Test
    public void wonOk() {
        try {
            testGame.getPlayer(1).setTurnValue(TurnValue.MOVE);
            testGame.getField(3,2).setLevel(2);
            testGame.getPlayer(1).turn(4, 2);
            Assert.assertEquals(testGame.getPlayer(1).getTurnValue(), TurnValue.WON);
            Assert.assertEquals(testGame.getPlayer(2).getTurnValue(), TurnValue.LOST);
        }catch (SantoriniException ex) {
            throw new ResponseStatusException(
                    ex.getException(), ex.toString(), ex);
        }
    }
}
