package ch.uzh.ifi.seal.soprafs19.godcard;

import ch.uzh.ifi.seal.soprafs19.constant.TurnValue;
import ch.uzh.ifi.seal.soprafs19.entity.Field;
import ch.uzh.ifi.seal.soprafs19.entity.Game;
import ch.uzh.ifi.seal.soprafs19.entity.Player;
import ch.uzh.ifi.seal.soprafs19.entity.Worker;
import ch.uzh.ifi.seal.soprafs19.exceptions.SantoriniException;
import ch.uzh.ifi.seal.soprafs19.turnvalue.Move;

public class Minotaur extends Move implements GodcardInterface {

    @Override
    public void turn(Player player, Integer x, Integer y) throws SantoriniException {
        if (player.getTurnValue() == TurnValue.MOVE && player.getChosenWorker() != null) {
            minotaurMove(player, x, y);
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

    private void minotaurMove(Player player, Integer x, Integer y) throws SantoriniException {
        Game game = player.getGame();
        Field chosenWorkerField = player.getWorker(player.getChosenWorker()).getField();
        if (game.getField(x, y).getWorker() != null) { //here i want to go and there is the opponent worker
            Field opponentWorkerField = game.getField(x, y); //take coordinates/field from that opponent worker
            Worker opponentWorker = opponentWorkerField.getWorker();
            opponentWorkerField.setWorker(null); //set the field=null from opponent worker
            getFieldBehind(chosenWorkerField, opponentWorkerField)
                    .setWorker(opponentWorker); //set opponent worker on his new field (with the formula)
            super.move(player, x, y); //set the my worker as parent class that where i clicked
        } else {
            super.move(player, x, y);
        }
    }

    private Field getFieldBehind(Field a, Field b) {
        int x = a.getX() + 2 * (b.getX() - a.getX());
        int y = a.getY() + 2 * (b.getY() - a.getY());
        return a.getGame().getField(x, y);
    }

    @Override
    public boolean movable(Field workerField, Field askedField) {
        boolean minotaurAllowedField = true;
        if (askedField.getWorker() != null){ //that field where I want to click, if there is an worker
            Field fieldBehind = getFieldBehind(workerField, askedField); //field behind the worker
            minotaurAllowedField = askedField.getWorker().getPlayer() != workerField.getWorker().getPlayer() && //is that worker an opponent worker and not my worker
                    fieldBehind!=null && //is the field behind the opponent in the board
                    fieldBehind.getWorker()==null && //is the field behind the opponent worker free
                    fieldBehind.getLevel()<4; // is the field behind the opponent without a dome
        }
        return isReachableField(workerField, askedField) && //if level change is correct and level<4
                minotaurAllowedField; //and the minotaurAllowedField have to be true to show that field as allowedField
    }

    @Override
    public void clearGodcard(Player player) {
        // no private variable to clear
    }
}