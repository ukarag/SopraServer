package ch.uzh.ifi.seal.soprafs19.service;

import ch.uzh.ifi.seal.soprafs19.Application;
import ch.uzh.ifi.seal.soprafs19.constant.ExceptionEnum;
import ch.uzh.ifi.seal.soprafs19.constant.Godcard;
import ch.uzh.ifi.seal.soprafs19.entity.Game;
import ch.uzh.ifi.seal.soprafs19.entity.Player;
import ch.uzh.ifi.seal.soprafs19.entity.User;
import ch.uzh.ifi.seal.soprafs19.exceptions.*;
import ch.uzh.ifi.seal.soprafs19.repository.GameRepository;
import ch.uzh.ifi.seal.soprafs19.constant.TurnValue;
import ch.uzh.ifi.seal.soprafs19.repository.UserRepository;
import ch.uzh.ifi.seal.soprafs19.wrapper.*;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.server.ResponseStatusException;

/**
 * Test class for the UserResource REST resource.
 *
 * @see GameService
 */
@WebAppConfiguration
@RunWith(SpringRunner.class)
@SpringBootTest(classes= Application.class)
public class GameServiceTest {

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
    private User testUser3;
    private Game testGame;

    @Before
    public void setUp() {
        try {
            testUser = new User();
            testUser.setUsername("testUsername");
            testUser.setPassword("testPassword");
            testUser = userService.createUser(testUser);

            testUser2 = new User();
            testUser2.setUsername("testUsername2");
            testUser2.setPassword("testPassword2");
            testUser2 = userService.createUser(testUser2);

            testUser3 = new User();
            testUser3.setUsername("testUsername3");
            testUser3.setPassword("testPassword3");
            testUser3 = userService.createUser(testUser3);

            testGame = new Game();
            testGame.setGameName("TestGameGetMineName");
            testGame.addPlayer(new Player(testUser, 1));
            testGame.setWithGodcards(false);
            testGame = gameService.createGame(testGame);
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
    public void createGameOk() {
        gameRepository.delete(testGame);

        Game testGame = new Game();
        testGame.setGameName("TestName");
        testGame.addPlayer(new Player(testUser, 1));
        testGame.setWithGodcards(false);

        Game createdGame = gameService.createGame(testGame);
        Assert.assertEquals(createdGame, testGame);
        Assert.assertEquals(createdGame.getGameName(), testGame.getGameName());
        Assert.assertEquals(createdGame.getWithGodcards(), testGame.getWithGodcards());
        Assert.assertEquals(createdGame.getGodcards().size(), 0);
        Assert.assertEquals(createdGame.getPlayer(1), testGame.getPlayer(1));
    }

    @Test
    public void createGameControllerOk() {
        try {
            gameRepository.delete(testGame);

            CreateGameIn gameData = new CreateGameIn();
            gameData.setGameName("TestName");
            gameData.setPlayer1(testUser.getToken());
            gameData.setWithGodcards(false);

            Long gameId = gameService.createGameController(gameData);
            Assert.assertNotNull(gameRepository.findById(gameId).orElse(null));
            Game foundGame = gameRepository.findById(gameId).orElse(null);
            Assert.assertNotNull(foundGame);
            Assert.assertEquals(gameData.getPlayer1(), foundGame.getPlayer(1).getUserToken());
            Assert.assertEquals(gameData.getGameName(), foundGame.getGameName());
            Assert.assertEquals(gameData.getWithGodcards(), foundGame.getWithGodcards());
        } catch (SantoriniException ex) {
            throw new ResponseStatusException(
                    ex.getException(), ex.toString(), ex);
        }
    }

    @Test
    public void createGameControllerNotOk() {
        try {
            CreateGameIn gameData = new CreateGameIn();
            gameData.setGameName("TestName");
            gameData.setPlayer1("invalid token");
            gameData.setWithGodcards(false);

            gameService.createGameController(gameData);
            Assert.fail();
        } catch (SantoriniException ex) {
            if (ex.getEnum()!= ExceptionEnum.USER_NOT_FOUND) {
                throw new ResponseStatusException(
                        ex.getException(), ex.toString(), ex);
            }
        }
    }

    @Test
    public void getGameByIdOk() {
        try {
            Game game = gameService.getGameById(testGame.getId());
            Assert.assertEquals(game, testGame);
        } catch (SantoriniException ex) {
            throw new ResponseStatusException(
                    ex.getException(), ex.toString(), ex);
        }
    }

    @Test
    public void getGameByIdNotOk() {
        try {
            gameService.getGameById(10000L);
            Assert.fail();
        } catch (SantoriniException ex) {
            if (ex.getEnum()!= ExceptionEnum.GAME_NOT_FOUND) {
                throw new ResponseStatusException(
                        ex.getException(), ex.toString(), ex);
            }
        }
    }

    @Test
    public void getMineOk() {
        try {
            GetGame gameData = gameService.getMine(testUser.getToken());
            Assert.assertEquals(gameData.getId(), testGame.getId());
            Assert.assertEquals(gameData.getGameName(), testGame.getGameName());
            Assert.assertEquals(gameData.getWithGodcards(), testGame.getWithGodcards());
            Assert.assertEquals(gameData.getPlayers().get(0).getUser(), testGame.getPlayer(1).getUser());
            Assert.assertEquals(gameData.getStage(), testGame.getStage());
            Assert.assertEquals(gameData.getBoard().get(0).getX(), testGame.getBoard().get(0).getX());
            Assert.assertEquals(gameData.getBoard().get(0).getY(), testGame.getBoard().get(0).getY());
            Assert.assertEquals(gameData.getMyNumber(), testGame.getPlayer(testUser.getToken()).getNumber());
        } catch (SantoriniException ex) {
            throw new ResponseStatusException(
                    ex.getException(), ex.toString(), ex);
        }
    }

    @Test
    public void getMineNotOk() {
        try {
            gameService.getMine("invalidToken");
            Assert.fail();
        } catch (SantoriniException ex) {
            if (ex.getEnum()!= ExceptionEnum.GAME_NOT_FOUND) {
                throw new ResponseStatusException(
                        ex.getException(), ex.toString(), ex);
            }
        }
    }

    @Test
    public void selectGodcardOk() {
        try {
            testGame.setWithGodcards(true);
            gameRepository.save(testGame);
            gameService.selectGodcards(testUser.getToken(), "ATLAS", "HEPHAESTUS");
            Game game = gameService.getGameByUserToken(testUser.getToken());
            Assert.assertSame(game.getGodcards().size(), 2);
            Assert.assertTrue(game.getGodcards().contains(Godcard.ATLAS));
            Assert.assertTrue(game.getGodcards().contains(Godcard.HEPHAESTUS));
        } catch (SantoriniException ex) {
            throw new ResponseStatusException(
                    ex.getException(), ex.toString(), ex);
        }
    }

    @Test
    public void selectGodcardNotOk() {
        try {
            gameService.selectGodcards("invalidToken", "ATLAS", "HEPHAESTUS");
            Assert.fail();
        } catch (SantoriniException ex) {
            if (ex.getEnum()!= ExceptionEnum.GAME_NOT_FOUND) {
                throw new ResponseStatusException(
                        ex.getException(), ex.toString(), ex);
            }
        }
    }

    @Test
    public void getGameByUserTokenOk() {
        try {
            Game game = gameService.getGameByUserToken(testUser.getToken());
            Assert.assertEquals(game, testGame);
            Assert.assertEquals(game.getGameName(), testGame.getGameName());
        } catch (SantoriniException ex) {
            throw new ResponseStatusException(
                    ex.getException(), ex.toString(), ex);
        }
    }

    @Test
    public void getGameByUserTokenNotOk() {
        try {
            gameService.getGameByUserToken("invalidToken");
            Assert.fail();
        } catch (SantoriniException ex) {
            if (ex.getEnum()!= ExceptionEnum.GAME_NOT_FOUND) {
                throw new ResponseStatusException(
                        ex.getException(), ex.toString(), ex);
            }
        }
    }

    @Test
    public void joinGameOk() {
        try {
            Game game = gameService.joinGame(testGame.getId(), testUser2.getToken());
            Assert.assertEquals(game, testGame);
            Assert.assertEquals(game.getGameName(), testGame.getGameName());
            Assert.assertEquals(game.getPlayer(1).getUser(), testGame.getPlayer(1).getUser());
            Assert.assertEquals(game.getPlayer(2).getUser(), testUser2);

            gameService.exitGame(testGame.getId(), testUser.getToken());
            Assert.assertNull(gameRepository.findByUserToken(testUser.getToken()));
            Assert.assertNotNull(testUser);
            Assert.assertNotNull(testGame.getId());
            Game game2 = gameService.joinGame(testGame.getId(), testUser.getToken());
            Assert.assertEquals(game, testGame);
            Assert.assertEquals(game.getGameName(), testGame.getGameName());
            Assert.assertEquals(game2.getPlayer(1).getUser(), testGame.getPlayer(1).getUser());
            Assert.assertEquals(game2.getPlayer(2).getUser(), game.getPlayer(2).getUser());
        } catch (SantoriniException ex) {
            throw new ResponseStatusException(
                    ex.getException(), ex.toString(), ex);
        }
    }

    @Test
    public void joinGameNotOkUserNotFound() {
        try {
            gameService.joinGame(testGame.getId(), "token");
            Assert.fail();
        } catch(SantoriniException ex) {
            if (ex.getEnum()!= ExceptionEnum.USER_NOT_FOUND) {
                throw new ResponseStatusException(
                        ex.getException(), ex.toString(), ex);
            }
        }
    }

    @Test
    public void joinGameNotOkGameNotFound() {
        try {
            gameService.joinGame(1000L, testUser.getToken());
            Assert.fail();
        } catch (SantoriniException ex) {
            if (ex.getEnum()!= ExceptionEnum.GAME_NOT_FOUND) {
                throw new ResponseStatusException(
                        ex.getException(), ex.toString(), ex);
            }
        }
    }

    @Test
    public void joinGameNotOkConflict() {
        try {
            gameService.joinGame(testGame.getId(), testUser2.getToken());
            gameService.joinGame(testGame.getId(), testUser3.getToken());
            Assert.fail();
        } catch (SantoriniException ex) {
            if (ex.getEnum()!= ExceptionEnum.GAME_FULL) {
                throw new ResponseStatusException(
                        ex.getException(), ex.toString(), ex);
            }
        }
    }

    @Test
    public void joinGameNotOkForbidden() {
        try {
            gameService.joinGame(testGame.getId(), testUser.getToken());
            Assert.fail();
        } catch (SantoriniException ex) {
            if (ex.getEnum()!= ExceptionEnum.PLAYER_ALREADY_IN_GAME) {
                throw new ResponseStatusException(
                        ex.getException(), ex.toString(), ex);
            }
        }
    }

    @Test
    public void exitGameOkPlayer1FirstOut() {
        try {
            Long gameId = testGame.getId();
            testGame = gameService.joinGame(gameId, testUser2.getToken());

            gameService.exitGame(gameId, testUser.getToken());
            Game game = gameRepository.findByUserToken(testUser2.getToken());
            Assert.assertNotNull(game);
            Assert.assertEquals(game.getPlayer(2).getUser(), testGame.getPlayer(2).getUser());

            gameService.exitGame(gameId, testUser2.getToken());
            Assert.assertNull(gameRepository.findById(gameId).orElse(null));
        } catch (SantoriniException ex) {
            throw new ResponseStatusException(
                    ex.getException(), ex.toString(), ex);
        }
    }

    @Test
    public void exitGameOkPlayer2FirstOut() {
        try {
            Long gameId = testGame.getId();
            testGame = gameService.joinGame(gameId, testUser2.getToken());

            gameService.exitGame(gameId, testUser2.getToken());
            Game game = gameRepository.findByUserToken(testUser.getToken());
            Assert.assertNotNull(game);
            Assert.assertEquals(game.getPlayer(1).getUser(), testGame.getPlayer(1).getUser());

            gameService.exitGame(gameId, testUser.getToken());
            Assert.assertNull(gameRepository.findById(gameId).orElse(null));
        } catch (SantoriniException ex) {
            throw new ResponseStatusException(
                    ex.getException(), ex.toString(), ex);
        }
    }

    @Test
    public void exitGameOkPlayerNotInGame() {
        try {
            Long gameId = testGame.getId();
            testGame = gameService.joinGame(gameId, testUser2.getToken());

            gameService.exitGame(gameId, testUser3.getToken());
            Game game = gameRepository.findByUserToken(testUser.getToken());
            Assert.assertNotNull(game);
            Assert.assertEquals(game.getPlayer(1).getUser(), testGame.getPlayer(1).getUser());
            Assert.assertEquals(game.getPlayer(2).getUser(), testGame.getPlayer(2).getUser());
        } catch (SantoriniException ex) {
            throw new ResponseStatusException(
                    ex.getException(), ex.toString(), ex);
        }
    }

    @Test
    public void exitGameNotOk() {
        try {
            gameService.exitGame(1000L, testUser.getToken());
            Assert.fail();
        } catch (SantoriniException ex) {
            if (ex.getEnum()!= ExceptionEnum.GAME_NOT_FOUND) {
                throw new ResponseStatusException(
                        ex.getException(), ex.toString(), ex);
            }
        }
    }

    @Test
    public void startOk() {
        try {
            testGame = gameService.joinGame(testGame.getId(), testUser2.getToken());
            Assert.assertSame(testGame.getStage(),0);

            gameService.start(testGame.getId());
            Game game = gameRepository.findByUserToken(testUser.getToken());
            Assert.assertSame(game.getStage(),1);
        } catch (SantoriniException ex) {
            throw new ResponseStatusException(
                    ex.getException(), ex.toString(), ex);
        }
    }

    @Test
    public void startNotOkNotFound() {
        try {
            gameService.start(1000L);
            Assert.fail();
        } catch (SantoriniException ex) {
            if (ex.getEnum()!= ExceptionEnum.GAME_NOT_FOUND) {
                throw new ResponseStatusException(
                        ex.getException(), ex.toString(), ex);
            }
        }
    }

    @Test
    public void startNotOkForbidden() {
        try {
            Assert.assertEquals(testGame.getPlayers().size(), 1);

            gameService.start(testGame.getId());
            Assert.fail();
        } catch (SantoriniException ex) {
            if (ex.getEnum()!= ExceptionEnum.GAME_NOT_FULL) {
                throw new ResponseStatusException(
                        ex.getException(), ex.toString(), ex);
            }
        }
    }

    @Test
    public void fastForwardOk() {
        try {
            testGame = gameService.joinGame(testGame.getId(), testUser2.getToken());
            Assert.assertSame(testGame.getStage(),0);

            gameService.fastForward(testGame.getId());
            Game game = gameRepository.findByUserToken(testUser.getToken());
            Assert.assertSame(game.getStage(),2);
        } catch (SantoriniException ex) {
            throw new ResponseStatusException(
                    ex.getException(), ex.toString(), ex);
        }
    }

    @Test
    public void fastForwardNotOkNotFound() {
        try {
            gameService.fastForward(1000L);
            Assert.fail();
        } catch (SantoriniException ex) {
            if (ex.getEnum()!= ExceptionEnum.GAME_NOT_FOUND) {
                throw new ResponseStatusException(
                        ex.getException(), ex.toString(), ex);
            }
        }
    }

    @Test
    public void fastForwardNotOkForbidden() {
        try {
            Assert.assertEquals(testGame.getPlayers().size(), 1);
            gameService.fastForward(testGame.getId());
            Assert.fail();
        } catch (SantoriniException ex) {
            if (ex.getEnum()!= ExceptionEnum.GAME_NOT_FULL) {
                throw new ResponseStatusException(
                        ex.getException(), ex.toString(), ex);
            }
        }
    }

    @Test
    public void saveGodcardsOk() {
        try {
            gameService.joinGame(testGame.getId(), testUser2.getToken());

            GetGame gameData = gameService.saveGodcards(testUser.getToken(), "ATLAS", "HEPHAESTUS");
            Assert.assertEquals(gameData.getPlayers().get(0).getGodCard(), Godcard.ATLAS);
            Assert.assertEquals(gameData.getPlayers().get(1).getGodCard(), Godcard.HEPHAESTUS);
        } catch (SantoriniException ex) {
            throw new ResponseStatusException(
                    ex.getException(), ex.toString(), ex);
        }
    }

    @Test
    public void saveGodcardsNotOk() {
        try {
            gameService.saveGodcards("invalidToken", "ATLAS", "HEPHAESTUS");
            Assert.fail();
        } catch (SantoriniException ex) {
            if (ex.getEnum()!= ExceptionEnum.GAME_NOT_FOUND) {
                throw new ResponseStatusException(
                        ex.getException(), ex.toString(), ex);
            }
        }
    }

    @Test
    public void saveStarterOk() {
        try {
            gameService.joinGame(testGame.getId(), testUser2.getToken());

            gameService.saveStarter(testUser.getToken(), 1);
            Game game = gameRepository.findByUserToken(testUser2.getToken());
            Assert.assertEquals(game.getPlayer(1).getTurnValue(), TurnValue.SETWORKER);
            Assert.assertEquals(game.getPlayer(2).getTurnValue(), TurnValue.NOTHING);
        } catch (SantoriniException ex) {
            throw new ResponseStatusException(
                    ex.getException(), ex.toString(), ex);
        }
    }

    @Test
    public void saveStarterNotOk() {
        try {
            gameService.saveStarter("invalidToken", 1);
            Assert.fail();
        } catch (SantoriniException ex) {
            if (ex.getEnum()!= ExceptionEnum.GAME_NOT_FOUND) {
                throw new ResponseStatusException(
                        ex.getException(), ex.toString(), ex);
            }
        }
    }

    @Test
    public void turnOkNotEnd() {
        try {
            gameService.joinGame(testGame.getId(), testUser2.getToken());
            testGame = gameRepository.findByUserToken(testUser.getToken());
            testGame.getPlayer(1).setTurnValue(TurnValue.SETWORKER);
            gameRepository.save(testGame);

            gameService.turn(testUser.getToken(), 1, 1);
            Game game = gameRepository.findByUserToken(testUser.getToken());
            Assert.assertEquals(game.getPlayer(1).getTurnValue(), TurnValue.SETWORKER);
            Assert.assertEquals(game.getPlayer(2).getTurnValue(), TurnValue.NOTHING);
        } catch (SantoriniException ex) {
            throw new ResponseStatusException(
                    ex.getException(), ex.toString(), ex);
        }
    }

    @Test
    public void turnOkEnd() {
        try {
            gameService.joinGame(testGame.getId(), testUser2.getToken());
            testGame = gameRepository.findByUserToken(testUser.getToken());
            testGame.getPlayer(1).setTurnValue(TurnValue.BUILD);
            testGame.getPlayer(2).setTurnValue(TurnValue.END);
            testGame.getField(2, 2).setWorker(testGame.getPlayer(1).getWorker(11));
            testGame.getField(0, 0).setWorker(testGame.getPlayer(1).getWorker(12));
            testGame.getField(0, 1).setWorker(testGame.getPlayer(2).getWorker(21));
            testGame.getField(0, 2).setWorker(testGame.getPlayer(2).getWorker(22));
            testGame.getPlayer(1).setChosenWorker(11);
            gameRepository.save(testGame);

            gameService.turn(testUser.getToken(), 1, 1);
            Game game = gameRepository.findByUserToken(testUser.getToken());
            Assert.assertEquals(game.getPlayer(1).getTurnValue(), TurnValue.END);
            Assert.assertEquals(game.getPlayer(2).getTurnValue(), TurnValue.MOVE);
        } catch (SantoriniException ex) {
            throw new ResponseStatusException(
                    ex.getException(), ex.toString(), ex);
        }
    }

    @Test
    public void turnNotOkNotFound() {
        try {
            gameService.turn("invalidToken", 1, 1);
            Assert.fail();
        } catch (SantoriniException ex) {
            if (ex.getEnum()!= ExceptionEnum.GAME_NOT_FOUND) {
                throw new ResponseStatusException(
                        ex.getException(), ex.toString(), ex);
            }
        }
    }

    @Test
    public void turnNotOkForbidden() {
        try {
            gameService.joinGame(testGame.getId(), testUser2.getToken());
            testGame = gameRepository.findByUserToken(testUser.getToken());
            testGame.getPlayer(1).setTurnValue(TurnValue.SETWORKER);
            gameRepository.save(testGame);

            gameService.turn(testUser.getToken(), 1, 1);
            gameService.turn(testUser.getToken(), 1, 1);
            Assert.fail();
        } catch (SantoriniException ex) {
            if (ex.getEnum()!= ExceptionEnum.TURN_NOT_ALLOWED) {
                throw new ResponseStatusException(
                        ex.getException(), ex.toString(), ex);
            }
        }
    }

    @Test
    public void nextTurnOkNotEnd() {
        try {
            gameService.joinGame(testGame.getId(), testUser2.getToken());
            testGame = gameRepository.findByUserToken(testUser.getToken());
            testGame.getPlayer(1).setTurnValue(TurnValue.BUILDMOVE);
            testGame.getPlayer(2).setTurnValue(TurnValue.END);
            testGame.getField(2, 2).setWorker(testGame.getPlayer(1).getWorker(11));
            testGame.getPlayer(1).setChosenWorker(11);
            gameRepository.save(testGame);

            Player playerInfo = new Player();
            playerInfo.setTurnValue(TurnValue.BUILD);

            gameService.nextTurn(testUser.getToken(), playerInfo);
            Game game = gameRepository.findByUserToken(testUser.getToken());
            Assert.assertEquals(game.getPlayer(1).getTurnValue(), TurnValue.BUILD);
            Assert.assertEquals(game.getPlayer(2).getTurnValue(), TurnValue.END);
        } catch (SantoriniException ex) {
            throw new ResponseStatusException(
                    ex.getException(), ex.toString(), ex);
        }
    }

    @Test
    public void nextTurnOkEnd() {
        try {
            gameService.joinGame(testGame.getId(), testUser2.getToken());
            testGame = gameRepository.findByUserToken(testUser.getToken());
            testGame.getPlayer(1).setTurnValue(TurnValue.BUILDEND);
            testGame.getPlayer(2).setTurnValue(TurnValue.END);
            testGame.getField(2, 2).setWorker(testGame.getPlayer(1).getWorker(11));
            testGame.getPlayer(1).setChosenWorker(11);
            gameRepository.save(testGame);

            Player playerInfo = new Player();
            playerInfo.setTurnValue(TurnValue.END);

            gameService.nextTurn(testUser.getToken(), playerInfo);
            Game game = gameRepository.findByUserToken(testUser.getToken());
            Assert.assertEquals(game.getPlayer(1).getTurnValue(), TurnValue.END);
            Assert.assertEquals(game.getPlayer(2).getTurnValue(), TurnValue.MOVE);
        } catch (SantoriniException ex) {
            throw new ResponseStatusException(
                    ex.getException(), ex.toString(), ex);
        }
    }

    @Test
    public void nextTurnNotOk() {
        try {
            gameService.nextTurn("invalidToken", new Player());
            Assert.fail();
        } catch (SantoriniException ex) {
            if (ex.getEnum()!= ExceptionEnum.GAME_NOT_FOUND) {
                throw new ResponseStatusException(
                        ex.getException(), ex.toString(), ex);
            }
        }
    }

    @Test
    public void hephaestusBuildOk() {
        try {
            gameService.joinGame(testGame.getId(), testUser2.getToken());
            testGame = gameRepository.findByUserToken(testUser.getToken());
            testGame.getPlayer(1).setGodCard(Godcard.HEPHAESTUS);
            testGame.getPlayer(2).setTurnValue(TurnValue.END);
            testGame.getField(2, 2).setWorker(testGame.getPlayer(1).getWorker(11));
            testGame.getField(0, 0).setWorker(testGame.getPlayer(1).getWorker(12));
            testGame.getField(0, 1).setWorker(testGame.getPlayer(2).getWorker(21));
            testGame.getField(0, 2).setWorker(testGame.getPlayer(2).getWorker(22));
            testGame.getPlayer(1).setChosenWorker(testGame.getField(2, 2).getWorker().getWorkerNr());
            testGame.getPlayer(1).setTurnValue(TurnValue.BUILD);
            testGame.getPlayer(1).turn(1,2); // get field (1,2) saved for building
            gameRepository.save(testGame);

            gameService.hephaestusBuild(testUser.getToken(), 2);
            Game game = gameService.getGameByUserToken(testUser.getToken());
            Assert.assertSame(game.getField(1, 2).getLevel(), 2);
            Assert.assertEquals(game.getPlayer(1).getTurnValue(), TurnValue.END);
        } catch (SantoriniException ex) {
            throw new ResponseStatusException(
                    ex.getException(), ex.toString(), ex);
        }
    }

    @Test
    public void hephaestusBuildNotOkForbidden() {
        try {
            gameService.hephaestusBuild(testUser.getToken(), 2);
            Assert.fail();
        } catch (SantoriniException ex) {
            if (ex.getEnum()!= ExceptionEnum.TURN_NOT_ALLOWED) {
                throw new ResponseStatusException(
                        ex.getException(), ex.toString(), ex);
            }
        }
    }

    @Test
    public void hephaestusBuildNotOkNotFound() {
        try {
            gameService.hephaestusBuild("invalidToken", 2);
            Assert.fail();
        } catch (SantoriniException ex) {
            if (ex.getEnum()!= ExceptionEnum.GAME_NOT_FOUND) {
                throw new ResponseStatusException(
                        ex.getException(), ex.toString(), ex);
            }
        }
    }

    @Test
    public void deleteGameOk() {
        try {
            gameService.deleteGame(testUser.getToken());
            Assert.assertNull(gameRepository.findByUserToken(testUser.getToken()));
        } catch (SantoriniException ex) {
            throw new ResponseStatusException(
                    ex.getException(), ex.toString(), ex);
        }
    }

    @Test
    public void deleteGameNotOk() {
        try {gameService.deleteGame("token");
            Assert.fail();
        } catch (SantoriniException ex) {
            if (ex.getEnum()!= ExceptionEnum.GAME_NOT_FOUND) {
                throw new ResponseStatusException(
                        ex.getException(), ex.toString(), ex);
            }
        }
    }
}
