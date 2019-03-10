package com.aa.mtg.game.turn.action;

import com.aa.mtg.cards.CardInstance;
import com.aa.mtg.game.status.GameStatus;
import com.aa.mtg.game.status.GameStatusUpdaterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ResolveService {

    private final GameStatusUpdaterService gameStatusUpdaterService;
    private ContinueTurnService continueTurnService;

    @Autowired
    public ResolveService(GameStatusUpdaterService gameStatusUpdaterService, ContinueTurnService continueTurnService) {
        this.gameStatusUpdaterService = gameStatusUpdaterService;
        this.continueTurnService = continueTurnService;
    }

    public void resolve(GameStatus gameStatus, String triggeredAction, int cardId) {
        if (gameStatus.getTurn().getTriggeredAction().equals(triggeredAction)) {
            if ("DISCARD_A_CARD".equals(triggeredAction)) {
                CardInstance cardInstance = gameStatus.getActivePlayer().getHand().extractCardById(cardId);
                gameStatus.getActivePlayer().getGraveyard().addCard(cardInstance);
                gameStatusUpdaterService.sendUpdateActivePlayerHand(gameStatus);
                gameStatusUpdaterService.sendUpdateActivePlayerGraveyard(gameStatus);
                gameStatus.getTurn().setTriggeredAction(null);
            }
            continueTurnService.continueTurn(gameStatus);

        } else {
            String message = "Cannot resolve triggeredAction " + triggeredAction + " as current triggeredAction is " + gameStatus.getTurn().getTriggeredAction();
            gameStatusUpdaterService.sendMessageToActivePlayer(gameStatus.getActivePlayer(), message);
            throw new RuntimeException(message);
        }
    }
}