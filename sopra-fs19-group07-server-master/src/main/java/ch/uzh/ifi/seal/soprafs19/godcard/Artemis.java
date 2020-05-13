package ch.uzh.ifi.seal.soprafs19.godcard;

import ch.uzh.ifi.seal.soprafs19.constant.TurnValue;
import ch.uzh.ifi.seal.soprafs19.entity.Field;
import ch.uzh.ifi.seal.soprafs19.entity.Game;
import ch.uzh.ifi.seal.soprafs19.entity.Player;
import ch.uzh.ifi.seal.soprafs19.entity.Worker;
import ch.uzh.ifi.seal.soprafs19.exceptions.SantoriniException;
import ch.uzh.ifi.seal.soprafs19.turnvalue.Move;

import java.util.HashMap;

public class Artemis extends Move implements GodcardInterface {

    private final HashMap<Long, Field> startField = new HashMap<>();

    @Override
    public void turn(Player player, Integer x, Integer y) throws SantoriniException {
        if (player.getTurnValue() == TurnValue.MOVE) {
            if (player.getChosenWorker() == null) {
                super.setChosenWorker(player, x, y);
            } else {
                artemisMove(player, x, y);
            }
        } else if (player.getTurnValue() == TurnValue.BUILD) {
            player.getTurnValue().turn(player, x, y);
            startField.remove(player.getGame().getId());
        } else {
            player.getTurnValue().turn(player, x, y);
        }
    }

    @Override
    public void allowedFields(Player player) {
        Game game = player.getGame();
        if (player.getTurnValue() == TurnValue.MOVE) {
            if (!startField.containsKey(game.getId())) {
                super.allowedFields(player);
            } else {
                allowedArtemis(player);
            }
        } else {
            player.getTurnValue().allowedFields(player);
        }
    }

    private void artemisMove(Player player, Integer x, Integer y) throws SantoriniException {
        Game game = player.getGame();
        Worker chosenWorker = player.getWorker(player.getChosenWorker());
        Field workerField = chosenWorker.getField();
        super.move(player, x, y);
        if (!startField.containsKey(game.getId()) && player.getTurnValue() != TurnValue.WON) {
            if(workerMovable(chosenWorker)) {
                startField.put(game.getId(), workerField);
                player.setTurnValue(TurnValue.BUILDMOVE);
            }
            else{
                player.setTurnValue(TurnValue.BUILD);
            }
        }
    }

    private void allowedArtemis(Player player) {
        Game game = player.getGame();
        Field workerField = player.getWorker(player.getChosenWorker()).getField();
        for (Field field : game.getBoard()) {
            Field start = startField.get(game.getId());
            field.setClickable(player.getNumber(), movable(workerField, field) &&
                    !(start.getX().equals(field.getX()) && start.getY().equals(field.getY())));
        }
    }

    @Override
    public void clearGodcard(Player player) {
        startField.remove(player.getGame().getId());
    }
}