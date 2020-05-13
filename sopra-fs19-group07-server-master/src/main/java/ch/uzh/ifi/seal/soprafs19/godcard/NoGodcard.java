package ch.uzh.ifi.seal.soprafs19.godcard;

import ch.uzh.ifi.seal.soprafs19.entity.Player;
import ch.uzh.ifi.seal.soprafs19.exceptions.SantoriniException;
import ch.uzh.ifi.seal.soprafs19.turnvalue.Move;

public class NoGodcard extends Move implements GodcardInterface {

    @Override
    public void allowedFields(Player player) {
        player.getTurnValue().allowedFields(player);
    }

    @Override
    public void turn(Player player, Integer x, Integer y) throws SantoriniException {
        player.getTurnValue().turn(player, x, y);
    }

    @Override
    public void clearGodcard(Player player) {
        // no private variable to clear
    }
}