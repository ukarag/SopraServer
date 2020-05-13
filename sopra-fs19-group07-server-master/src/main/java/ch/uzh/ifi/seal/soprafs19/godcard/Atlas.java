package ch.uzh.ifi.seal.soprafs19.godcard;

import ch.uzh.ifi.seal.soprafs19.constant.TurnValue;
import ch.uzh.ifi.seal.soprafs19.entity.Player;
import ch.uzh.ifi.seal.soprafs19.exceptions.SantoriniException;

public class Atlas extends NoGodcard implements GodcardInterface {

    @Override
    public void turn(Player player, Integer x, Integer y) throws SantoriniException {
        if (player.getTurnValue() == TurnValue.MOVE && player.getChosenWorker() != null) {
            super.move(player, x, y);
            if (player.getTurnValue() != TurnValue.WON) {
                player.setTurnValue(TurnValue.BUILDDOME);
            }
        } else {
            player.getTurnValue().turn(player, x, y);
        }
    }
}