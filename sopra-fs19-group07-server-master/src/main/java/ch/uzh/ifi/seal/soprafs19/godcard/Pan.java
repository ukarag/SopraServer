package ch.uzh.ifi.seal.soprafs19.godcard;

import ch.uzh.ifi.seal.soprafs19.constant.TurnValue;
import ch.uzh.ifi.seal.soprafs19.entity.Game;
import ch.uzh.ifi.seal.soprafs19.entity.Player;
import ch.uzh.ifi.seal.soprafs19.exceptions.SantoriniException;

public class Pan extends NoGodcard implements GodcardInterface {

    @Override
    public void turn(Player player, Integer x, Integer y) throws SantoriniException {
        if (player.getTurnValue() == TurnValue.MOVE && player.getChosenWorker() != null) {
            panMove(player, x, y);
        } else {
            player.getTurnValue().turn(player, x, y);
        }
    }

    private void panMove(Player player, Integer x, Integer y) throws SantoriniException {
        Game game = player.getGame();
        if (player.getWorker(player.getChosenWorker()).getField().getLevel() >= game.getField(x, y).getLevel() + 2) {
            player.setTurnValue(TurnValue.WON);
            game.getPlayer(player.getNumber() == 1 ? 2 : 1).setTurnValue(TurnValue.LOST);
        } else {
            super.move(player, x, y);
        }
    }
}