package ch.uzh.ifi.seal.soprafs19.godcard;

import ch.uzh.ifi.seal.soprafs19.constant.TurnValue;
import ch.uzh.ifi.seal.soprafs19.entity.Game;
import ch.uzh.ifi.seal.soprafs19.entity.Player;
import ch.uzh.ifi.seal.soprafs19.exceptions.SantoriniException;

public class Athena extends NoGodcard implements GodcardInterface {

    @Override
    public void turn(Player player, Integer x, Integer y) throws SantoriniException {
        if (player.getTurnValue() == TurnValue.MOVE) {
            Integer chosenWorker = player.getChosenWorker();
            if (chosenWorker != null) {
                Game game = player.getGame();
                game.getPlayer(player.getNumber() == 1 ? 2 : 1).setMoveUp(
                        player.getWorker(chosenWorker).getField().getLevel() >= game.getField(x, y).getLevel());

            }
            super.turn(player, x, y);
        } else {
            player.getTurnValue().turn(player, x, y);
        }
    }
}