package com.matag.game.turn.phases.beginning;

import com.matag.game.cardinstance.CardInstance;
import com.matag.game.status.GameStatus;
import com.matag.game.turn.action.tap.TapPermanentService;
import com.matag.game.turn.phases.AbstractPhase;
import com.matag.game.turn.phases.Phase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UntapPhase extends AbstractPhase {
  public static final String UT = "UT";

  private final TapPermanentService tapPermanentService;

  @Autowired
  private UpkeepPhase upkeepPhase;

  public UntapPhase(TapPermanentService tapPermanentService) {
    this.tapPermanentService = tapPermanentService;
  }

  @Override
  public String getName() {
    return UT;
  }

  @Override
  public Phase getNextPhase(GameStatus gameStatus) {
    return upkeepPhase;
  }

  @Override
  public void action(GameStatus gameStatus) {
    super.action(gameStatus);

    for (CardInstance cardInstance : gameStatus.getCurrentPlayer().getBattlefield().search().tapped().getCards()) {
      if (cardInstance.getModifiers().isDoesNotUntapNextTurn()) {
        cardInstance.getModifiers().setDoesNotUntapNextTurn(false);
      } else {
        tapPermanentService.untap(gameStatus, cardInstance.getId());
      }
    }

    gameStatus.getCurrentPlayer().getBattlefield().search().withSummoningSickness().getCards()
      .forEach(cardInstance -> cardInstance.getModifiers().setSummoningSickness(false));
  }
}
