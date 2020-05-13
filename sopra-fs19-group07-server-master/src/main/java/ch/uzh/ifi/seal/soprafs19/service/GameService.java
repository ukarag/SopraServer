package ch.uzh.ifi.seal.soprafs19.service;

import ch.uzh.ifi.seal.soprafs19.constant.ExceptionEnum;
import ch.uzh.ifi.seal.soprafs19.constant.Godcard;
import ch.uzh.ifi.seal.soprafs19.constant.TurnValue;
import ch.uzh.ifi.seal.soprafs19.entity.Game;
import ch.uzh.ifi.seal.soprafs19.entity.Player;
import ch.uzh.ifi.seal.soprafs19.entity.User;
import ch.uzh.ifi.seal.soprafs19.exceptions.*;
import ch.uzh.ifi.seal.soprafs19.repository.GameRepository;
import ch.uzh.ifi.seal.soprafs19.repository.UserRepository;
import ch.uzh.ifi.seal.soprafs19.wrapper.CreateGameIn;
import ch.uzh.ifi.seal.soprafs19.wrapper.GetGame;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class GameService {

    private final GameRepository gameRepository;
    private final UserRepository userRepository;

    @Autowired
    public GameService(@Qualifier("gameRepository") GameRepository gameRepository,
                       @Qualifier("userRepository") UserRepository userRepository) {
        this.gameRepository = gameRepository;
        this.userRepository = userRepository;
    }

    public Iterable<Game> getGames() {
        return this.gameRepository.findAll();
    }

    public Game createGame(Game newGame) {
        newGame = gameRepository.save(newGame);
        return newGame;
    }

    public Long createGameController(CreateGameIn gameData) throws SantoriniException {
        Game newGame = new Game();
        newGame.setGameName(gameData.getGameName());
        User user = userRepository.findByToken(gameData.getPlayer1());
        if (user == null) {
            throw new SantoriniException(ExceptionEnum.USER_NOT_FOUND);
        }
        Player newPlayer = new Player(user, 1);
        newGame.addPlayer(newPlayer);
        newGame.setWithGodcards(gameData.getWithGodcards());
        newGame = createGame(newGame);
        return newGame.getId();
    }

    public Game getGameById(Long id) throws SantoriniException {
        Game game = this.gameRepository.findById(id).orElse(null);
        if (game == null) {
            throw new SantoriniException(ExceptionEnum.GAME_NOT_FOUND);
        }
        return game;
    }

    public GetGame getMine(String userToken) throws SantoriniException {
        getGameByUserToken(userToken);
        return createGetGame(userToken);
    }

    public Game getGameByUserToken(String userToken) throws SantoriniException {
        Game game = this.gameRepository.findByUserToken(userToken);
        if (game == null) {
            throw new SantoriniException(ExceptionEnum.GAME_NOT_FOUND);
        }
        return game;
    }

    // saves the godcards selected by start player
    public void selectGodcards(String token, String godcard1, String godcard2) throws SantoriniException {
        Game game = this.getGameByUserToken(token);
        game.getGodcards().add(Godcard.valueOf(godcard1));
        game.getGodcards().add(Godcard.valueOf(godcard2));
        gameRepository.save(game);
    }

    public Game joinGame(Long gameId, String userToken) throws SantoriniException {
        Game dbGame = getGameById(gameId);
        User user = userRepository.findByToken(userToken);
        if (user == null) {
            throw new SantoriniException(ExceptionEnum.USER_NOT_FOUND);
        }
        Player newPlayer;
        if (dbGame.getPlayer(1) == null) {
            newPlayer = new Player(user, 1);
        } else if (dbGame.getPlayer(2) == null) {
            if (dbGame.getPlayer(1).getUserToken().equals(userToken)) {
                throw new SantoriniException(ExceptionEnum.PLAYER_ALREADY_IN_GAME);
            }
            newPlayer = new Player(user, 2);
        } else
            throw new SantoriniException(ExceptionEnum.GAME_FULL);
        dbGame.addPlayer(newPlayer);
        gameRepository.save(dbGame);
        return dbGame;
    }

    public void exitGame(Long gameId, String userToken) throws SantoriniException {
        Game dbGame = getGameById(gameId);
        if (dbGame.getPlayer(1) != null && dbGame.getPlayer(1).getUserToken().equals(userToken)) {
            exit(dbGame, 1);
        } else if (dbGame.getPlayer(2).getUserToken().equals(userToken)) {
            exit(dbGame, 2);
        }
        if (dbGame.getPlayers().isEmpty()) {
            gameRepository.deleteById(gameId);
        }
    }

    private void exit(Game game, Integer number) {
        Player player = game.getPlayer(number);
        game.removePlayer(player);
        gameRepository.save(game);
    }

    public void start(Long gameId) throws SantoriniException {
        Game game = getGameById(gameId);
        if (game.getPlayers().size() != 2) {
            throw new SantoriniException(ExceptionEnum.GAME_NOT_FULL);
        }
        game.setStage(1);
        gameRepository.save(game);
    }

    public void fastForward(Long gameId) throws SantoriniException {
        Game game = getGameById(gameId);
        if (game.getPlayers().size() != 2) {
            throw new SantoriniException(ExceptionEnum.GAME_NOT_FULL);
        }
        game.getPlayer(1).setTurnValue(TurnValue.END);
        game.getPlayer(1).setGodCard(Godcard.ATLAS);
        game.getPlayer(2).setTurnValue(TurnValue.MOVE);
        game.getPlayer(2).setGodCard(Godcard.HEPHAESTUS);
        game.getField(0, 2).setWorker(game.getPlayer(2).getWorker(21));
        game.getField(0, 3).setLevel(4);
        game.getField(1, 0).setLevel(3);
        game.getField(2, 1).setWorker(game.getPlayer(1).getWorker(11));
        game.getField(1, 2).setLevel(2);
        game.getField(2, 2).setLevel(4);
        game.getField(2, 3).setLevel(2);
        game.getField(2, 4).setLevel(3);
        game.getField(3, 1).setLevel(2);
        game.getField(3, 1).setWorker(game.getPlayer(1).getWorker(12));
        game.getField(3, 3).setLevel(2);
        game.getField(4, 4).setWorker(game.getPlayer(2).getWorker(22));
        game.getField(4, 1).setLevel(4);
        game.getField(4, 3).setLevel(3);
        game.getField(3, 4).setLevel(2);
        game.setStage(2);
        game.getPlayer(1).allowedFields();
        game.getPlayer(2).allowedFields();
        gameRepository.save(game);
    }

    // saves the godcards in their players
    public GetGame saveGodcards(String token, String godcard1, String godcard2) throws SantoriniException {
        Game game = this.getGameByUserToken(token);
        game.getPlayer(1).setGodCard(Godcard.valueOf(godcard1));
        game.getPlayer(2).setGodCard(Godcard.valueOf(godcard2));
        gameRepository.save(game);
        return createGetGame(token);
    }

    public void saveStarter(String token, Integer startPlayer) throws SantoriniException {
        Game game = getGameByUserToken(token);
        Player player = game.getPlayer(startPlayer);
        player.turn(0, 0);
        player.allowedFields();
        game.setStage(2);
        gameRepository.save(game);
    }

    public GetGame turn(String token, Integer x, Integer y) throws SantoriniException {
        Game game = this.getGameByUserToken(token);
        Player player = game.getPlayer(token);
        player.turn(x, y);
        player.allowedFields();
        if (player.getTurnValue() == TurnValue.END) {
            game.getPlayer(player.getNumber() == 1 ? 2 : 1).allowedFields();
        }
        gameRepository.save(game);
        return createGetGame(token);
    }

    public GetGame nextTurn(String token, Player playerInfo) throws SantoriniException {
        Game game = this.getGameByUserToken(token);
        Player player = game.getPlayer(token);
        player.setTurnValue(playerInfo.getTurnValue());
        player.allowedFields();
        if (playerInfo.getTurnValue() == TurnValue.END) {
            player.setChosenWorker(null);
            Player otherPlayer = game.getPlayer(player.getNumber() == 1 ? 2 : 1);
            otherPlayer.turn(1, 1);
            otherPlayer.allowedFields();
        }
        gameRepository.save(game);
        return createGetGame(token);
    }

    public GetGame hephaestusBuild(String token, Integer level) throws SantoriniException {
        Game game = this.getGameByUserToken(token);
        if (game.getPlayer(token).getTurnValue() != TurnValue.HEPHAESTUSBUILD) {
            throw new SantoriniException(ExceptionEnum.TURN_NOT_ALLOWED);
        }
        return turn(token, 0, level); // x is not used, y is for level
    }

    public void deleteGame(String token) throws SantoriniException {
        Game game = getGameByUserToken(token);
        for (Player player : game.getPlayers()) {
            player.getGodCard().clearGodcard(player);
        }
        gameRepository.deleteById(game.getId());
    }

    private GetGame createGetGame(String token) {
        GetGame result = new GetGame();
        Game game = gameRepository.findByUserToken(token);
        result.setGame(game);
        result.setMyNumber(game.getPlayer(token).getNumber());
        return result;
    }
}
