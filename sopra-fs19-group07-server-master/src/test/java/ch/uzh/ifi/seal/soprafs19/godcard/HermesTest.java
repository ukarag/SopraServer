package ch.uzh.ifi.seal.soprafs19.godcard;

import ch.uzh.ifi.seal.soprafs19.Application;
import ch.uzh.ifi.seal.soprafs19.constant.ExceptionEnum;
import ch.uzh.ifi.seal.soprafs19.constant.Godcard;
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
public class HermesTest {

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
            testPlayer.setGodCard(Godcard.HERMES);
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

            // 4 | 1 | 1 | 2 | 1
            // 0 | 1 | 2 | 3 | 0
            // 0 | 1 | 0 | 4 | 0
            // 0 | 3 | 1 | 0 | 3
            // 0 | 1 | 2 | 0 | 0
            testGame.getField(0,0).setLevel(4);
            testGame.getField(0,1).setLevel(1);
            testGame.getField(0,2).setLevel(1);
            testGame.getField(0,3).setLevel(2);
            testGame.getField(0,4).setLevel(1);
            testGame.getField(1,1).setLevel(1);
            testGame.getField(1,2).setLevel(2);
            testGame.getField(1,3).setLevel(3);
            testGame.getField(2,1).setLevel(1);
            testGame.getField(2,3).setLevel(4);
            testGame.getField(3,1).setLevel(3);
            testGame.getField(3,2).setLevel(1);
            testGame.getField(3,4).setLevel(3);
            testGame.getField(4,1).setLevel(1);
            testGame.getField(4,2).setLevel(2);

            testGame.getPlayer(1).setTurnValue(TurnValue.MOVE);
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
    public void endOk() {
        try {
            testGame.getPlayer(1).setTurnValue(TurnValue.END);

            testGame.getPlayer(1).turn(1, 1);
            Assert.assertEquals(testGame.getPlayer(1).getTurnValue(), TurnValue.BUILDMOVE);
        } catch (SantoriniException ex) {
            throw new ResponseStatusException(
                    ex.getException(), ex.toString(), ex);
        }
    }

    @Test
    public void setChosenWorkerOkMove() {
        try {
            testGame.getPlayer(1).setChosenWorker(null);

            testGame.getPlayer(1).turn(3, 2);
            Assert.assertSame(testGame.getPlayer(1).getChosenWorker(), 11);
            Assert.assertEquals(testGame.getPlayer(1).getTurnValue(), TurnValue.MOVE);
        } catch (SantoriniException ex) {
            throw new ResponseStatusException(
                    ex.getException(), ex.toString(), ex);
        }
    }

    @Test
    public void setChosenWorkerOkBuild() {
        try {
            testGame.getPlayer(1).setChosenWorker(null);
            testGame.getPlayer(1).setTurnValue(TurnValue.BUILD);

            testGame.getPlayer(1).turn(3, 2);
            Assert.assertSame(testGame.getPlayer(1).getChosenWorker(), 11);
            Assert.assertEquals(testGame.getPlayer(1).getTurnValue(), TurnValue.BUILD);
        } catch (SantoriniException ex) {
            throw new ResponseStatusException(
                    ex.getException(), ex.toString(), ex);
        }
    }

    @Test
    public void moveOkMoveUp() {
        try {
            testGame.getPlayer(1).turn(4,2);
            Assert.assertEquals(testGame.getPlayer(1).getTurnValue(), TurnValue.BUILD);
            Assert.assertNotNull(testGame.getPlayer(1).getChosenWorker());
        } catch (SantoriniException ex) {
            throw new ResponseStatusException(
                    ex.getException(), ex.toString(), ex);
        }
    }

    @Test
    public void moveOkMoveDown() {
        try {
            testGame.getPlayer(1).turn(4,3);
            Assert.assertEquals(testGame.getPlayer(1).getTurnValue(), TurnValue.BUILD);
            Assert.assertNotNull(testGame.getPlayer(1).getChosenWorker());
        } catch (SantoriniException ex) {
            throw new ResponseStatusException(
                    ex.getException(), ex.toString(), ex);
        }
    }

    @Test
    public void moveOkMoveSameLevel() {
        try {
            testGame.getPlayer(1).turn(0,2);
            Assert.assertEquals(testGame.getPlayer(1).getTurnValue(), TurnValue.BUILDMOVE);
            Assert.assertNull(testGame.getPlayer(1).getChosenWorker());
        } catch (SantoriniException ex) {
            throw new ResponseStatusException(
                    ex.getException(), ex.toString(), ex);
        }
    }

    @Test
    public void moveNotOk() {
        try {
            testGame.getPlayer(1).turn(3,1);
            Assert.fail();
        } catch (SantoriniException ex) {
            if (ex.getEnum()!= ExceptionEnum.TURN_NOT_ALLOWED) {
                throw new ResponseStatusException(
                        ex.getException(), ex.toString(), ex);
            }
        }
    }

    @Test
    public void buildOk() {
        try {
            testGame.getPlayer(1).setTurnValue(TurnValue.BUILD);
            testGame.getPlayer(1).turn(3, 3);
            Assert.assertSame(testGame.getField(3,3).getLevel(), 1);
            Assert.assertEquals(testGame.getPlayer(1).getTurnValue(), TurnValue.END);
            Assert.assertEquals(testGame.getPlayer(2).getTurnValue(), TurnValue.MOVE);
        } catch (SantoriniException ex) {
            throw new ResponseStatusException(
                    ex.getException(), ex.toString(), ex);
        }
    }

    @Test
    public void turnOk() {
        try {
            testGame.getPlayer(1).setTurnValue(TurnValue.SETWORKER);
            testGame.getPlayer(1).getWorker(12).setField(null);
            testGame.getField(1, 2).setWorker(null);

            testGame.getPlayer(1).turn(1, 2);
            Assert.assertNotNull(testGame.getField(1,2).getWorker());
            Assert.assertEquals(testGame.getPlayer(1).getTurnValue(), TurnValue.END);
            Assert.assertEquals(testGame.getPlayer(2).getTurnValue(), TurnValue.MOVE);
        } catch (SantoriniException ex) {
            throw new ResponseStatusException(
                    ex.getException(), ex.toString(), ex);
        }
    }

    @Test
    public void allowedChosenWorkerInMove() {
        testGame.getPlayer(1).setChosenWorker(null);

        testGame.getPlayer(1).allowedFields();
        for (Field field : testGame.getBoard()) {
            int x = field.getX();
            int y = field.getY();
            if ((x==1 || x==3) && y==2) {
                Assert.assertTrue(field.getClickable()[0]);
            } else {
                Assert.assertFalse(field.getClickable()[0]);
            }
        }
    }

    @Test
    public void allowedMove() {
        try {
            // first move in turn
            testGame.getPlayer(1).allowedFields();
            for (Field field : testGame.getBoard()) {
                int x = field.getX();
                int y = field.getY();
                if (((x==1 || x==2 || x==4) && y==1) ||
                        ((x==0 || x==3 || x==4) && y==2) ||
                        ((x==3 || x==4) && y==3)) {
                    Assert.assertTrue(field.getClickable()[0]);
                } else {
                    Assert.assertFalse(field.getClickable()[0]);
                }
            }

            // second move in turn
            testGame.getPlayer(1).turn(4,1);
            testGame.getPlayer(1).setTurnValue(TurnValue.MOVE);
            testGame.getPlayer(1).turn(4,1);
            testGame.getPlayer(1).allowedFields();
            for (Field field : testGame.getBoard()) {
                int x = field.getX();
                int y = field.getY();
                if (((x==1 || x==2 || x==4) && y==1) ||
                        ((x==0 || x==3) && y==2)) {
                    Assert.assertTrue(field.getClickable()[0]);
                } else {
                    Assert.assertFalse(field.getClickable()[0]);
                }
            }

            // third move with other worker
            testGame.getPlayer(1).turn(4,1);
            testGame.getPlayer(1).setTurnValue(TurnValue.MOVE);
            testGame.getPlayer(1).turn(1,2);
            testGame.getPlayer(1).allowedFields();
            for (Field field : testGame.getBoard()) {
                int x = field.getX();
                int y = field.getY();
                if ((x==0 && y==3) || (x==1 && y==2)) {
                    Assert.assertTrue(field.getClickable()[0]);
                } else {
                    Assert.assertFalse(field.getClickable()[0]);
                }
            }
        } catch (SantoriniException ex) {
            throw new ResponseStatusException(
                    ex.getException(), ex.toString(), ex);
        }
    }

    @Test
    public void allowedMoveAtBorder() {
        testGame.getField(3, 2).setWorker(testGame.getPlayer(1).getWorker(null));
        testGame.getField(4, 1).setWorker(testGame.getPlayer(1).getWorker(11));
        testGame.getPlayer(1).allowedFields();
        for (Field field : testGame.getBoard()) {
            int x = field.getX();
            int y = field.getY();
            if (((x==3 || x==4) && y==0) ||
                    ((x==1 || x==2 || x==4) && y==1) ||
                    ((x==0 || x==3 || x==4) && y==2)) {
                Assert.assertTrue(field.getClickable()[0]);
            } else {
                Assert.assertFalse(field.getClickable()[0]);
            }
        }
    }

    @Test
    public void allowedChosenWorkerInBuild() {
        testGame.getPlayer(1).setTurnValue(TurnValue.BUILD);
        testGame.getPlayer(1).setChosenWorker(null);

        testGame.getPlayer(1).allowedFields();
        for (Field field : testGame.getBoard()) {
            int x = field.getX();
            int y = field.getY();
            if ((x==1 || x==3) && y==2) {
                Assert.assertTrue(field.getClickable()[0]);
            } else {
                Assert.assertFalse(field.getClickable()[0]);
            }
        }
    }

    @Test
    public void allowedBuildOk() {
        testGame.getPlayer(1).setTurnValue(TurnValue.BUILD);
        testGame.getPlayer(1).allowedFields();
        for (Field field : testGame.getBoard()) {
            int x = field.getX();
            int y = field.getY();
            if (((x==2 || x==3 || x==4) && y==1) ||
                    (x==4 && y==2) ||
                    ((x==3 || x==4) && y==3)) {
                Assert.assertTrue(field.getClickable()[0]);
            } else {
                Assert.assertFalse(field.getClickable()[0]);
            }
        }
    }

    @Test
    public void allowedFieldsOk() {
        testGame.getPlayer(2).setGodCard(Godcard.HERMES);
        testGame.getPlayer(2).allowedFields();
        for (Field field : testGame.getBoard()) {
            Assert.assertFalse(field.getClickable()[1]);
        }
    }

    @Test
    public void checkNotMovableWonOk() {
        try {
            testGame.getField(3, 2).setWorker(testGame.getPlayer(1).getWorker(11));
            testGame.getField(4, 4).setWorker(testGame.getPlayer(1).getWorker(12));
            testGame.getField(3,2).setLevel(2);
            testGame.getField(3,3).setLevel(2);
            testGame.getField(4,2).setLevel(2);
            testGame.getPlayer(1).turn(4,3);
            Assert.assertEquals(testGame.getPlayer(1).getTurnValue(), TurnValue.LOST);
            Assert.assertEquals(testGame.getPlayer(2).getTurnValue(), TurnValue.WON);
        } catch (SantoriniException ex) {
            throw new ResponseStatusException(
                    ex.getException(), ex.toString(), ex);
        }
    }

    @Test
    public void checkNotMovableWonOkNotDecided() {
        try {
            testGame.getField(3, 2).setWorker(testGame.getPlayer(1).getWorker(11));
            testGame.getField(4, 4).setWorker(testGame.getPlayer(1).getWorker(12));
            testGame.getField(3, 3).setWorker(testGame.getPlayer(2).getWorker(22));
            testGame.getField(3,2).setLevel(2);
            testGame.getField(3,3).setLevel(1);
            testGame.getField(4,2).setLevel(2);
            testGame.getPlayer(1).turn(4,3);
            Assert.assertEquals(testGame.getPlayer(1).getTurnValue(), TurnValue.BUILD);
        } catch (SantoriniException ex) {
            throw new ResponseStatusException(
                    ex.getException(), ex.toString(), ex);
        }
    }

    @Test
    public void movableOk() {
        Assert.assertTrue(testGame.getPlayer(1).getGodCard().movable(testGame.getField(3, 2), testGame.getField(3, 2)));
        Assert.assertTrue(testGame.getPlayer(1).getGodCard().movable(testGame.getField(3, 2), testGame.getField(4, 2)));
        Assert.assertFalse(testGame.getPlayer(1).getGodCard().movable(testGame.getField(3, 2), testGame.getField(3, 1)));
    }
}
