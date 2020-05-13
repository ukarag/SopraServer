package ch.uzh.ifi.seal.soprafs19.godcard;

import ch.uzh.ifi.seal.soprafs19.constant.TurnValue;
import ch.uzh.ifi.seal.soprafs19.entity.Field;
import ch.uzh.ifi.seal.soprafs19.entity.Game;
import ch.uzh.ifi.seal.soprafs19.entity.Player;
import ch.uzh.ifi.seal.soprafs19.entity.Worker;
import ch.uzh.ifi.seal.soprafs19.exceptions.SantoriniException;
import ch.uzh.ifi.seal.soprafs19.turnvalue.Build;

import java.util.HashMap;

public class Demeter extends Build implements GodcardInterface {

    private final HashMap<Long, Field> builtField = new HashMap<>();

    @Override
    public void turn(Player player, Integer x, Integer y) throws SantoriniException {
        if (player.getTurnValue() == TurnValue.BUILD) {
            Game game = player.getGame();
            game.getField(x, y).addLevel();
            checkNotMovableWon(player);
            Long gameId = game.getId();
            if (!builtField.containsKey(gameId) && checkWorkerBuildable(player.getWorker(player.getChosenWorker()))) {
                builtField.put(gameId, game.getField(x, y));
                player.setTurnValue(TurnValue.BUILDEND);
            } else {
                builtField.remove(player.getGame().getId());
                player.setChosenWorker(null);
                player.setTurnValue(TurnValue.END);
                player.getGame().getPlayer(player.getNumber() == 1 ? 2 : 1).turn(1, 1);
            }
        } else {
            player.getTurnValue().turn(player, x, y);
        }
    }

    @Override
    public void allowedFields(Player player) {
        Game game = player.getGame();
        Long gameId = game.getId();
        if (player.getTurnValue() == TurnValue.BUILD) {
            allowedBuild(player);
        } else if (player.getTurnValue() == TurnValue.END) {
            TurnValue.END.allowedFields(player);
            builtField.remove(gameId);
        } else {
            player.getTurnValue().allowedFields(player);
        }
    }

    private void allowedBuild(Player player) {
        Game game = player.getGame();
        if (builtField.get(game.getId())==null) {
            super.allowedFields(player);
        } else {
            for (Field field : game.getBoard()) {
                field.setClickable(
                        player.getNumber(),
                        buildable(player.getWorker(player.getChosenWorker()).getField(), field));
            }
        }
    }

    private boolean checkWorkerBuildable(Worker worker) {
        boolean buildable = false;
        Field workerField = worker.getField();
        int x = workerField.getX();
        int y = workerField.getY();
        for (int i = x - 1; i < x + 2 && !buildable; i++) {
            for (int j = y - 1; j < y + 2; j++) {
                Field askedField = worker.getPlayer().getGame().getField(i, j);
                if (askedField!=null &&
                        buildable(workerField, askedField)) {
                    buildable = true;
                    break;
                }
            }
        }
        return buildable;
    }

    @Override
    protected boolean buildable(Field workerField, Field askedField) {
        Long gameId = workerField.getGame().getId();
        return super.buildable(workerField, askedField) &&
                (!builtField.containsKey(gameId) ||
                        !(askedField.getX().equals(builtField.get(gameId).getX()) &&
                                askedField.getY().equals(builtField.get(gameId).getY())));
    }


    @Override
    public void clearGodcard(Player player) {
        builtField.remove(player.getGame().getId());
    }
}