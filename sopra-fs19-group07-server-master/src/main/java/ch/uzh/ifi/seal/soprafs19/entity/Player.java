package ch.uzh.ifi.seal.soprafs19.entity;

import ch.uzh.ifi.seal.soprafs19.constant.Godcard;
import ch.uzh.ifi.seal.soprafs19.constant.TurnValue;
import ch.uzh.ifi.seal.soprafs19.exceptions.SantoriniException;
import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "player")
public class Player implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String userToken;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "game_id")
    private Game game;

    @Column(nullable = false)
    private Integer number;

    @Column(nullable = false)
    private Boolean moveUp;

    @Column
    private Integer chosenWorker;

    @OneToMany(mappedBy = "player", orphanRemoval = true, fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private List<Worker> workers;

    @Column
    private Godcard godCard;

    @Column(nullable = false)
    private TurnValue turnValue;

    public Player(User user, Integer number) {
        this.user = user;
        this.userToken = this.user.getToken();
        this.number = number;
        this.moveUp = true;
        this.workers = createWorkers(number);
        this.godCard = Godcard.NOGODCARD;
        this.turnValue = TurnValue.NOTHING;
    }

    public Player() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @JsonIgnore
    public String getUserToken() {
        return userToken;
    }

    void setUserToken(String userToken) {
        this.userToken = userToken;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @JsonIgnore
    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public Integer getNumber() {
        return number;
    }

    void setNumber(Integer number) {
        this.number = number;
    }

    @JsonIgnore
    public Boolean getMoveUp() {
        return moveUp;
    }

    public void setMoveUp(Boolean moveUp) {
        this.moveUp = moveUp;
    }

    public Integer getChosenWorker() {
        return chosenWorker;
    }

    public void setChosenWorker(Integer chosenWorker) {
        this.chosenWorker = chosenWorker;
    }

    public List<Worker> getWorkers() {
        return workers;
    }

    public Worker getWorker(Integer number) {
        for (Worker worker : workers)
            if (worker.getWorkerNr().equals(number)) {
                return worker;
            }
        return null;
    }

    private List<Worker> createWorkers(Integer number) {
        List<Worker> result = new ArrayList<>();
        for (int i = 1; i <= 2; i++) {
            Worker worker = new Worker(number == 1 ? i + 10 : i + 20);
            result.add(worker);
            worker.setPlayer(this);
        }
        return result;
    }

    public Godcard getGodCard() {
        return godCard;
    }

    public void setGodCard(Godcard godCard) {
        this.godCard = godCard;
    }

    public TurnValue getTurnValue() {
        return turnValue;
    }

    public void setTurnValue(TurnValue turnValue) {
        this.turnValue = turnValue;
    }

    public void turn(Integer x, Integer y) throws SantoriniException {
        godCard.turn(this, x, y);
    }

    public void allowedFields() {
        godCard.allowedFields(this);
    }
}
