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
public class MoveTest {

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
            testGame.getPlayer(1).turn(1, 2);
            testGame.getPlayer(2).turn(0, 0);
            testGame.getPlayer(2).turn(0, 1);
            testGame.getField(2,1).setLevel(3);
            testGame.getField(2,2).setLevel(2);
            testGame.getField(2,3).setLevel(1);
            testGame.getField(3,2).setLevel(2);
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
    public void setChosenWorkerOk() {
        try {
            Assert.assertNull(testGame.getPlayer(1).getChosenWorker());
            testGame.getPlayer(1).turn(3, 2);
            Assert.assertSame(testGame.getPlayer(1).getChosenWorker(), 11);
        } catch (SantoriniException ex) {
            throw new ResponseStatusException(
                    ex.getException(), ex.toString(), ex);
        }
    }

    @Test
    public void setChosenWorkerNotOkNoWorker() {
        try {
            Assert.assertNull(testGame.getPlayer(1).getChosenWorker());
            testGame.getPlayer(1).turn(2, 2);
            Assert.fail();
        } catch (SantoriniException ex) {
            if (ex.getEnum()!= ExceptionEnum.TURN_NOT_ALLOWED) {
                throw new ResponseStatusException(
                        ex.getException(), ex.toString(), ex);
            }
        }
    }

    @Test
    public void moveOk() {
        try {
            testGame.getPlayer(1).setChosenWorker(11);

            testGame.getPlayer(1).turn(2, 2);
            Assert.assertNull(testGame.getField(3,2).getWorker());
            Assert.assertNotNull(testGame.getField(2,2).getWorker());
            Assert.assertNotNull(testGame.getPlayer(1).getWorker(11));
            Assert.assertEquals(testGame.getField(2,2).getWorker(), testGame.getPlayer(1).getWorker(11));
            Assert.assertEquals(testGame.getPlayer(1).getTurnValue(), TurnValue.BUILD);
        } catch (SantoriniException ex) {
            throw new ResponseStatusException(
                    ex.getException(), ex.toString(), ex);
        }
    }

    @Test
    public void moveOk3To3() {
        try {
            testGame.getPlayer(1).setChosenWorker(11);
            testGame.getField(3,2).setLevel(3);

            testGame.getPlayer(1).turn(2, 1);
            Assert.assertNull(testGame.getField(3,2).getWorker());
            Assert.assertNotNull(testGame.getField(2,1).getWorker());
            Assert.assertNotNull(testGame.getPlayer(1).getWorker(11));
            Assert.assertEquals(testGame.getField(2,1).getWorker(), testGame.getPlayer(1).getWorker(11));
            Assert.assertEquals(testGame.getPlayer(1).getTurnValue(), TurnValue.BUILD);
        } catch (SantoriniException ex) {
            throw new ResponseStatusException(
                    ex.getException(), ex.toString(), ex);
        }
    }

    @Test
    public void moveNotOk() {
        try {
            testGame.getPlayer(2).setTurnValue(TurnValue.MOVE);
            testGame.getPlayer(2).setChosenWorker(21);
            testGame.getPlayer(2).turn(0, 1);
            Assert.fail();
        } catch (SantoriniException ex) {
            if (ex.getEnum()!= ExceptionEnum.TURN_NOT_ALLOWED) {
                throw new ResponseStatusException(
                        ex.getException(), ex.toString(), ex);
            }
        }
    }

    @Test
    public void allowedWorkersOk() {
        Assert.assertEquals(testGame.getPlayer(1).getTurnValue(), TurnValue.MOVE);
        testGame.getPlayer(1).allowedFields();
        for (Field field: testGame.getBoard()) {
            int x = field.getX();
            int y = field.getY();
            if ((x==3 && y==2) ||
                    (x==1 && y==2)) {
                Assert.assertTrue(field.getClickable()[0]);
            } else {
                Assert.assertFalse(field.getClickable()[0]);
            }
        }
    }

    @Test
    public void allowedWorkersOkOneNotMovable() {
        testGame.getPlayer(2).setTurnValue(TurnValue.MOVE);
        testGame.getField(1,0).setLevel(2);
        testGame.getField(1,1).setLevel(2);
        testGame.getPlayer(2).allowedFields();
        for (Field field: testGame.getBoard()) {
            int x = field.getX();
            int y = field.getY();
            if (x==0 && y==1) {
                Assert.assertTrue(field.getClickable()[1]);
            } else {
                Assert.assertFalse(field.getClickable()[1]);
            }
        }
    }

    @Test
    public void allowedMoveOk() {
        testGame.getPlayer(1).setChosenWorker(11);
        testGame.getPlayer(1).allowedFields();
        Assert.assertSame(testGame.getField(3, 2).getWorker().getWorkerNr(), 11);
        for (Field field : testGame.getBoard()) {
            int x = field.getX();
            int y = field.getY();
            if (((x==2 || x==4) && (y==1 || y==2 || y==3)) ||
                    (x==3 && (y==1 || y==3))) {
                Assert.assertTrue(field.getClickable()[0]);
            } else {
                Assert.assertFalse(field.getClickable()[0]);
            }
        }

        testGame.getPlayer(1).setChosenWorker(12);
        testGame.getPlayer(1).allowedFields();
        Assert.assertSame(testGame.getField(3, 2).getWorker().getWorkerNr(), 11);
        for (Field field : testGame.getBoard()) {
            int x = field.getX();
            int y = field.getY();
            if ((x==0 && (y==2 || y==3)) ||
                    (x==1 && (y==1 || y==3)) ||
                    (x==2 && y==3)) {
                Assert.assertTrue(field.getClickable()[0]);
            } else {
                Assert.assertFalse(field.getClickable()[0]);
            }
        }

        testGame.getPlayer(1).setTurnValue(TurnValue.END);
        testGame.getPlayer(2).setTurnValue(TurnValue.MOVE);
        testGame.getPlayer(2).setChosenWorker(21);
        testGame.getPlayer(2).allowedFields();
        Assert.assertSame(testGame.getField(0, 0).getWorker().getWorkerNr(), 21);
        Assert.assertSame(testGame.getField(0, 1).getWorker().getWorkerNr(), 22);
        Assert.assertSame(testGame.getField(3, 2).getWorker().getWorkerNr(), 11);
        for (Field field : testGame.getBoard()) {
            int x = field.getX();
            int y = field.getY();
            if ((x==1 && (y==0 || y==1))) {
                Assert.assertTrue(field.getClickable()[1]);
            } else {
                Assert.assertFalse(field.getClickable()[1]);
            }
        }
    }

    @Test
    public void allowedMoveOkOnLevel3() {
        testGame.getPlayer(1).setChosenWorker(11);
        testGame.getField(3, 2).setLevel(3);
        testGame.getField(3, 3).setLevel(4);

        testGame.getPlayer(1).allowedFields();
        Assert.assertSame(testGame.getField(3, 2).getWorker().getWorkerNr(), 11);
        for (Field field : testGame.getBoard()) {
            int x = field.getX();
            int y = field.getY();
            if (((x==2 || x==4) && (y==1 || y==2 || y==3)) ||
                    (x==3 && y==1)) {
                Assert.assertTrue(field.getClickable()[0]);
            } else {
                Assert.assertFalse(field.getClickable()[0]);
            }
        }
    }

    @Test
    public void checkMoveWonOkPlayer1Won() {
        try {
            testGame.getPlayer(1).setChosenWorker(11);
            testGame.getField(2,2).setLevel(3);
            testGame.getPlayer(1).turn(2,2);
            Assert.assertEquals(testGame.getPlayer(1).getTurnValue(), TurnValue.WON);
            Assert.assertEquals(testGame.getPlayer(2).getTurnValue(), TurnValue.LOST);
        } catch (SantoriniException ex) {
            throw new ResponseStatusException(
                    ex.getException(), ex.toString(), ex);
        }
    }

    @Test
    public void checkMoveWonOkPlayer2Won() {
        try {
            testGame.getField(0,1).setLevel(2);
            testGame.getField(0,2).setLevel(3);
            testGame.getPlayer(2).setTurnValue(TurnValue.MOVE);
            testGame.getPlayer(2).setChosenWorker(22);
            testGame.getPlayer(2).turn(0,2);
            Assert.assertEquals(testGame.getPlayer(1).getTurnValue(), TurnValue.LOST);
            Assert.assertEquals(testGame.getPlayer(2).getTurnValue(), TurnValue.WON);
        } catch (SantoriniException ex) {
            throw new ResponseStatusException(
                    ex.getException(), ex.toString(), ex);
        }
    }

    @Test
    public void checkNotMovableWonOkPlayer1Lost() {
        try {
            //set worker on desired field
            Player player = testGame.getPlayer(1);
            Player player2 = testGame.getPlayer(2);
            testGame.getField(0, 0).setWorker(null);
            testGame.getField(1, 0).setWorker(player.getWorker(11));
            testGame.getField(0, 1).setWorker(player.getWorker(12));
            testGame.getField(1, 1).setWorker(player2.getWorker(21));
            testGame.getField(0, 2).setWorker(player2.getWorker(22));

            testGame.getField(1,0).setLevel(2);
            testGame.getField(1,2).setLevel(2);
            testGame.getPlayer(1).setChosenWorker(11);

            // Player 1 moves to (0,0)
            testGame.getPlayer(1).turn( 0, 0);
            Assert.assertEquals(testGame.getPlayer(1).getTurnValue(), TurnValue.LOST);
            Assert.assertEquals(testGame.getPlayer(2).getTurnValue(), TurnValue.WON);
        } catch (SantoriniException ex) {
            throw new ResponseStatusException(
                    ex.getException(), ex.toString(), ex);
        }
    }

    @Test
    public void checkNotMovableWonOkPlayer2Lost() {
        try {
            //set worker on desired field
            Player player = testGame.getPlayer(1);
            Player player2 = testGame.getPlayer(2);
            testGame.getField(0, 0).setWorker(null);
            testGame.getField(1, 1).setWorker(player.getWorker(11));
            testGame.getField(0, 2).setWorker(player.getWorker(12));
            testGame.getField(1, 0).setWorker(player2.getWorker(21));
            testGame.getField(0, 1).setWorker(player2.getWorker(22));

            testGame.getField(1,0).setLevel(2);
            testGame.getField(1,2).setLevel(2);
            testGame.getPlayer(2).setTurnValue(TurnValue.MOVE);
            testGame.getPlayer(2).setChosenWorker(21);

            // Player 1 moves to (0,0)
            testGame.getPlayer(2).turn( 0, 0);
            Assert.assertEquals(testGame.getPlayer(1).getTurnValue(), TurnValue.WON);
            Assert.assertEquals(testGame.getPlayer(2).getTurnValue(), TurnValue.LOST);
        } catch (SantoriniException ex) {
            throw new ResponseStatusException(
                    ex.getException(), ex.toString(), ex);
        }
    }
}
