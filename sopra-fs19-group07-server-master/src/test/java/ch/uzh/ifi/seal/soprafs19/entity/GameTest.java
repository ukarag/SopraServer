package ch.uzh.ifi.seal.soprafs19.entity;

import org.junit.Assert;
import org.junit.Test;

public class GameTest {

    private final Game game = new Game();

    @Test
    public void getAndSetId() {
        Assert.assertNull(game.getId());
        game.setId(1L);
        Assert.assertSame(game.getId(),1L);
    }

    @Test
    public void getAndSetUsername() {
        Assert.assertNull(game.getGameName());
        game.setGameName("testName");
        Assert.assertEquals(game.getGameName(),"testName");
    }

    @Test
    public void getAndSetToken() {
        Assert.assertNull(game.getGameName());
        game.setGameName("testToken");
        Assert.assertEquals(game.getGameName(),"testToken");
    }

    @Test
    public void getAndSetWithGodcards() {
        Assert.assertNull(game.getWithGodcards());
        game.setWithGodcards(true);
        Assert.assertTrue(game.getWithGodcards());
    }

    @Test
    public void getPlayers() {
        User testUser = new User();
        testUser.setUsername("testGetPlayersUsername");
        testUser.setPassword("testGetPlayersPassword");
        Player testPlayer = new Player(testUser, 1);

        User testUser2 = new User();
        testUser2.setUsername("testGetPlayersUsername2");
        testUser2.setPassword("testGetPlayersPassword2");
        Player testPlayer2 = new Player(testUser2, 2);

        game.addPlayer(testPlayer);
        game.addPlayer(testPlayer2);

        Assert.assertNotNull(game.getPlayers());
        Assert.assertTrue(game.getPlayers().contains(testPlayer));
        Assert.assertTrue(game.getPlayers().contains(testPlayer2));
    }

    @Test
    public void getAndAddPlayerByNumber() {
        User testUser = new User();
        testUser.setUsername("testGetPlayerUsername");
        testUser.setPassword("testGetPlayerPassword");
        Player testPlayer = new Player(testUser, 1);

        game.addPlayer(testPlayer);

        Player player1 = game.getPlayer(1);
        Assert.assertNotNull(player1);
        Assert.assertNull(game.getPlayer(2));
        Assert.assertEquals(player1.getId(), testPlayer.getId());
        Assert.assertEquals(player1.getUserToken(), testPlayer.getUserToken());
        Assert.assertEquals(player1.getUser(), testPlayer.getUser());
        Assert.assertEquals(player1.getNumber(), testPlayer.getNumber());
        Assert.assertEquals(player1.getMoveUp(), testPlayer.getMoveUp());
        Assert.assertEquals(player1.getGodCard(), testPlayer.getGodCard());
        Assert.assertEquals(player1.getTurnValue(), testPlayer.getTurnValue());
    }

    @Test
    public void getAndAddPlayerByToken() {
        User testUser = new User();
        testUser.setUsername("testGetPlayerUsername");
        testUser.setPassword("testGetPlayerPassword");
        testUser.setToken("testGetPlayerToken");
        Player testPlayer = new Player(testUser, 1);

        game.addPlayer(testPlayer);

        Player player1 = game.getPlayer("testGetPlayerToken");
        Assert.assertNotNull(player1);
        Assert.assertNull(game.getPlayer("token"));
        Assert.assertEquals(player1.getId(), testPlayer.getId());
        Assert.assertEquals(player1.getUserToken(), testPlayer.getUserToken());
        Assert.assertEquals(player1.getUser(), testPlayer.getUser());
        Assert.assertEquals(player1.getNumber(), testPlayer.getNumber());
        Assert.assertEquals(player1.getMoveUp(), testPlayer.getMoveUp());
        Assert.assertEquals(player1.getGodCard(), testPlayer.getGodCard());
        Assert.assertEquals(player1.getTurnValue(), testPlayer.getTurnValue());

        int size = game.getPlayers().size();
        game.addPlayer(testPlayer);
        Assert.assertSame(game.getPlayers().size(), size);
    }

    @Test
    public void removePlayer() {
        User testUser = new User();
        testUser.setUsername("testRemoveUsername");
        testUser.setPassword("testRemovePassword");
        Player testPlayer = new Player(testUser, 1);

        game.addPlayer(testPlayer);
        Assert.assertNotNull(game.getPlayer(1));

        game.removePlayer(testPlayer);
        Assert.assertNull(game.getPlayer(1));
    }

    @Test
    public void getAndSetStage() {
        Assert.assertSame(game.getStage(), 0);
        game.setStage(1);
        Assert.assertSame(game.getStage(), 1);
    }

    @Test
    public void getField() {
        Assert.assertSame(game.getField(1, 1).getGame(), game);
        Assert.assertNull(game.getField(5, 5));
    }

    @Test
    public void equals() {
        Game game1 = new Game();
        game1.setId(1L);
        Game game2 = new Game();
        game2.setId(1L);
        Assert.assertEquals(game1, game2);
        Assert.assertEquals(game1.hashCode(), game2.hashCode());
        game2.setId(2L);
        Assert.assertNotEquals(game1, game2);
        Assert.assertNotEquals(game1, 1);
        Assert.assertNotEquals(game1, null);
    }
}
