package ch.uzh.ifi.seal.soprafs19.godcard;

import ch.uzh.ifi.seal.soprafs19.constant.TurnValue;
import ch.uzh.ifi.seal.soprafs19.entity.Field;
import ch.uzh.ifi.seal.soprafs19.entity.Player;
import ch.uzh.ifi.seal.soprafs19.exceptions.SantoriniException;
import ch.uzh.ifi.seal.soprafs19.turnvalue.Move;

import java.util.HashMap;

public class Prometheus extends Move implements GodcardInterface {

    private final HashMap<Long, Boolean> firstBuild = new HashMap<>(); // if build before or after moving
    private final HashMap<Long, Boolean> moveUpOk = new HashMap<>(); // am I allowed to move up?

    @Override
    public void turn(Player player, Integer x, Integer y) throws SantoriniException {
        Long gameId = player.getGame().getId();
        if (!firstBuild.containsKey(gameId)) {
            firstBuild.put(gameId, true);
            moveUpOk.put(gameId, true);
        }
        if (player.getTurnValue() == TurnValue.MOVE) {
            if (player.getChosenWorker() == null) {
                super.setChosenWorker(player, x, y);
                player.setTurnValue(TurnValue.BUILDMOVE);
            } else {
                super.move(player, x, y);
                firstBuild.put(gameId, false);
            }
        } else if (player.getTurnValue() == TurnValue.BUILD) {
            if (firstBuild.get(gameId)) {
                player.getGame().getField(x, y).addLevel();
                player.setTurnValue(TurnValue.MOVE);
                moveUpOk.put(gameId, false);
            } else {
                player.getTurnValue().turn(player, x, y);
                moveUpOk.put(gameId, true);
                firstBuild.put(gameId, true);
            }
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

    @Override
    public boolean movable(Field workerField, Field askedField) {
        boolean prometheusAllowedField = moveUpOk.get(workerField.getGame().getId()) ||
                askedField.getLevel() <= workerField.getLevel();
        return prometheusAllowedField && super.movable(workerField, askedField);
    }

    @Override
    public void clearGodcard(Player player) {
        Long gameId = player.getGame().getId();
        firstBuild.remove(gameId);
        moveUpOk.remove(gameId);
    }
}