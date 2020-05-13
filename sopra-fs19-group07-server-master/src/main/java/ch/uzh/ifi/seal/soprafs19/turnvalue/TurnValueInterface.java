package ch.uzh.ifi.seal.soprafs19.turnvalue;

import ch.uzh.ifi.seal.soprafs19.entity.Player;
import ch.uzh.ifi.seal.soprafs19.exceptions.SantoriniException;

public interface TurnValueInterface {
    void turn(Player player, Integer x, Integer y) throws SantoriniException;

    void allowedFields(Player player);
}
