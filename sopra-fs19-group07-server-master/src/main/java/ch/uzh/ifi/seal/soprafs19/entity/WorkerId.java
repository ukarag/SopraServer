package ch.uzh.ifi.seal.soprafs19.entity;

import javax.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
class WorkerId implements Serializable {
    private Long playerId;

    private Integer workerNr;

    Long getPlayerId() {
        return playerId;
    }

    void setPlayerId(Long playerId) {
        this.playerId = playerId;
    }

    Integer getWorkerNr() {
        return workerNr;
    }

    void setWorkerNr(Integer workerNr) {
        this.workerNr = workerNr;
    }
}
