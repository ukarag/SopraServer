package ch.uzh.ifi.seal.soprafs19.constant;

import ch.uzh.ifi.seal.soprafs19.entity.Player;
import ch.uzh.ifi.seal.soprafs19.exceptions.SantoriniException;
import ch.uzh.ifi.seal.soprafs19.turnvalue.*;

public enum TurnValue implements TurnValueInterface {
    NOTHING(new Nothing()),
    SETWORKER(new SetWorker()),
    MOVE(new Move()),
    END(new End()),
    BUILD(new Build()),
    WON(new NotOnBoard()),
    LOST(new NotOnBoard()),
    BUILDMOVE(new NotOnBoard()),
    BUILDEND(new NotOnBoard()),
    BUILDDOME(new NotOnBoard()),
    DOME(new Dome()),
    HEPHAESTUSBUILD(new NotOnBoard());

    private final TurnValueInterface turnValueInterface;

    TurnValue(TurnValueInterface turnValueInterface) {
        this.turnValueInterface = turnValueInterface;
    }

    @Override
    public void turn(Player player, Integer x, Integer y) throws SantoriniException {
        turnValueInterface.turn(player, x, y);
    }

    @Override
    public void allowedFields(Player player) {
        turnValueInterface.allowedFields(player);
    }
}
