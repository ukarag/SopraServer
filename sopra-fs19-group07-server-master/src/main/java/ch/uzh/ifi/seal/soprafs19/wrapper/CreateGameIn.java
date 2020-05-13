package ch.uzh.ifi.seal.soprafs19.wrapper;

public class CreateGameIn {
    private String gameName;
    private String player1;
    private Boolean withGodcards;

    public void setGameName(String gameName) {
        this.gameName = gameName;
    }

    public String getGameName() {
        return gameName;
    }

    public void setPlayer1(String player1) {
        this.player1 = player1;
    }

    public String getPlayer1() {
        return player1;
    }

    public void setWithGodcards(Boolean withGodcards) {
        this.withGodcards = withGodcards;
    }

    public Boolean getWithGodcards() {
        return withGodcards;
    }
}
