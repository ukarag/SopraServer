package ch.uzh.ifi.seal.soprafs19.constant;

import ch.uzh.ifi.seal.soprafs19.entity.Field;
import ch.uzh.ifi.seal.soprafs19.entity.Player;
import ch.uzh.ifi.seal.soprafs19.exceptions.SantoriniException;
import ch.uzh.ifi.seal.soprafs19.godcard.*;

public enum Godcard implements GodcardInterface {
    APOLLO(new Apollo()),
    ATHENA(new Athena()),
    ARTEMIS(new Artemis()),
    ATLAS(new Atlas()),
    HERMES(new Hermes()),
    HEPHAESTUS(new Hephaestus()),
    MINOTAUR(new Minotaur()),
    PROMETHEUS(new Prometheus()),
    DEMETER(new Demeter()),
    PAN(new Pan()),
    NOGODCARD(new NoGodcard());

    private final GodcardInterface godcardInterface;

    Godcard(GodcardInterface godcardInterface) {
        this.godcardInterface = godcardInterface;
    }

    @Override
    public void allowedFields(Player player) {
        godcardInterface.allowedFields(player);
    }

    @Override
    public void turn(Player player, Integer x, Integer y) throws SantoriniException {
        godcardInterface.turn(player, x, y);
    }

    @Override
    public boolean movable(Field workerField, Field askedField) {
        return godcardInterface.movable(workerField, askedField);
    }

    @Override
    public void clearGodcard(Player player) {
        godcardInterface.clearGodcard(player);
    }
}