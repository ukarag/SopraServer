package ch.uzh.ifi.seal.soprafs19.godcard;

import ch.uzh.ifi.seal.soprafs19.constant.TurnValue;
import ch.uzh.ifi.seal.soprafs19.entity.Field;
import ch.uzh.ifi.seal.soprafs19.entity.Game;
import ch.uzh.ifi.seal.soprafs19.entity.Player;
import ch.uzh.ifi.seal.soprafs19.entity.Worker;
import ch.uzh.ifi.seal.soprafs19.exceptions.SantoriniException;
import ch.uzh.ifi.seal.soprafs19.turnvalue.Move;

public class Apollo extends Move implements GodcardInterface {

    @Override
    public void turn(Player player, Integer x, Integer y) throws SantoriniException {
        if (player.getTurnValue() == TurnValue.MOVE && player.getChosenWorker() != null) {
            apolloMove(player, x, y);
        } else {
            player.getTurnValue().turn(player, x, y);
        }
    }

    @Override
    public void allowedFields(Player player) {
        if (player.getTurnValue() == TurnValue.MOVE) {
            super.allowedFields(player);
        } else {
            player.getTurnValue().allowedFields(player);
        }
    }

    private void apolloMove(Player player, Integer x, Integer y) throws SantoriniException {
        Game game = player.getGame();
        Field chosenWorkerField = player.getWorker(player.getChosenWorker()).getField();
        if (game.getField(x, y).getWorker() != null) {
            Worker opponentWorker = game.getField(x, y).getWorker();
            super.turn(player, x, y);
            chosenWorkerField.setWorker(opponentWorker);
        } else {
            super.turn(player, x, y);
        }
    }

    @Override
    public boolean movable(Field workerField, Field askedField) {
        boolean apolloAllowedField = true;
        if (askedField.getWorker() != null) {
            apolloAllowedField = askedField.getWorker().getPlayer() != workerField.getWorker().getPlayer();
        }
        return isReachableField(workerField, askedField) &&
                apolloAllowedField;
    }

    @Override
    public void clearGodcard(Player player) {
        // no private variable to clear
    }
}