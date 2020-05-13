package ch.uzh.ifi.seal.soprafs19.godcard;

import ch.uzh.ifi.seal.soprafs19.constant.TurnValue;
import ch.uzh.ifi.seal.soprafs19.entity.Field;
import ch.uzh.ifi.seal.soprafs19.entity.Game;
import ch.uzh.ifi.seal.soprafs19.entity.Player;
import ch.uzh.ifi.seal.soprafs19.exceptions.SantoriniException;
import ch.uzh.ifi.seal.soprafs19.turnvalue.Build;

import java.util.HashMap;

public class Hephaestus extends Build implements GodcardInterface {

    private final HashMap<Long, Field> toBuildField = new HashMap<>();

    @Override
    public void turn(Player player, Integer x, Integer y) throws SantoriniException {
        if (player.getTurnValue() == TurnValue.BUILD) {
            Game game = player.getGame();
            Long gameId = game.getId();
            Field gameField = game.getField(x, y);
            if (gameField.getLevel() > 1) {
                super.turn(player, x, y);
            } else {
                toBuildField.put(gameId, gameField);
                player.setTurnValue(TurnValue.HEPHAESTUSBUILD);
            }
        } else if (player.getTurnValue() == TurnValue.HEPHAESTUSBUILD) {
            // use y as level
            hephaestusBuild(player, y);
        } else {
            player.getTurnValue().turn(player, x, y);
        }
    }

    private void hephaestusBuild(Player player, Integer level) throws SantoriniException {
        Game game = player.getGame();
        Long gameId = game.getId();
        Field currentField = toBuildField.get(gameId);
        super.turn(player, currentField.getX(), currentField.getY());
        if (level == 2) {
            game.getField(currentField.getX(), currentField.getY()).addLevel();
            checkNotMovableWon(player);
        }
        toBuildField.remove(gameId);
    }

    @Override
    public void allowedFields(Player player) {
        player.getTurnValue().allowedFields(player);
    }

    @Override
    public void clearGodcard(Player player) {
        toBuildField.remove(player.getGame().getId());
    }
}