package com.aa.mtg.game.turn.action.service;

import com.aa.mtg.game.player.Player;
import com.aa.mtg.game.status.GameStatus;
import com.aa.mtg.game.status.GameStatusUpdaterService;
import com.aa.mtg.game.turn.Turn;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DeclareAttackerService {

    private final GameStatusUpdaterService gameStatusUpdaterService;
    private final ContinueTurnService continueTurnService;

    @Autowired
    public DeclareAttackerService(GameStatusUpdaterService gameStatusUpdaterService, ContinueTurnService continueTurnService) {
        this.gameStatusUpdaterService = gameStatusUpdaterService;
        this.continueTurnService = continueTurnService;
    }

    public void declareAttackers(GameStatus gameStatus, List<Integer> cardIds) {
        Turn turn = gameStatus.getTurn();
        Player currentPlayer = gameStatus.getCurrentPlayer();

        if (!turn.getCurrentPhase().equals("DA")) {
            throw new RuntimeException("Attackers declared during phase: " + turn.getCurrentPhase());
        }

        cardIds.forEach(cardId -> currentPlayer.getBattlefield().findCardById(cardId).checkIfCanAttack());
        cardIds.forEach(cardId -> currentPlayer.getBattlefield().findCardById(cardId).declareAsAttacker());

        gameStatusUpdaterService.sendUpdatePlayerBattlefield(gameStatus, currentPlayer);
        continueTurnService.continueTurn(gameStatus);
    }
}