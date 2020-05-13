package ch.uzh.ifi.seal.soprafs19.turnvalue;

import ch.uzh.ifi.seal.soprafs19.entity.Player;

public class NotOnBoard extends End implements TurnValueInterface {

    @Override
    public void turn(Player player, Integer x, Integer y) {
        // no actions on the board
    }
}