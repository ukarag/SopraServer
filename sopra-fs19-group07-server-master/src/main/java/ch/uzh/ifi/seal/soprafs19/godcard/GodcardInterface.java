package ch.uzh.ifi.seal.soprafs19.godcard;

import ch.uzh.ifi.seal.soprafs19.entity.Field;
import ch.uzh.ifi.seal.soprafs19.entity.Player;
import ch.uzh.ifi.seal.soprafs19.exceptions.SantoriniException;

public interface GodcardInterface {
    void allowedFields(Player player);

    void turn(Player player, Integer x, Integer y) throws SantoriniException;

    boolean movable(Field workerField, Field askedField);

    void clearGodcard(Player player);
}
