package ch.uzh.ifi.seal.soprafs19.entity;

import ch.uzh.ifi.seal.soprafs19.constant.Godcard;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "game")
public class Game implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String gameName;

    @Column(nullable = false)
    private Boolean withGodcards;

    @Type(type = "ch.uzh.ifi.seal.soprafs19.constant.Godcard")
    private final List<Godcard> godcards;

    @OneToMany(mappedBy = "game", orphanRemoval = true, fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @Fetch(value = FetchMode.SUBSELECT)
    private final List<Player> players;

    @OneToMany(mappedBy = "game", orphanRemoval = true, fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @Fetch(value = FetchMode.SUBSELECT)
    private final List<Field> board;

    @Column
    private Integer stage = 0;


    public Game() {
        this.board = createBoard();
        this.players = new ArrayList<>();
        this.godcards = new ArrayList<>();

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getGameName() {
        return gameName;
    }

    public void setGameName(String gameName) {
        this.gameName = gameName;
    }

    public List<Player> getPlayers() {
        return players;
    }

    private List<Field> createBoard() {
        List<Field> result = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                Field field = new Field(i, j);
                result.add(field);
                field.setGame(this);
            }
        }
        return result;
    }

    public void addPlayer(Player player) {
        if (getPlayer(player.getNumber()) == null) {
            getPlayers().add(player);
            player.setGame(this);
        }
    }

    public Player getPlayer(Integer number) {
        for (Player player : players)
            if (player.getNumber().equals(number)) {
                return player;
            }
        return null;
    }

    public Player getPlayer(String token) {
        for (Player player : players)
            if (player.getUserToken().equals(token)) {
                return player;
            }
        return null;
    }

    public void removePlayer(Player player) {
        getPlayers().remove(player);
    }

    public Boolean getWithGodcards() {
        return withGodcards;
    }

    public void setWithGodcards(Boolean withGodcards) {
        this.withGodcards = withGodcards;
    }

    public List<Godcard> getGodcards() {
        return godcards;
    }

    public Integer getStage() {
        return stage;
    }

    public void setStage(Integer stage) {
        this.stage = stage;
    }

    public List<Field> getBoard() {
        return board;
    }

    public Field getField(Integer x, Integer y) {
        for (Field field : board)
            if (field.getX().equals(x) && field.getY().equals(y)) {
                return field;
            }
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Game game = (Game) o;
        return getId().equals(game.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
}
