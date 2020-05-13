package ch.uzh.ifi.seal.soprafs19.godcard;

import ch.uzh.ifi.seal.soprafs19.constant.ExceptionEnum;
import ch.uzh.ifi.seal.soprafs19.constant.TurnValue;
import ch.uzh.ifi.seal.soprafs19.entity.Field;
import ch.uzh.ifi.seal.soprafs19.entity.Game;
import ch.uzh.ifi.seal.soprafs19.entity.Player;
import ch.uzh.ifi.seal.soprafs19.entity.Worker;
import ch.uzh.ifi.seal.soprafs19.exceptions.SantoriniException;
import ch.uzh.ifi.seal.soprafs19.turnvalue.Move;

import java.util.HashMap;
import java.util.LinkedList;

public class Hermes extends Move implements GodcardInterface {

    private final HashMap<Long, Boolean> moved = new HashMap<>();

    @Override
    public void turn(Player player, Integer x, Integer y) throws SantoriniException {
        switch (player.getTurnValue()) {
            case END:
                player.setTurnValue((TurnValue.BUILDMOVE));
                break;
            case MOVE:
                hermesMove(player, x, y);
                break;
            case BUILD:
                hermesBuild(player, x, y);
                break;
            default:
                player.getTurnValue().turn(player, x, y);
        }
    }

    private void hermesMove(Player player, Integer x, Integer y) throws SantoriniException {
        if (player.getChosenWorker() == null) {
            super.setChosenWorker(player, x, y);
        } else {
            Game game = player.getGame();
            Long gameId = game.getId();
            Field workerField = player.getWorker(player.getChosenWorker()).getField();
            if (!moved.containsKey(gameId) && workerField.getLevel().equals(game.getField(x, y).getLevel())) {
                moved.put(gameId, true);
            }
            super.move(player, x, y);
            if (moved.containsKey(gameId)) {
                player.setTurnValue(TurnValue.BUILDMOVE);
                player.setChosenWorker(null);
            }
        }
    }

    private void hermesBuild(Player player, Integer x, Integer y) throws SantoriniException {
        if (player.getChosenWorker() == null) {
            super.setChosenWorker(player, x, y);
        } else {
            moved.remove(player.getGame().getId());
            player.getTurnValue().turn(player, x, y);
        }
    }

    @Override
    protected void checkMoveAllowed(Player player, Integer x, Integer y) throws SantoriniException {
        allowedHermes(player);
        if (!player.getGame().getField(x, y).getClickable()[player.getNumber() - 1]) {
            throw new SantoriniException(ExceptionEnum.TURN_NOT_ALLOWED);
        }
    }

    @Override
    public void allowedFields(Player player) {
        if (player.getTurnValue() == TurnValue.MOVE) {
            if (player.getChosenWorker() == null) {
                super.allowedWorkers(player);
            } else {
                allowedHermes(player);
            }
        } else if (player.getTurnValue() == TurnValue.BUILD) {
            if (player.getChosenWorker() == null) {
                super.allowedWorkers(player);
            } else {
                player.getTurnValue().allowedFields(player);
            }
        } else {
            player.getTurnValue().allowedFields(player);
        }
    }

    private void allowedHermes(Player player) {
        Game game = player.getGame();
        Field workerField = player.getWorker(player.getChosenWorker()).getField();
        for (Field field : game.getBoard()) {
            field.setClickable(player.getNumber(), false);
        }

        workerField.setClickable(player.getNumber(), true);
        allowedSameHeight(player);

        if (!moved.containsKey(game.getId())) {
            allowedNeighbourFields(player);
        }
    }

    private void allowedNeighbourFields(Player player) {
        Field workerField = player.getWorker(player.getChosenWorker()).getField();
        int x = workerField.getX();
        int y = workerField.getY();
        for (int i = x - 1; i <= x + 1; i++) {
            for (int j = y - 1; j <= y + 1; j++) {
                Field askedField = player.getGame().getField(i, j);
                if (askedField!=null &&
                        super.movable(workerField, askedField)) {
                    askedField.setClickable(player.getNumber(), true);
                }
            }
        }
    }

    // set clickable true for all fields, which are accessible on the same level
    private void allowedSameHeight(Player player) {
        LinkedList<Field> toCheck = new LinkedList<>();
        toCheck.add(player.getWorker(player.getChosenWorker()).getField());
        while (!toCheck.isEmpty()) {
            allowedNeighbours(player, toCheck);
        }
    }

    private void allowedNeighbours(Player player, LinkedList<Field> toCheck) {
        Game game = player.getGame();
        Field examinedField = toCheck.pop();
        int x = examinedField.getX();
        int y = examinedField.getY();
        for (int i = x - 1; i <= x + 1; i++) {
            for (int j = y - 1; j <= y + 1; j++) {
                Field askedField = game.getField(i, j);
                if (askedField!=null &&
                        examinedField.getLevel().equals(askedField.getLevel()) &&
                        !askedField.getClickable()[player.getNumber() - 1] &&
                        askedField.getWorker() == null) {
                    toCheck.add(game.getField(i, j));
                    askedField.setClickable(player.getNumber(), true);
                }
            }
        }
    }

    @Override
    protected boolean workerMovable(Worker worker) {
        boolean movable = false;
        Field workerField = worker.getField();
        int x = workerField.getX();
        int y = workerField.getY();
        for (int i = x - 1; i < x + 2 && !movable; i++) {
            for (int j = y - 1; j < y + 2; j++) {
                Field askedField = worker.getPlayer().getGame().getField(i, j);
                if (askedField!= null &&
                        askedField.getLevel() <= workerField.getLevel()+1 &&
                        (askedField.getWorker()==null ||
                                askedField.getWorker().getPlayer() != worker.getPlayer())) {
                    movable = true;
                    break;
                }
            }
        }
        return movable;
    }

    // return if there is a adjacent field which is allowed to move
    @Override
    public boolean movable(Field workerField, Field askedField) {
        return askedField == workerField ||
                super.movable(workerField, askedField);
    }

    @Override
    public void clearGodcard(Player player) {
        moved.remove(player.getGame().getId());
    }
}