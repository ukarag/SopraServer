package ch.uzh.ifi.seal.soprafs19.turnvalue;

import ch.uzh.ifi.seal.soprafs19.constant.ExceptionEnum;
import ch.uzh.ifi.seal.soprafs19.constant.TurnValue;
import ch.uzh.ifi.seal.soprafs19.entity.Field;
import ch.uzh.ifi.seal.soprafs19.entity.Game;
import ch.uzh.ifi.seal.soprafs19.entity.Player;
import ch.uzh.ifi.seal.soprafs19.entity.Worker;
import ch.uzh.ifi.seal.soprafs19.exceptions.SantoriniException;

import static java.lang.Math.abs;

public class Move implements TurnValueInterface {

    @Override
    public void turn(Player player, Integer x, Integer y) throws SantoriniException {
        if (player.getChosenWorker() == null) {
            setChosenWorker(player, x, y);
        } else {
            move(player, x, y);
        }
    }

    protected void setChosenWorker(Player player, Integer x, Integer y) throws SantoriniException {
        Game game = player.getGame();
        if (game.getField(x, y).getWorker() == null) {
            throw new SantoriniException(ExceptionEnum.TURN_NOT_ALLOWED);
        }
        player.setChosenWorker(game.getField(x, y).getWorker().getWorkerNr());
    }

    protected void move(Player player, Integer x, Integer y) throws SantoriniException {
        checkMoveAllowed(player, x, y);
        Worker chosenWorker = player.getWorker(player.getChosenWorker());
        Field startField = chosenWorker.getField();
        chosenWorker.getField().setWorker(null);
        Field endField = player.getGame().getField(x, y);
        endField.setWorker(chosenWorker);
        player.setTurnValue(TurnValue.BUILD);
        checkMoveWon(player, startField, endField);
        checkNotMovableWon(player);
    }

    protected void checkMoveAllowed(Player player, Integer x, Integer y) throws SantoriniException {
        if (!movable(player.getWorker(player.getChosenWorker()).getField(), player.getGame().getField(x, y))) {
            throw new SantoriniException(ExceptionEnum.TURN_NOT_ALLOWED);
        }
    }

    @Override
    public void allowedFields(Player player) {
        if (player.getChosenWorker() == null) {
            allowedWorkers(player);
        } else {
            allowedMove(player);
        }
    }

    protected void allowedWorkers(Player player) {
        Game game = player.getGame();
        for (Field field : game.getBoard()) {
            if (field.getWorker() != null && workerMovable(field.getWorker())) {
                field.setClickable(player.getNumber(), player.getWorkers().contains(field.getWorker()));
            } else {
                field.setClickable(player.getNumber(), false);
            }
        }
    }

    private void allowedMove(Player player) {
        Game game = player.getGame();
        int playerNr = player.getNumber();
        Field workerField = player.getWorker(player.getChosenWorker()).getField();
        for (Field field : game.getBoard()) {
            field.setClickable(playerNr, movable(workerField, field));
        }
    }

    private void checkMoveWon(Player player, Field startField, Field endField) {
        if (startField.getLevel() != 3 && endField.getLevel() == 3) {
            player.setTurnValue(TurnValue.WON);
            player.getGame().getPlayer(player.getNumber() == 1 ? 2 : 1).setTurnValue(TurnValue.LOST);
        }
    }

    protected void checkNotMovableWon(Player player) {
        Game game = player.getGame();
        for (Player p : game.getPlayers()) {
            if (!playerMovable(p)) {
                p.setTurnValue(TurnValue.LOST);
                game.getPlayer(p.getNumber() == 1 ? 2 : 1).setTurnValue(TurnValue.WON);
            }
        }
    }

    private boolean playerMovable(Player player) {
        boolean movable = false;
        for (Worker worker : player.getWorkers()) {
            boolean workerMovable = workerMovable(worker);
            if (workerMovable) {
                movable = true;
                break;
            }
        }
        return movable;
    }

    protected boolean workerMovable(Worker worker) {
        boolean movable = false;
        Field workerField = worker.getField();
        int x = workerField.getX();
        int y = workerField.getY();
        for (int i = x - 1; i < x + 2 && !movable; i++) {
            for (int j = y - 1; j < y + 2; j++) {
                Field askedField = worker.getPlayer().getGame().getField(i, j);
                if (askedField!=null && !(i == x && j == y) &&
                        worker.getPlayer().getGodCard().movable(workerField, askedField)) {
                    movable = true;
                    break;
                }
            }
        }
        return movable;
    }

    public boolean movable(Field workerField, Field askedField) {
        return isReachableField(workerField, askedField) &&
                askedField.getWorker() == null;
    }

    protected boolean isReachableField(Field workerField, Field askedField) {
        return isNeighbour(workerField, askedField) &&
                askedField.getLevel() <= workerField.getLevel() + (workerField.getWorker().getPlayer().getMoveUp() ? 1 : 0) &&
                askedField.getLevel() < 4;
    }

    boolean isNeighbour(Field workerField, Field askedField) {
        return abs(workerField.getX() - askedField.getX()) <= 1 &&
                abs(workerField.getY() - askedField.getY()) <= 1;
    }
}