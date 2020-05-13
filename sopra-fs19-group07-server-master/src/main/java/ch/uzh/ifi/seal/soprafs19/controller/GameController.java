package ch.uzh.ifi.seal.soprafs19.controller;

import ch.uzh.ifi.seal.soprafs19.entity.Game;
import ch.uzh.ifi.seal.soprafs19.entity.Player;
import ch.uzh.ifi.seal.soprafs19.exceptions.*;
import ch.uzh.ifi.seal.soprafs19.service.GameService;
import ch.uzh.ifi.seal.soprafs19.wrapper.CreateGameIn;
import ch.uzh.ifi.seal.soprafs19.wrapper.GetGame;
import ch.uzh.ifi.seal.soprafs19.wrapper.GodcardsIn;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@SuppressWarnings("MVCPathVariableInspection")
@RestController
public class GameController {

    private final GameService service;

    GameController(GameService service) {
        this.service = service;
    }

    @GetMapping("/games")
    ResponseEntity<Iterable<Game>> all() {
        return ResponseEntity.status(HttpStatus.OK).body(service.getGames());
    }

    @GetMapping("/games/{gameId}")
    ResponseEntity<Game> one(
            @PathVariable("gameId") Long id) {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(service.getGameById(id));
        } catch (SantoriniException ex) {
            throw new ResponseStatusException(
                    ex.getException(), ex.toString(), ex);
        }
    }

    @GetMapping("/games/mine")
    ResponseEntity<GetGame> mine(
            @RequestHeader("userToken") String userToken) {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(service.getMine(userToken));
        } catch (SantoriniException ex) {
            throw new ResponseStatusException(
                    ex.getException(), ex.toString(), ex);
        }
    }

    @PostMapping("/games")
    ResponseEntity<Long> createGame(@RequestBody CreateGameIn newGame) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(this.service.createGameController(newGame));
        } catch (SantoriniException ex) {
            throw new ResponseStatusException(
                    ex.getException(), ex.toString(), ex);
        }
    }

    @CrossOrigin
    @PutMapping("/games/selectGodcards")
    ResponseEntity<Game> selectGodcards(
            @RequestHeader("userToken") String token, @RequestBody GodcardsIn godcards) {
        try {
            service.selectGodcards(token, godcards.getGodcard1(), godcards.getGodcard2());
            return ResponseEntity.noContent().build();
        } catch (SantoriniException ex) {
            throw new ResponseStatusException(
                    ex.getException(), ex.toString(), ex);
        }
    }

    @CrossOrigin
    @PutMapping("/games/join/{gameId}")
    ResponseEntity<Game> joinGame(
            @RequestHeader("userToken") String userToken, @PathVariable("gameId") Long gameId) {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(service.joinGame(gameId, userToken));
        } catch (SantoriniException ex) {
            throw new ResponseStatusException(
                    ex.getException(), ex.toString(), ex);
        }
    }

    @CrossOrigin
    @PutMapping("/games/exit/{gameId}")
    ResponseEntity<Game> exitGame(
            @RequestHeader("userToken") String userToken, @PathVariable("gameId") Long gameId) {
        try {
            service.exitGame(gameId, userToken);
            return ResponseEntity.noContent().build();
        } catch (SantoriniException ex) {
            throw new ResponseStatusException(
                    ex.getException(), ex.toString(), ex);
        }
    }

    @CrossOrigin
    @PutMapping("/games/start/{gameId}")
    ResponseEntity<Game> start(
            @PathVariable("gameId") Long id) {
        try {
            service.start(id);
            return ResponseEntity.noContent().build();
        } catch (SantoriniException ex) {
            throw new ResponseStatusException(
                    ex.getException(), ex.toString(), ex);
        }
    }

    @CrossOrigin
    @PutMapping("/games/fast_forward/{gameId}")
    ResponseEntity<GetGame> fastForward(
            @PathVariable("gameId") Long id) {
        try {
            service.fastForward(id);
            return ResponseEntity.noContent().build();
        } catch (SantoriniException ex) {
            throw new ResponseStatusException(
                    ex.getException(), ex.toString(), ex);
        }
    }

    @CrossOrigin
    @PutMapping("/games/godcards")
    ResponseEntity<GetGame> saveGodcards(
            @RequestHeader("userToken") String token, @RequestBody GodcardsIn godcards) {
        try {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(service.saveGodcards(token, godcards.getGodcard1(), godcards.getGodcard2()));
        } catch (SantoriniException ex) {
            throw new ResponseStatusException(
                    ex.getException(), ex.toString(), ex);
        }
    }

    @CrossOrigin
    @PutMapping("/games/starter/{startPlayer}")
    ResponseEntity<Game> saveStarter(
            @RequestHeader("userToken") String token, @PathVariable("startPlayer") Integer startPlayer) {
        try {
            service.saveStarter(token, startPlayer);
            return ResponseEntity.noContent().build();
        } catch (SantoriniException ex) {
            throw new ResponseStatusException(
                    ex.getException(), ex.toString(), ex);
        }
    }

    @CrossOrigin
    @PutMapping("/games/turn/{x}/{y}")
    ResponseEntity<GetGame> turn(
            @RequestHeader("userToken") String token, @PathVariable("x") Integer x, @PathVariable("y") Integer y) {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(service.turn(token, x, y));
        } catch (SantoriniException ex) {
            throw new ResponseStatusException(
                    ex.getException(), ex.toString(), ex);
        }
    }

    @CrossOrigin
    @PutMapping("/games/next")
    ResponseEntity<GetGame> nextTurn(
            @RequestBody Player player, @RequestHeader("userToken") String token) {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(service.nextTurn(token, player));
        } catch (SantoriniException ex) {
            throw new ResponseStatusException(
                    ex.getException(), ex.toString(), ex);
        }
    }

    @CrossOrigin
    @PutMapping("/games/hephaestus/{level}")
    ResponseEntity<GetGame> hephaestusTurn(
            @RequestHeader("userToken") String token,
            @PathVariable("level") Integer level) {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(service.hephaestusBuild(token, level));
        } catch (SantoriniException ex) {
            throw new ResponseStatusException(
                    ex.getException(), ex.toString(), ex);
        }
    }

    @CrossOrigin
    @DeleteMapping("/games")
    ResponseEntity<Game> deleteGame(
            @RequestHeader("userToken") String token) {
        try {
            service.deleteGame(token);
            return ResponseEntity.noContent().build();
        } catch (SantoriniException ex) {
            throw new ResponseStatusException(
                    ex.getException(), ex.toString(), ex);
        }
    }
}