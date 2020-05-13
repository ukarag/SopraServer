package ch.uzh.ifi.seal.soprafs19.entity;

import ch.uzh.ifi.seal.soprafs19.Application;
import ch.uzh.ifi.seal.soprafs19.constant.Godcard;
import ch.uzh.ifi.seal.soprafs19.exceptions.SantoriniException;
import ch.uzh.ifi.seal.soprafs19.service.UserService;
import ch.uzh.ifi.seal.soprafs19.constant.TurnValue;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.server.ResponseStatusException;


@RunWith(SpringRunner.class)
@SpringBootTest(classes= Application.class)
public class PlayerTest {

    @Autowired
    private UserService userService;

    private final User user = new User();

    private final Player player = new Player();

    @Before
    public void setUp() {
        user.setToken("testToken");
    }

    @Test
    public void newPlayer() {
        User testUser = new User();
        testUser.setToken("testUserToken");
        Player testPlayer = new Player(testUser, 1);
        Assert.assertEquals(testPlayer.getUser(), testUser);
        Assert.assertEquals(testPlayer.getUserToken(), testUser.getToken());
        Assert.assertEquals(testPlayer.getMoveUp(), true);
        Assert.assertEquals(testPlayer.getTurnValue(), TurnValue.NOTHING);
    }

    @Test
    public void getAndSetId() {
        Assert.assertNull(player.getId());
        player.setId(1L);
        Assert.assertSame(player.getId(),1L);
    }

    @Test
    public void getAndSetUserToken() {
        Assert.assertNull(player.getUserToken());
        player.setUserToken("testToken");
        Assert.assertEquals(player.getUserToken(),"testToken");
    }

    @Test
    public void getAndSetUser() {
        Assert.assertNull(player.getUser());
        player.setUser(user);
        Assert.assertEquals(player.getUser(),user);
    }

    @Test
    public void getAndSetGame() {
        Game game = new Game();
        Assert.assertNull(player.getGame());
        player.setGame(game);
        Assert.assertEquals(player.getGame(), game);
    }

    @Test
    public void getAndSetNumber() {
        Assert.assertNull(player.getNumber());
        player.setNumber(1);
        Assert.assertSame(player.getNumber(),1);
    }

    @Test
    public void getAndSetMoveUp() {
        Assert.assertNull(player.getMoveUp());
        player.setMoveUp(true);
        Assert.assertEquals(player.getMoveUp(),true);
    }

    @Test
    public void getWorkerAndWorkers() {
        try {
            User testUser = new User();
            testUser.setUsername("testPlayerUsername");
            testUser.setPassword("testPlayerPassword");
            testUser = userService.createUser(testUser);
            Player testPlayer = new Player(testUser, 1);
            Assert.assertNotNull(testPlayer.getWorkers());
            Assert.assertNotNull(testPlayer.getWorker(11));
            Assert.assertNotNull(testPlayer.getWorker(12));
            Assert.assertNull(testPlayer.getWorker(10));
            Assert.assertTrue(testPlayer.getWorkers().contains(testPlayer.getWorker(11)));
            Assert.assertTrue(testPlayer.getWorkers().contains(testPlayer.getWorker(12)));
        } catch (SantoriniException ex) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, ex.getMessage(), ex);
        }
    }

    @Test
    public void getAndSetGodcard() {
        Assert.assertNull(player.getGodCard());
        player.setGodCard(Godcard.NOGODCARD);
        Assert.assertEquals(player.getGodCard(), Godcard.NOGODCARD);
    }

    @Test
    public void getAndSetTurnValue() {
        Assert.assertNull(player.getTurnValue());
        player.setTurnValue(TurnValue.MOVE);
        Assert.assertEquals(player.getTurnValue(), TurnValue.MOVE);
    }
}
