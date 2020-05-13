package ch.uzh.ifi.seal.soprafs19.turnvalue;

import ch.uzh.ifi.seal.soprafs19.entity.Field;
import ch.uzh.ifi.seal.soprafs19.entity.Player;
import ch.uzh.ifi.seal.soprafs19.exceptions.SantoriniException;

public class Dome extends Build implements TurnValueInterface {

    @Override
    public void turn(Player player, Integer x, Integer y) throws SantoriniException {
        Field field = player.getGame().getField(x, y);
        super.turn(player, x, y);
        player.getGame().getField(x, y).setLevel(player.getGame().getField(x, y).getLevel() - 1);
        field.addDome();
        checkNotMovableWon(player);
    }
}
