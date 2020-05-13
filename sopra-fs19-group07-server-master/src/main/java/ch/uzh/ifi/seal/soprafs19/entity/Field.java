package ch.uzh.ifi.seal.soprafs19.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "field")
public class Field implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "x")
    private int x;

    @Column(name = "y")
    private int y;

    private Integer level = 0;

    private final Boolean[] clickable;

    @OneToOne(mappedBy = "field", cascade = CascadeType.ALL)
    private Worker worker = null;

    @ManyToOne(optional = false)
    @JoinColumn(name = "game_id")
    private Game game;

    public Field(Integer x, Integer y) {
        this.x = x;
        this.y = y;
        this.clickable = createClickable();
    }

    public Field() {
        this.clickable = createClickable();
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public void addLevel() {
        if (level < 4)
            this.level++;
    }

    public void addDome() {
        if (level < 3) {
            this.level = 40 + level;
        } else {
            this.level = 4;
        }
    }

    public Boolean[] getClickable() {
        return clickable;
    }

    private Boolean[] createClickable() {
        Boolean[] result = new Boolean[2];
        for (int i = 0; i < 2; i++) {
            result[i] = false;
        }
        return result;
    }

    public void setClickable(int playerNr, Boolean value) {
        this.clickable[playerNr - 1] = value;
    }

    public Worker getWorker() {
        return worker;
    }

    public void setWorker(Worker worker) {
        if (worker == null) {
            if (this.worker != null) {
                this.worker.setField(null);
            }
        } else {
            worker.setField(this);
        }
        this.worker = worker;
    }

    @JsonIgnore
    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public Integer getX() {
        return x;
    }

    public Integer getY() {
        return y;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Field field = (Field) o;
        return field.getX().equals(getX()) &&
                field.getY().equals(getY());
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, getX(), getY());
    }
}
