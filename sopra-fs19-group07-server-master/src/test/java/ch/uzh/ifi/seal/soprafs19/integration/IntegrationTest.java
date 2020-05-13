package ch.uzh.ifi.seal.soprafs19.integration;

import ch.uzh.ifi.seal.soprafs19.Application;
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
public class IntegrationTest {

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
    private User testUser2;
    private Game testGame;

    @Before
    public void setUp() {
        try {
            testUser = new User();
            testUser.setUsername("testUsername");
            testUser.setPassword("testPassword");
            testUser = userService.createUser(testUser);

            testUser2 = new User();
            testUser2.setUsername("testIntegration2GodsUsername2");
            testUser2.setPassword("testIntegration2GodsPassword2");
            testUser2 = userService.createUser(testUser2);
            String token2 = testUser2.getToken();


            testGame = new Game();
            testGame.setGameName("TestApolloMoveName");
            testGame.addPlayer(new Player(testUser, 1));
            testGame.setWithGodcards(true);
            testGame = gameService.createGame(testGame);
            testGame = gameService.joinGame(testGame.getId(), token2);
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
    public void apolloArtemis() {
        try {
            String token = testUser.getToken();
            String token2 = testUser2.getToken();
            Player playerInfo = new Player();

            // player 1 selects godcards of game
            gameService.selectGodcards(token, "APOLLO", "ARTEMIS");

            // player 2 decides who has which godcards
            gameService.saveGodcards(token2, "APOLLO", "ARTEMIS");
            testGame = gameRepository.findByUserToken(token);
            Assert.assertEquals(testGame.getPlayer(1).getGodCard(), Godcard.APOLLO);
            Assert.assertEquals(testGame.getPlayer(2).getGodCard(), Godcard.ARTEMIS);

            // player 1 selects himself as startPlayer
            gameService.saveStarter(token, 1);
            testGame = gameRepository.findByUserToken(token);
            Assert.assertEquals(testGame.getPlayer(1).getTurnValue(), TurnValue.SETWORKER);
            Assert.assertEquals(testGame.getPlayer(2).getTurnValue(), TurnValue.NOTHING);

            // player 1 sets his worker
            gameService.turn(token, 1, 1);
            gameService.turn(token, 3, 1);
            testGame = gameRepository.findByUserToken(token);
            Assert.assertEquals(testGame.getPlayer(1).getTurnValue(), TurnValue.END);
            Assert.assertEquals(testGame.getPlayer(2).getTurnValue(), TurnValue.SETWORKER);

            // player 2 sets his worker
            gameService.turn(token2, 1, 3);
            gameService.turn(token2, 3, 3);
            testGame = gameRepository.findByUserToken(token);
            Assert.assertEquals(testGame.getPlayer(1).getTurnValue(), TurnValue.MOVE);
            Assert.assertEquals(testGame.getPlayer(2).getTurnValue(), TurnValue.END);

            // player 1 turn
            gameService.turn(token, 1, 1);
            gameService.turn(token, 1, 2);
            gameService.turn(token, 2, 2);
            testGame = gameRepository.findByUserToken(token);
            Assert.assertSame(testGame.getField(2, 2).getLevel(), 1);
            Assert.assertEquals(testGame.getPlayer(1).getTurnValue(), TurnValue.END);
            Assert.assertEquals(testGame.getPlayer(2).getTurnValue(), TurnValue.MOVE);

            //player 2 turn
            gameService.turn(token2, 1, 3);
            gameService.turn(token2, 2, 2);
            testGame = gameRepository.findByUserToken(token);
            Assert.assertSame(testGame.getPlayer(2).getTurnValue(), TurnValue.BUILDMOVE);
            playerInfo.setTurnValue(TurnValue.MOVE);
            gameService.nextTurn(token2, playerInfo);
            testGame = gameRepository.findByUserToken(token);
            Assert.assertFalse(testGame.getField(1, 3).getClickable()[1]);
            gameService.turn(token2, 3, 2);
            gameService.turn(token2, 2, 1);
            testGame = gameRepository.findByUserToken(token);
            Assert.assertSame(testGame.getField(2, 1).getLevel(), 1);
            Assert.assertEquals(testGame.getPlayer(1).getTurnValue(), TurnValue.MOVE);
            Assert.assertEquals(testGame.getPlayer(2).getTurnValue(), TurnValue.END);

            //player 1 turn
            gameService.turn(token, 3, 1);
            gameService.turn(token, 3, 2);
            gameService.turn(token, 4, 2);
            testGame = gameRepository.findByUserToken(token);
            Assert.assertSame(testGame.getField(4, 2).getLevel(), 1);
            Assert.assertEquals(testGame.getPlayer(1).getWorker(12), testGame.getField(3,2).getWorker());
            Assert.assertEquals(testGame.getPlayer(2).getWorker(21), testGame.getField(3,1).getWorker());
            Assert.assertEquals(testGame.getPlayer(1).getTurnValue(), TurnValue.END);
            Assert.assertEquals(testGame.getPlayer(2).getTurnValue(), TurnValue.MOVE);

            //player 2 turn
            gameService.turn(token2, 3, 3);
            gameService.turn(token2, 2, 2);
            testGame = gameRepository.findByUserToken(token);
            Assert.assertSame(testGame.getPlayer(2).getTurnValue(), TurnValue.BUILDMOVE);
            playerInfo.setTurnValue(TurnValue.BUILD);
            gameService.nextTurn(token2, playerInfo);
            gameService.turn(token2, 2, 1);
            testGame = gameRepository.findByUserToken(token);
            Assert.assertSame(testGame.getField(2, 1).getLevel(), 2);
            Assert.assertEquals(testGame.getPlayer(1).getTurnValue(), TurnValue.MOVE);
            Assert.assertEquals(testGame.getPlayer(2).getTurnValue(), TurnValue.END);
        } catch (SantoriniException ex) {
            throw new ResponseStatusException(
                    ex.getException(), ex.toString(), ex);
        }
    }

    @Test
    public void atlasDemeter() {
        try {
            String token = testUser.getToken();
            String token2 = testUser2.getToken();
            Player playerInfo = new Player();

            // player 1 selects godcards of game
            gameService.selectGodcards(token, "ATLAS", "DEMETER");

            // player 2 decides who has which godcards
            gameService.saveGodcards(token2, "ATLAS", "DEMETER");
            testGame = gameRepository.findByUserToken(token);
            Assert.assertEquals(testGame.getPlayer(1).getGodCard(), Godcard.ATLAS);
            Assert.assertEquals(testGame.getPlayer(2).getGodCard(), Godcard.DEMETER);

            // player 1 selects himself as startPlayer
            gameService.saveStarter(token, 1);
            testGame = gameRepository.findByUserToken(token);
            Assert.assertEquals(testGame.getPlayer(1).getTurnValue(), TurnValue.SETWORKER);
            Assert.assertEquals(testGame.getPlayer(2).getTurnValue(), TurnValue.NOTHING);

            // player 1 sets his worker
            gameService.turn(token, 1, 1);
            gameService.turn(token, 3, 1);
            testGame = gameRepository.findByUserToken(token);
            Assert.assertEquals(testGame.getPlayer(1).getTurnValue(), TurnValue.END);
            Assert.assertEquals(testGame.getPlayer(2).getTurnValue(), TurnValue.SETWORKER);

            // player 2 sets his worker
            gameService.turn(token2, 1, 3);
            gameService.turn(token2, 3, 3);
            testGame = gameRepository.findByUserToken(token);
            Assert.assertEquals(testGame.getPlayer(1).getTurnValue(), TurnValue.MOVE);
            Assert.assertEquals(testGame.getPlayer(2).getTurnValue(), TurnValue.END);

            // player 1 turn
            gameService.turn(token, 1, 1);
            gameService.turn(token, 2, 1);
            playerInfo.setTurnValue(TurnValue.DOME);
            gameService.nextTurn(token, playerInfo);
            gameService.turn(token, 1, 0);
            testGame = gameRepository.findByUserToken(token);
            Assert.assertSame(testGame.getField(1, 0).getLevel(), 40);
            Assert.assertEquals(testGame.getPlayer(1).getTurnValue(), TurnValue.END);
            Assert.assertEquals(testGame.getPlayer(2).getTurnValue(), TurnValue.MOVE);

            //player 2 turn
            gameService.turn(token2, 1, 3);
            gameService.turn(token2, 1, 4);
            gameService.turn(token2, 1, 3);
            playerInfo.setTurnValue(TurnValue.BUILD);
            gameService.nextTurn(token2, playerInfo);
            testGame = gameRepository.findByUserToken(token);
            Assert.assertSame(testGame.getField(1, 3).getLevel(), 1);
            Assert.assertFalse(testGame.getField(1, 3).getClickable()[1]);
            gameService.turn(token2, 2, 3);
            testGame = gameRepository.findByUserToken(token);
            Assert.assertEquals(testGame.getPlayer(1).getTurnValue(), TurnValue.MOVE);
            Assert.assertEquals(testGame.getPlayer(2).getTurnValue(), TurnValue.END);
            Assert.assertSame(testGame.getField(2, 3).getLevel(), 1);

            //player 1 turn
            gameService.turn(token, 2, 1);
            gameService.turn(token, 1, 2);
            playerInfo.setTurnValue(TurnValue.DOME);
            gameService.nextTurn(token, playerInfo);
            gameService.turn(token, 2, 3);
            testGame = gameRepository.findByUserToken(token);
            Assert.assertSame(testGame.getField(2, 3).getLevel(), 41);
            Assert.assertEquals(testGame.getPlayer(1).getTurnValue(), TurnValue.END);
            Assert.assertEquals(testGame.getPlayer(2).getTurnValue(), TurnValue.MOVE);

            //player 2 turn
            gameService.turn(token2, 1, 4);
            gameService.turn(token2, 0, 4);
            gameService.turn(token2, 1, 3);
            playerInfo.setTurnValue(TurnValue.END);
            gameService.nextTurn(token2, playerInfo);
            testGame = gameRepository.findByUserToken(token);
            Assert.assertSame(testGame.getField(1, 3).getLevel(), 2);
            Assert.assertEquals(testGame.getPlayer(1).getTurnValue(), TurnValue.MOVE);
            Assert.assertEquals(testGame.getPlayer(2).getTurnValue(), TurnValue.END);

            //player 1 turn
            gameService.turn(token, 1, 2);
            gameService.turn(token, 2, 2);
            playerInfo.setTurnValue(TurnValue.DOME);
            gameService.nextTurn(token, playerInfo);
            gameService.turn(token, 1, 3);
            testGame = gameRepository.findByUserToken(token);
            Assert.assertSame(testGame.getField(1, 3).getLevel(), 42);
            Assert.assertEquals(testGame.getPlayer(1).getTurnValue(), TurnValue.END);
            Assert.assertEquals(testGame.getPlayer(2).getTurnValue(), TurnValue.MOVE);
        } catch (SantoriniException ex) {
            throw new ResponseStatusException(
                    ex.getException(), ex.toString(), ex);
        }
    }

    @Test
    public void prometheusPan() {
        try {
            String token = testUser.getToken();
            String token2 = testUser2.getToken();
            Player playerInfo = new Player();

            // player 1 selects godcards of game
            gameService.selectGodcards(token, "PAN", "PROMETHEUS");

            // player 2 decides who has which godcards
            gameService.saveGodcards(token2, "PROMETHEUS", "PAN");
            testGame = gameRepository.findByUserToken(token);
            Assert.assertEquals(testGame.getPlayer(1).getGodCard(), Godcard.PROMETHEUS);
            Assert.assertEquals(testGame.getPlayer(2).getGodCard(), Godcard.PAN);

            // player 1 selects himself as startPlayer
            gameService.saveStarter(token, 1);
            testGame = gameRepository.findByUserToken(token);
            Assert.assertEquals(testGame.getPlayer(1).getTurnValue(), TurnValue.SETWORKER);
            Assert.assertEquals(testGame.getPlayer(2).getTurnValue(), TurnValue.NOTHING);

            // player 1 sets his worker
            gameService.turn(token, 1, 1);
            gameService.turn(token, 3, 1);
            testGame = gameRepository.findByUserToken(token);
            Assert.assertEquals(testGame.getPlayer(1).getTurnValue(), TurnValue.END);
            Assert.assertEquals(testGame.getPlayer(2).getTurnValue(), TurnValue.SETWORKER);

            // player 2 sets his worker
            gameService.turn(token2, 1, 3);
            gameService.turn(token2, 3, 3);
            testGame = gameRepository.findByUserToken(token);
            Assert.assertEquals(testGame.getPlayer(1).getTurnValue(), TurnValue.MOVE);
            Assert.assertEquals(testGame.getPlayer(2).getTurnValue(), TurnValue.END);

            // player 1 turn
            gameService.turn(token, 1, 1);
            playerInfo.setTurnValue(TurnValue.BUILD);
            gameService.nextTurn(token, playerInfo);
            gameService.turn(token, 1, 2);
            testGame = gameRepository.findByUserToken(token);
            Assert.assertFalse(testGame.getField(1, 2).getClickable()[0]);
            gameService.turn(token, 2, 1);
            gameService.turn(token, 2, 2);
            testGame = gameRepository.findByUserToken(token);
            Assert.assertEquals(testGame.getPlayer(1).getTurnValue(), TurnValue.END);
            Assert.assertEquals(testGame.getPlayer(2).getTurnValue(), TurnValue.MOVE);

            //player 2 turn
            gameService.turn(token2, 1, 3);
            gameService.turn(token2, 1, 2);
            gameService.turn(token2, 2, 2);
            testGame = gameRepository.findByUserToken(token);
            Assert.assertEquals(testGame.getPlayer(1).getTurnValue(), TurnValue.MOVE);
            Assert.assertEquals(testGame.getPlayer(2).getTurnValue(), TurnValue.END);

            //player 1 turn
            gameService.turn(token, 3, 1);
            playerInfo.setTurnValue(TurnValue.MOVE);
            gameService.nextTurn(token, playerInfo);
            gameService.turn(token, 4, 1);
            gameService.turn(token, 3, 1);
            testGame = gameRepository.findByUserToken(token);
            Assert.assertEquals(testGame.getPlayer(1).getTurnValue(), TurnValue.END);
            Assert.assertEquals(testGame.getPlayer(2).getTurnValue(), TurnValue.MOVE);

            //player 2 turn
            gameService.turn(token2, 1, 2);
            gameService.turn(token2, 2, 2);
            gameService.turn(token2, 1, 1);
            testGame = gameRepository.findByUserToken(token);
            Assert.assertEquals(testGame.getPlayer(1).getTurnValue(), TurnValue.MOVE);
            Assert.assertEquals(testGame.getPlayer(2).getTurnValue(), TurnValue.END);

            //player 1 turn
            gameService.turn(token, 4, 1);
            playerInfo.setTurnValue(TurnValue.MOVE);
            gameService.nextTurn(token, playerInfo);
            gameService.turn(token, 3,1);
            gameService.turn(token, 4, 1);
            testGame = gameRepository.findByUserToken(token);
            Assert.assertEquals(testGame.getPlayer(1).getTurnValue(), TurnValue.END);
            Assert.assertEquals(testGame.getPlayer(2).getTurnValue(), TurnValue.MOVE);

            //player 2 turn
            gameService.turn(token2, 2, 2);
            gameService.turn(token2, 2, 1);
            testGame = gameRepository.findByUserToken(token);
            Assert.assertEquals(testGame.getPlayer(1).getTurnValue(), TurnValue.LOST);
            Assert.assertEquals(testGame.getPlayer(2).getTurnValue(), TurnValue.WON);
        } catch (SantoriniException ex) {
            throw new ResponseStatusException(
                    ex.getException(), ex.toString(), ex);
        }
    }


    @Test
    public void atlasAthena() {
        try {
            String token = testUser.getToken();
            String token2 = testUser2.getToken();
            Player playerInfo = new Player();

            // player 1 selects godcards of game
            gameService.selectGodcards(token, "ATLAS", "ATHENA");

            // player 2 decides who has which godcards
            gameService.saveGodcards(token2, "ATLAS", "ATHENA");
            testGame = gameRepository.findByUserToken(token);
            Assert.assertEquals(testGame.getPlayer(1).getGodCard(), Godcard.ATLAS);
            Assert.assertEquals(testGame.getPlayer(2).getGodCard(), Godcard.ATHENA);

            // player 1 selects himself as startPlayer
            gameService.saveStarter(token, 1);
            testGame = gameRepository.findByUserToken(token);
            Assert.assertEquals(testGame.getPlayer(1).getTurnValue(), TurnValue.SETWORKER);
            Assert.assertEquals(testGame.getPlayer(2).getTurnValue(), TurnValue.NOTHING);


            // player 1 sets his worker
            gameService.turn(token, 1, 0);
            gameService.turn(token, 2, 1);
            testGame = gameRepository.findByUserToken(token);
            Assert.assertEquals(testGame.getPlayer(1).getTurnValue(), TurnValue.END);
            Assert.assertEquals(testGame.getPlayer(2).getTurnValue(), TurnValue.SETWORKER);

            // player 2 sets his worker
            gameService.turn(token2, 0, 0);
            gameService.turn(token2, 0, 3);
            testGame = gameRepository.findByUserToken(token);
            Assert.assertEquals(testGame.getPlayer(1).getTurnValue(), TurnValue.MOVE);
            Assert.assertEquals(testGame.getPlayer(2).getTurnValue(), TurnValue.END);

            // player 1 turn
            gameService.turn(token, 2, 1);
            gameService.turn(token, 2, 2);
            playerInfo.setTurnValue(TurnValue.DOME);
            gameService.nextTurn(token, playerInfo);
            //dome at (1,1)
            gameService.turn(token, 1, 1);
            testGame = gameRepository.findByUserToken(token);
            Assert.assertEquals(testGame.getPlayer(1).getTurnValue(), TurnValue.END);
            Assert.assertEquals(testGame.getPlayer(2).getTurnValue(), TurnValue.MOVE);

            //player 2 turn
            gameService.turn(token2, 0, 3);
            gameService.turn(token2, 0, 2);
            gameService.turn(token2, 0, 3);
            testGame = gameRepository.findByUserToken(token);
            Assert.assertEquals(testGame.getPlayer(1).getTurnValue(), TurnValue.MOVE);
            Assert.assertEquals(testGame.getPlayer(2).getTurnValue(), TurnValue.END);

            //player 1 turn
            gameService.turn(token, 2, 2);
            gameService.turn(token, 1, 3);
            playerInfo.setTurnValue(TurnValue.DOME);
            gameService.nextTurn(token, playerInfo);
            //dome at (1,4)
            gameService.turn(token, 1, 4);
            testGame = gameRepository.findByUserToken(token);
            Assert.assertEquals(testGame.getPlayer(1).getTurnValue(), TurnValue.END);
            Assert.assertEquals(testGame.getPlayer(2).getTurnValue(), TurnValue.MOVE);

            //player 2 turn
            gameService.turn(token2, 0, 2);
            gameService.turn(token2, 0, 1);
            gameService.turn(token2, 0, 2);
            testGame = gameRepository.findByUserToken(token);
            Assert.assertEquals(testGame.getPlayer(1).getTurnValue(), TurnValue.MOVE);
            Assert.assertEquals(testGame.getPlayer(2).getTurnValue(), TurnValue.END);

            //player 1 turn
            gameService.turn(token, 1, 3);
            gameService.turn(token, 0,2);
            playerInfo.setTurnValue(TurnValue.DOME);
            gameService.nextTurn(token, playerInfo);
            //dome at (1,2)
            gameService.turn(token, 1, 2);
            testGame = gameRepository.findByUserToken(token);
            for (Field field: testGame.getBoard()) {
                Assert.assertFalse(field.getClickable()[1]);
            }
            Assert.assertEquals(testGame.getPlayer(1).getTurnValue(), TurnValue.WON);
            Assert.assertEquals(testGame.getPlayer(2).getTurnValue(), TurnValue.LOST);

        } catch (SantoriniException ex) {
            throw new ResponseStatusException(
                    ex.getException(), ex.toString(), ex);
        }
    }

    @Test
    public void hephaestusMinotaur() {
        try {
            String token = testUser.getToken();
            String token2 = testUser2.getToken();

            // player 1 selects godcards of game
            gameService.selectGodcards(token, "HEPHAESTUS", "MINOTAUR");

            // player 2 decides who has which godcards
            gameService.saveGodcards(token2, "HEPHAESTUS", "MINOTAUR");
            testGame = gameRepository.findByUserToken(token);
            Assert.assertEquals(testGame.getPlayer(1).getGodCard(), Godcard.HEPHAESTUS);
            Assert.assertEquals(testGame.getPlayer(2).getGodCard(), Godcard.MINOTAUR);

            // player 1 selects himself as startPlayer
            gameService.saveStarter(token, 1);
            testGame = gameRepository.findByUserToken(token);
            Assert.assertEquals(testGame.getPlayer(1).getTurnValue(), TurnValue.SETWORKER);
            Assert.assertEquals(testGame.getPlayer(2).getTurnValue(), TurnValue.NOTHING);

            // player 1 sets his worker
            gameService.turn(token, 1, 1);
            gameService.turn(token, 3, 1);
            testGame = gameRepository.findByUserToken(token);
            Assert.assertEquals(testGame.getPlayer(1).getTurnValue(), TurnValue.END);
            Assert.assertEquals(testGame.getPlayer(2).getTurnValue(), TurnValue.SETWORKER);

            // player 2 sets his worker
            gameService.turn(token2, 1, 3);
            gameService.turn(token2, 3, 3);
            testGame = gameRepository.findByUserToken(token);
            Assert.assertEquals(testGame.getPlayer(1).getTurnValue(), TurnValue.MOVE);
            Assert.assertEquals(testGame.getPlayer(2).getTurnValue(), TurnValue.END);

            // player 1 turn
            gameService.turn(token, 1, 1);
            gameService.turn(token, 2, 2);
            gameService.turn(token, 3, 2);
            testGame = gameRepository.findByUserToken(token);
            Assert.assertEquals(testGame.getPlayer(1).getTurnValue(), TurnValue.HEPHAESTUSBUILD);
            gameService.hephaestusBuild(token, 2);
            testGame = gameRepository.findByUserToken(token);
            Assert.assertSame(testGame.getField(3, 2).getLevel(), 2);
            Assert.assertEquals(testGame.getPlayer(1).getTurnValue(), TurnValue.END);
            Assert.assertEquals(testGame.getPlayer(2).getTurnValue(), TurnValue.MOVE);

            //player 2 turn
            gameService.turn(token2, 1, 3);
            testGame = gameRepository.findByUserToken(token);
            Assert.assertFalse(testGame.getField(2, 2).getClickable()[1]);
            gameService.turn(token2, 1, 2);
            gameService.turn(token2, 1, 1);
            testGame = gameRepository.findByUserToken(token);
            Assert.assertEquals(testGame.getPlayer(1).getTurnValue(), TurnValue.MOVE);
            Assert.assertEquals(testGame.getPlayer(2).getTurnValue(), TurnValue.END);

            //player 1 turn
            gameService.turn(token, 3, 1);
            gameService.turn(token, 2, 1);
            gameService.turn(token, 1, 1);
            testGame = gameRepository.findByUserToken(token);
            Assert.assertEquals(testGame.getPlayer(1).getTurnValue(), TurnValue.HEPHAESTUSBUILD);
            gameService.hephaestusBuild(token, 2);
            testGame = gameRepository.findByUserToken(token);
            Assert.assertSame(testGame.getField(1, 1).getLevel(), 3);
            Assert.assertEquals(testGame.getPlayer(1).getTurnValue(), TurnValue.END);
            Assert.assertEquals(testGame.getPlayer(2).getTurnValue(), TurnValue.MOVE);

            //player 2 turn
            gameService.turn(token2, 3, 3);
            gameService.turn(token2, 2, 2);
            testGame = gameRepository.findByUserToken(token);
            Assert.assertEquals(testGame.getField(1, 1).getWorker(), testGame.getPlayer(1).getWorker(11));
            Assert.assertEquals(testGame.getField(2, 2).getWorker(), testGame.getPlayer(2).getWorker(22));
            Assert.assertEquals(testGame.getPlayer(1).getTurnValue(), TurnValue.END);
            Assert.assertEquals(testGame.getPlayer(2).getTurnValue(), TurnValue.BUILD);
            gameService.turn(token2, 3, 2);
            testGame = gameRepository.findByUserToken(token);
            Assert.assertEquals(testGame.getPlayer(1).getTurnValue(), TurnValue.MOVE);
            Assert.assertEquals(testGame.getPlayer(2).getTurnValue(), TurnValue.END);

            //player 1 turn
            gameService.turn(token, 2, 1);
            gameService.turn(token, 3,1);
            gameService.turn(token, 2, 1);
            testGame = gameRepository.findByUserToken(token);
            Assert.assertEquals(testGame.getPlayer(1).getTurnValue(), TurnValue.HEPHAESTUSBUILD);
            gameService.hephaestusBuild(token, 2);
            testGame = gameRepository.findByUserToken(token);
            Assert.assertSame(testGame.getField(2, 1).getLevel(), 2);
            Assert.assertEquals(testGame.getPlayer(1).getTurnValue(), TurnValue.END);
            Assert.assertEquals(testGame.getPlayer(2).getTurnValue(), TurnValue.MOVE);

            //player 2 turn
            gameService.turn(token2, 1, 2);
            testGame = gameRepository.findByUserToken(token);
            Assert.assertFalse(testGame.getField(2, 2).getClickable()[1]);
            Assert.assertFalse(testGame.getField(1, 1).getClickable()[1]);
            gameService.turn(token2, 2, 3);
            gameService.turn(token2, 1, 2);
            testGame = gameRepository.findByUserToken(token);
            Assert.assertEquals(testGame.getPlayer(1).getTurnValue(), TurnValue.MOVE);
            Assert.assertEquals(testGame.getPlayer(2).getTurnValue(), TurnValue.END);

            //player 1 turn
            gameService.turn(token, 3, 1);
            gameService.turn(token, 4,1);
            gameService.turn(token, 3, 2);
            testGame = gameRepository.findByUserToken(token);
            Assert.assertEquals(testGame.getPlayer(1).getTurnValue(), TurnValue.END);
            Assert.assertEquals(testGame.getPlayer(2).getTurnValue(), TurnValue.MOVE);

            //player 2 turn
            gameService.turn(token2, 2, 3);
            gameService.turn(token2, 1, 2);
            gameService.turn(token2, 2, 1);
            testGame = gameRepository.findByUserToken(token);
            Assert.assertEquals(testGame.getPlayer(1).getTurnValue(), TurnValue.MOVE);
            Assert.assertEquals(testGame.getPlayer(2).getTurnValue(), TurnValue.END);

            //player 1 turn
            gameService.turn(token, 1, 1);
            gameService.turn(token, 2,1);
            testGame = gameRepository.findByUserToken(token);
            Assert.assertEquals(testGame.getPlayer(1).getTurnValue(), TurnValue.BUILD);
            Assert.assertEquals(testGame.getPlayer(2).getTurnValue(), TurnValue.END);
            gameService.turn(token, 3, 1);
            testGame = gameRepository.findByUserToken(token);
            Assert.assertEquals(testGame.getPlayer(1).getTurnValue(), TurnValue.HEPHAESTUSBUILD);
            gameService.hephaestusBuild(token, 1);
            testGame = gameRepository.findByUserToken(token);
            Assert.assertSame(testGame.getField(3, 1).getLevel(), 1);
            Assert.assertEquals(testGame.getPlayer(1).getTurnValue(), TurnValue.END);
            Assert.assertEquals(testGame.getPlayer(2).getTurnValue(), TurnValue.MOVE);
        } catch (SantoriniException ex) {
            throw new ResponseStatusException(
                    ex.getException(), ex.toString(), ex);
        }
    }

    @Test
    public void athenaHermes() {
        try {
            String token = testUser.getToken();
            String token2 = testUser2.getToken();
            Player playerInfo = new Player();

            // player 1 selects godcards of game
            gameService.selectGodcards(token, "ATHENA", "HERMES");

            // player 2 decides who has which godcards
            gameService.saveGodcards(token2, "ATHENA", "HERMES");
            testGame = gameRepository.findByUserToken(token);
            Assert.assertEquals(testGame.getPlayer(1).getGodCard(), Godcard.ATHENA);
            Assert.assertEquals(testGame.getPlayer(2).getGodCard(), Godcard.HERMES);

            // player 1 selects himself as startPlayer
            gameService.saveStarter(token, 1);
            testGame = gameRepository.findByUserToken(token);
            Assert.assertEquals(testGame.getPlayer(1).getTurnValue(), TurnValue.SETWORKER);
            Assert.assertEquals(testGame.getPlayer(2).getTurnValue(), TurnValue.NOTHING);

            // player 1 sets his worker
            gameService.turn(token, 1, 1);
            gameService.turn(token, 3, 1);
            testGame = gameRepository.findByUserToken(token);
            Assert.assertEquals(testGame.getPlayer(1).getTurnValue(), TurnValue.END);
            Assert.assertEquals(testGame.getPlayer(2).getTurnValue(), TurnValue.SETWORKER);

            // player 2 sets his worker
            gameService.turn(token2, 1, 3);
            gameService.turn(token2, 3, 3);
            testGame = gameRepository.findByUserToken(token);
            Assert.assertEquals(testGame.getPlayer(1).getTurnValue(), TurnValue.MOVE);
            Assert.assertEquals(testGame.getPlayer(2).getTurnValue(), TurnValue.END);

            // player 1 turn
            gameService.turn(token, 1, 1);
            gameService.turn(token, 2, 2);
            gameService.turn(token, 2, 1);
            testGame = gameRepository.findByUserToken(token);
            Assert.assertEquals(testGame.getPlayer(1).getTurnValue(), TurnValue.END);
            Assert.assertEquals(testGame.getPlayer(2).getTurnValue(), TurnValue.BUILDMOVE);

            //player 2 turn
            playerInfo.setTurnValue(TurnValue.MOVE);
            gameService.nextTurn(token2, playerInfo);
            gameService.turn(token2, 1, 3);
            testGame = gameRepository.findByUserToken(token);
            for (Field field : testGame.getBoard()) {
                int x = field.getX();
                int y = field.getY();
                if ((x==2 && (y==1 || y==2)) ||
                        (x==3  && (y==1 || y==3))) {
                    Assert.assertFalse(field.getClickable()[1]);
                } else {
                    Assert.assertTrue(field.getClickable()[1]);
                }
            }
            gameService.turn(token2, 1, 2);
            testGame = gameRepository.findByUserToken(token);
            Assert.assertSame(testGame.getPlayer(2).getTurnValue(), TurnValue.BUILDMOVE);
            playerInfo.setTurnValue(TurnValue.MOVE);
            gameService.nextTurn(token2, playerInfo);
            gameService.turn(token2, 3, 3);
            gameService.turn(token2, 2, 0);
            testGame = gameRepository.findByUserToken(token);
            Assert.assertSame(testGame.getPlayer(2).getTurnValue(), TurnValue.BUILDMOVE);
            playerInfo.setTurnValue(TurnValue.BUILD);
            gameService.nextTurn(token2, playerInfo);
            gameService.turn(token2, 1, 2);
            gameService.turn(token2, 1, 1);
            testGame = gameRepository.findByUserToken(token);
            Assert.assertSame(testGame.getField(1, 1).getLevel(), 1);
            Assert.assertEquals(testGame.getPlayer(1).getTurnValue(), TurnValue.MOVE);
            Assert.assertEquals(testGame.getPlayer(2).getTurnValue(), TurnValue.END);

            //player 1 turn
            gameService.turn(token, 3, 1);
            gameService.turn(token, 2, 1);
            gameService.turn(token, 3, 1);
            testGame = gameRepository.findByUserToken(token);
            Assert.assertSame(testGame.getField(3, 1).getLevel(), 1);
            Assert.assertFalse(testGame.getPlayer(2).getMoveUp());
            Assert.assertEquals(testGame.getPlayer(1).getTurnValue(), TurnValue.END);
            Assert.assertEquals(testGame.getPlayer(2).getTurnValue(), TurnValue.BUILDMOVE);

            //player 2 turn
            playerInfo.setTurnValue(TurnValue.MOVE);
            gameService.nextTurn(token2, playerInfo);
            gameService.turn(token2, 1, 2);
            testGame = gameRepository.findByUserToken(token);
            for (Field field : testGame.getBoard()) {
                int x = field.getX();
                int y = field.getY();
                if ((x==1 && y==1) ||
                        (x==2  && (y==0 || y==1 || y==2)) ||
                        (x==3  && y==1)) {
                    Assert.assertFalse(field.getClickable()[1]);
                } else {
                    Assert.assertTrue(field.getClickable()[1]);
                }
            }
            gameService.turn(token2, 3, 3);
            testGame = gameRepository.findByUserToken(token);
            Assert.assertSame(testGame.getPlayer(2).getTurnValue(), TurnValue.BUILDMOVE);
            playerInfo.setTurnValue(TurnValue.BUILD);
            gameService.nextTurn(token2, playerInfo);
            gameService.turn(token2, 3, 3);
            gameService.turn(token2, 2, 4);
            testGame = gameRepository.findByUserToken(token);
            Assert.assertSame(testGame.getField(2, 4).getLevel(), 1);
            Assert.assertEquals(testGame.getPlayer(1).getTurnValue(), TurnValue.MOVE);
            Assert.assertEquals(testGame.getPlayer(2).getTurnValue(), TurnValue.END);

            //player 1 turn
            gameService.turn(token, 2, 1);
            gameService.turn(token, 3, 0);
            gameService.turn(token, 4, 0);
            testGame = gameRepository.findByUserToken(token);
            Assert.assertSame(testGame.getField(4, 0).getLevel(), 1);
            Assert.assertTrue(testGame.getPlayer(2).getMoveUp());
            Assert.assertEquals(testGame.getPlayer(1).getTurnValue(), TurnValue.END);
            Assert.assertEquals(testGame.getPlayer(2).getTurnValue(), TurnValue.BUILDMOVE);

            //player 2 turn
            playerInfo.setTurnValue(TurnValue.MOVE);
            gameService.nextTurn(token2, playerInfo);
            gameService.turn(token2, 2, 0);
            gameService.turn(token2, 2, 1);
            gameService.turn(token2, 1, 1);
            testGame = gameRepository.findByUserToken(token);
            Assert.assertSame(testGame.getField(1, 1).getLevel(), 2);
            Assert.assertEquals(testGame.getPlayer(1).getTurnValue(), TurnValue.MOVE);
            Assert.assertEquals(testGame.getPlayer(2).getTurnValue(), TurnValue.END);

            //player 1 turn
            gameService.turn(token, 2, 2);
            gameService.turn(token, 1, 3);
            gameService.turn(token, 0, 3);
            testGame = gameRepository.findByUserToken(token);
            Assert.assertSame(testGame.getField(0, 3).getLevel(), 1);
            Assert.assertTrue(testGame.getPlayer(2).getMoveUp());
            Assert.assertEquals(testGame.getPlayer(1).getTurnValue(), TurnValue.END);
            Assert.assertEquals(testGame.getPlayer(2).getTurnValue(), TurnValue.BUILDMOVE);

            //player 2 turn
            playerInfo.setTurnValue(TurnValue.MOVE);
            gameService.nextTurn(token2, playerInfo);
            gameService.turn(token2, 3, 3);
            gameService.turn(token2, 4, 4);
            testGame = gameRepository.findByUserToken(token);
            Assert.assertSame(testGame.getPlayer(2).getTurnValue(), TurnValue.BUILDMOVE);
            playerInfo.setTurnValue(TurnValue.MOVE);
            gameService.nextTurn(token2, playerInfo);
            gameService.turn(token2, 2, 1);
            Assert.assertSame(testGame.getPlayer(2).getTurnValue(), TurnValue.BUILDMOVE);
            playerInfo.setTurnValue(TurnValue.MOVE);
            gameService.nextTurn(token2, playerInfo);
            testGame = gameRepository.findByUserToken(token);
            for (Field field : testGame.getBoard()) {
                int x = field.getX();
                int y = field.getY();
                if ((x==2 && y==1) ||
                        (x==3 && y==1) ||
                        (x==4 && y==0)) {
                    Assert.assertTrue(field.getClickable()[1]);
                } else {
                    Assert.assertFalse(field.getClickable()[1]);
                }
            }
            gameService.turn(token2, 2, 1);
            testGame = gameRepository.findByUserToken(token);
            Assert.assertSame(testGame.getPlayer(2).getTurnValue(), TurnValue.BUILDMOVE);
            playerInfo.setTurnValue(TurnValue.BUILD);
            gameService.nextTurn(token2, playerInfo);
            gameService.turn(token2, 4, 4);
            gameService.turn(token2, 3, 4);
            testGame = gameRepository.findByUserToken(token);
            Assert.assertSame(testGame.getField(3, 4).getLevel(), 1);
            Assert.assertEquals(testGame.getPlayer(1).getTurnValue(), TurnValue.MOVE);
            Assert.assertEquals(testGame.getPlayer(2).getTurnValue(), TurnValue.END);

            //player 1 turn
            gameService.turn(token, 1, 3);
            gameService.turn(token, 2, 4);
            gameService.turn(token, 2, 3);
            testGame = gameRepository.findByUserToken(token);
            Assert.assertSame(testGame.getField(2, 3).getLevel(), 1);
            Assert.assertFalse(testGame.getPlayer(2).getMoveUp());
            Assert.assertEquals(testGame.getPlayer(1).getTurnValue(), TurnValue.END);
            Assert.assertEquals(testGame.getPlayer(2).getTurnValue(), TurnValue.BUILDMOVE);

            //player 2 turn
            playerInfo.setTurnValue(TurnValue.BUILD);
            gameService.nextTurn(token2, playerInfo);
            gameService.turn(token2, 4, 4);
            gameService.turn(token2, 4, 3);
            testGame = gameRepository.findByUserToken(token);
            Assert.assertSame(testGame.getField(4, 3).getLevel(), 1);
            Assert.assertEquals(testGame.getPlayer(1).getTurnValue(), TurnValue.MOVE);
            Assert.assertEquals(testGame.getPlayer(2).getTurnValue(), TurnValue.END);
        } catch (SantoriniException ex) {
            throw new ResponseStatusException(
                    ex.getException(), ex.toString(), ex);
        }
    }
}
