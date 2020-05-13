package ch.uzh.ifi.seal.soprafs19.turnvalue;

import ch.uzh.ifi.seal.soprafs19.constant.ExceptionEnum;
import ch.uzh.ifi.seal.soprafs19.constant.TurnValue;
import ch.uzh.ifi.seal.soprafs19.entity.Field;
import ch.uzh.ifi.seal.soprafs19.entity.Game;
import ch.uzh.ifi.seal.soprafs19.entity.Player;
import ch.uzh.ifi.seal.soprafs19.exceptions.SantoriniException;

public class Build extends Move implements TurnValueInterface {

    @Override
    public void turn(Player player, Integer x, Integer y) throws SantoriniException {
        Game game = player.getGame();
        Field field = game.getField(x, y);
        if (field.getWorker() != null || field.getLevel() == 4) {
            throw new SantoriniException(ExceptionEnum.TURN_NOT_ALLOWED);
        }
        field.addLevel();
        player.setChosenWorker(null);
        player.setTurnValue(TurnValue.END);
        game.getPlayer(player.getNumber() == 1 ? 2 : 1).turn(x, y);
        checkNotMovableWon(player);
    }

    @Override
    public void allowedFields(Player player) {
        Game game = player.getGame();
        for (Field field : game.getBoard()) {
            field.setClickable(player.getNumber(),
                    buildable(player.getWorker(
                            player.getChosenWorker()).getField(), field));
        }
    }

    protected boolean buildable(Field workerField, Field askedField) {
        return isNeighbour(workerField, askedField) &&
                askedField.getLevel() < 4 && askedField.getWorker() == null;
    }
}
