package ch.uzh.ifi.seal.soprafs19.turnvalue;

import ch.uzh.ifi.seal.soprafs19.constant.TurnValue;
import ch.uzh.ifi.seal.soprafs19.entity.Player;

public class Nothing extends End implements TurnValueInterface {

    @Override
    public void turn(Player player, Integer x, Integer y) {
        player.setTurnValue(TurnValue.SETWORKER);
    }
}
