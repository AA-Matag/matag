package com.aa.mtg.game.turn.action;

import com.aa.mtg.cards.CardInstance;
import com.aa.mtg.cards.ability.Ability;
import com.aa.mtg.cards.ability.action.AbilityAction;
import com.aa.mtg.cards.ability.action.AbilityActionFactory;
import com.aa.mtg.game.message.MessageException;
import com.aa.mtg.game.status.GameStatus;
import com.aa.mtg.game.status.GameStatusUpdaterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static com.aa.mtg.cards.ability.type.AbilityType.HASTE;
import static com.aa.mtg.cards.properties.Type.CREATURE;

@Service
public class ResolveService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ResolveService.class);

    private final GameStatusUpdaterService gameStatusUpdaterService;
    private final ContinueTurnService continueTurnService;
    private final AbilityActionFactory abilityActionFactory;
    private final EnterCardIntoBattlefieldService enterCardIntoBattlefieldService;

    @Autowired
    public ResolveService(GameStatusUpdaterService gameStatusUpdaterService, ContinueTurnService continueTurnService, AbilityActionFactory abilityActionFactory,
                          EnterCardIntoBattlefieldService enterCardIntoBattlefieldService) {
        this.gameStatusUpdaterService = gameStatusUpdaterService;
        this.continueTurnService = continueTurnService;
        this.abilityActionFactory = abilityActionFactory;
        this.enterCardIntoBattlefieldService = enterCardIntoBattlefieldService;
    }

    public void resolve(GameStatus gameStatus, String triggeredNonStackAction, List<Integer> cardIds) {
        if (!gameStatus.getStack().isEmpty()) {
            Object stackItemToResolve = gameStatus.getStack().remove();

            if (stackItemToResolve instanceof CardInstance) {
                CardInstance cardToResolve = (CardInstance) stackItemToResolve;
                resolveCardInstanceFromStack(gameStatus, cardToResolve);

            } else {

            }

        } else if (gameStatus.getTurn().getTriggeredNonStackAction().equals(triggeredNonStackAction)) {
            resolveTriggeredNonStackAction(gameStatus, triggeredNonStackAction, cardIds);

        } else {
            String message = "Cannot resolve triggeredNonStackAction " + triggeredNonStackAction + " as current triggeredNonStackAction is " + gameStatus.getTurn().getTriggeredNonStackAction();
            throw new MessageException(message);
        }
    }

    private void resolveTriggeredNonStackAction(GameStatus gameStatus, String triggeredNonStackAction, List<Integer> cardIds) {
        switch (triggeredNonStackAction) {
            case "DISCARD_A_CARD": {
                CardInstance cardInstance = gameStatus.getCurrentPlayer().getHand().extractCardById(cardIds.get(0));
                gameStatus.putIntoGraveyard(cardInstance);
                gameStatusUpdaterService.sendUpdateCurrentPlayerHand(gameStatus);
                gameStatusUpdaterService.sendUpdateCurrentPlayerGraveyard(gameStatus);
                gameStatus.getTurn().setTriggeredNonStackAction(null);
                break;
            }
        }
        continueTurnService.continueTurn(gameStatus);
    }

    private void resolveCardInstanceFromStack(GameStatus gameStatus, CardInstance cardToResolve) {
        gameStatusUpdaterService.sendUpdateStack(gameStatus);

        performAbilityAction(gameStatus, cardToResolve);

        if (cardToResolve.isPermanent()) {
            if (cardToResolve.isOfType(CREATURE) && !cardToResolve.hasAbility(HASTE)) {
                cardToResolve.getModifiers().setSummoningSickness(true);
            }

            enterCardIntoBattlefieldService.enter(gameStatus, cardToResolve);

        } else {
            gameStatus.putIntoGraveyard(cardToResolve);
        }

        gameStatusUpdaterService.sendUpdateBattlefields(gameStatus);
        gameStatusUpdaterService.sendUpdateGraveyards(gameStatus);

        gameStatus.getTurn().setCurrentPhaseActivePlayer(gameStatus.getCurrentPlayer().getName());
        gameStatusUpdaterService.sendUpdateTurn(gameStatus);
    }

    private void performAbilityAction(GameStatus gameStatus, CardInstance cardToResolve) {
        for (Ability ability : cardToResolve.getAbilities()) {
            AbilityAction mainAbilityAction = abilityActionFactory.getAbilityAction(ability.getMainAbilityType());
            if (mainAbilityAction != null) {
                try {
                    for (int i = 0; i < ability.getTargets().size(); i++) {
                        ability.getTargets().get(i).check(gameStatus, cardToResolve.getModifiers().getTargets().get(i));
                    }

                    mainAbilityAction.perform(ability, cardToResolve, gameStatus);

                    for (int i = 1; i < ability.getAbilityTypes().size(); i++) {
                        AbilityAction furtherAbilityAction = abilityActionFactory.getAbilityAction(ability.getAbilityTypes().get(i));
                        furtherAbilityAction.perform(ability, cardToResolve, gameStatus);
                    }

                } catch (MessageException e) {
                    LOGGER.info("{}: Target is now invalid during resolution, dropping the action. [{}] ", cardToResolve.getIdAndName(), e.getMessage());
                }
            }
        }

        cardToResolve.getModifiers().setTargets(new ArrayList<>());
    }
}
