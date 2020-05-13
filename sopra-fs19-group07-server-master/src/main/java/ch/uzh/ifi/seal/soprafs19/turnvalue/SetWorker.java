package ch.uzh.ifi.seal.soprafs19.turnvalue;

import ch.uzh.ifi.seal.soprafs19.constant.ExceptionEnum;
import ch.uzh.ifi.seal.soprafs19.constant.TurnValue;
import ch.uzh.ifi.seal.soprafs19.entity.Field;
import ch.uzh.ifi.seal.soprafs19.entity.Game;
import ch.uzh.ifi.seal.soprafs19.entity.Player;
import ch.uzh.ifi.seal.soprafs19.entity.Worker;
import ch.uzh.ifi.seal.soprafs19.exceptions.SantoriniException;

public class SetWorker implements TurnValueInterface {

    @Override
    public void turn(Player player, Integer x, Integer y) throws SantoriniException {
        Game game = player.getGame();
        if (game.getField(x, y).getWorker() != null) {
            throw new SantoriniException(ExceptionEnum.TURN_NOT_ALLOWED);
        }
        if (player.getWorker(10 * player.getNumber() + 1).getField() == null) {
            Worker worker = player.getWorker(10 * player.getNumber() + 1);
            game.getField(x, y).setWorker(worker);
        } else {
            Worker worker = player.getWorker(10 * player.getNumber() + 2);
            game.getField(x, y).setWorker(worker);
            player.setTurnValue(TurnValue.END);
            game.getPlayer(player.getNumber() == 1 ? 2 : 1).turn(0, 0);
        }
    }

    @Override
    public void allowedFields(Player player) {
        Game game = player.getGame();
        for (Field field : game.getBoard()) {
            field.setClickable(player.getNumber(), field.getWorker() == null);
        }
    }
}