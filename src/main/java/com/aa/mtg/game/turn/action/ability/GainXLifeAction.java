package com.aa.mtg.game.turn.action.ability;

import com.aa.mtg.cards.CardInstance;
import com.aa.mtg.cards.ability.action.AbilityAction;
import com.aa.mtg.game.player.Player;
import com.aa.mtg.game.status.GameStatus;
import com.aa.mtg.game.status.GameStatusUpdaterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GainXLifeAction implements AbilityAction {
    private static final Logger LOGGER = LoggerFactory.getLogger(GainXLifeAction.class);

    private final GameStatusUpdaterService gameStatusUpdaterService;

    @Autowired
    public GainXLifeAction(GameStatusUpdaterService gameStatusUpdaterService) {
        this.gameStatusUpdaterService = gameStatusUpdaterService;
    }

    @Override
    public void perform(CardInstance cardInstance, GameStatus gameStatus, String parameter) {
        int lifeToGain = Integer.valueOf(parameter);

        Player currentPlayer = gameStatus.getCurrentPlayer();
        currentPlayer.increaseLife(lifeToGain);
        gameStatusUpdaterService.sendUpdatePlayerLife(gameStatus, currentPlayer);

        LOGGER.info("{} gained {} life.", currentPlayer.getName(), lifeToGain);
    }
}