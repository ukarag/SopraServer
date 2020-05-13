package ch.uzh.ifi.seal.soprafs19.turnvalue;

import ch.uzh.ifi.seal.soprafs19.constant.TurnValue;
import ch.uzh.ifi.seal.soprafs19.entity.Field;
import ch.uzh.ifi.seal.soprafs19.entity.Game;
import ch.uzh.ifi.seal.soprafs19.entity.Player;

public class End implements TurnValueInterface {

    @Override
    public void turn(Player player, Integer x, Integer y) {
        player.setTurnValue(TurnValue.MOVE);
    }

    @Override
    public void allowedFields(Player player) {
        Game game = player.getGame();
        for (Field field : game.getBoard()) {
            field.setClickable(player.getNumber(), false);
        }
    }
}
