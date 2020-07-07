package com.matag.game.turn.action.selection;

import com.matag.cards.ability.selector.MagicInstanceSelector;
import com.matag.cards.ability.selector.SelectorType;
import com.matag.game.cardinstance.CardInstance;
import com.matag.game.cardinstance.ability.AbilityAction;
import com.matag.game.cardinstance.ability.CardInstanceAbility;
import com.matag.game.status.GameStatus;
import com.matag.game.turn.action.permanent.PermanentGetService;
import com.matag.game.turn.action.player.PlayerGetService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class SelectedPermanentsGetAction implements AbilityAction {
  private final MagicInstancePermanentSelectorService magicInstancePermanentSelectorService;
  private final MagicInstancePlayerSelectorService magicInstancePlayerSelectorService;
  private final PermanentGetService permanentGetService;
  private final PlayerGetService playerGetService;

  @Override
  public void perform(CardInstance cardInstance, GameStatus gameStatus, CardInstanceAbility ability) {
    var magicInstanceSelector = ability.getMagicInstanceSelector();
    if (magicInstanceSelector.getSelectorType() == SelectorType.PLAYER) {
      magicInstancePlayerSelectorService.selectPlayers(gameStatus, cardInstance, magicInstanceSelector)
        .forEach(player -> playerGetService.thatPlayerGets(cardInstance, gameStatus, ability.getParameters(), player));

    } else {
      magicInstancePermanentSelectorService.select(gameStatus, cardInstance, magicInstanceSelector).getCards()
        .forEach(card -> permanentGetService.thatPermanentGets(cardInstance, gameStatus, ability.getParameters(), card));
    }
  }
}
