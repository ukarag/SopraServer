package ch.uzh.ifi.seal.soprafs19.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "worker")
public class Worker implements Serializable {

    @EmbeddedId
    private final WorkerId id = new WorkerId();

    @OneToOne
    @JoinColumn(name = "field_id")
    private Field field;

    @ManyToOne(optional = false)
    @MapsId("playerId")
    @JoinColumn(name = "player_id")
    private Player player;

    Worker(Integer workerNr) {
        this.id.setWorkerNr(workerNr);
    }

    Worker() {}

    @JsonIgnore
    public Field getField() {
        return field;
    }

    public void setField(Field field) {
        this.field = field;
    }

    @JsonIgnore
    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public Integer getWorkerNr() {
        return id.getWorkerNr();
    }

    void setWorkerNr(Integer workerNr) {
        id.setWorkerNr(workerNr);
    }
}


