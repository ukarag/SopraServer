package ch.uzh.ifi.seal.soprafs19.wrapper;

import ch.uzh.ifi.seal.soprafs19.constant.Godcard;
import ch.uzh.ifi.seal.soprafs19.entity.Field;
import ch.uzh.ifi.seal.soprafs19.entity.Game;
import ch.uzh.ifi.seal.soprafs19.entity.Player;

import java.util.List;

public class GetGame {
    private Long id;
    private String gameName;
    private Boolean withGodcards;
    private List<Godcard> godcards;
    private List<Player> players;
    private Integer stage;
    private List<Field> board;
    private Integer myNumber;

    public void setGame(Game game) {
        this.id = game.getId();
        this.gameName = game.getGameName();
        this.withGodcards = game.getWithGodcards();
        this.godcards = game.getGodcards();
        this.players = game.getPlayers();
        this.stage = game.getStage();
        this.board = game.getBoard();
    }

    public Long getId() {
        return id;
    }

    public String getGameName() {
        return gameName;
    }

    public Boolean getWithGodcards() {
        return withGodcards;
    }

    public List<Godcard> getGodcards() {
        return godcards;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public Integer getStage() {
        return stage;
    }

    public List<Field> getBoard() {
        return board;
    }

    public void setMyNumber(Integer myNumber) {
        this.myNumber = myNumber;
    }

    public Integer getMyNumber() {
        return myNumber;
    }
}
