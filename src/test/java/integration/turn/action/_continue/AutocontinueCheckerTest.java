package integration.turn.action._continue;

import com.matag.cards.Cards;
import com.matag.game.cardinstance.CardInstance;
import com.matag.game.cardinstance.CardInstanceFactory;
import com.matag.game.turn.action._continue.AutocontinueChecker;
import integration.TestUtils;
import integration.turn.action.leave.LeaveTestConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import static com.matag.game.turn.phases.beginning.UpkeepPhase.UP;
import static com.matag.game.turn.phases.combat.DeclareAttackersPhase.DA;
import static com.matag.game.turn.phases.ending.CleanupPhase.CL;
import static com.matag.game.turn.phases.main1.Main1Phase.M1;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {ContinueTestConfiguration.class, LeaveTestConfiguration.class})
public class AutocontinueCheckerTest {
  @Autowired
  private TestUtils testUtils;

  @Autowired
  private AutocontinueChecker autocontinueChecker;

  @Autowired
  private Cards cards;

  @Autowired
  private CardInstanceFactory cardInstanceFactory;

  @Test
  public void canPerformAnyActionReturnsTrueIfM1Player() {
    // Given
    var gameStatus = testUtils.testGameStatus();
    gameStatus.getTurn().setCurrentPhase(M1);
    gameStatus.getPlayer1().getHand().getCards().clear();

    // When
    var result = autocontinueChecker.canPerformAnyAction(gameStatus);

    // Then
    assertThat(result).isTrue();
  }

  @Test
  public void canPerformAnyActionReturnsFalseIfM1Opponent() {
    // Given
    var gameStatus = testUtils.testGameStatus();
    gameStatus.getTurn().setCurrentPhase(M1);
    gameStatus.getTurn().setCurrentTurnPlayer("Player2");
    gameStatus.getPlayer1().getHand().getCards().clear();

    // When
    var result = autocontinueChecker.canPerformAnyAction(gameStatus);

    // Then
    assertThat(result).isFalse();
  }

  @Test
  public void canPerformAnyActionReturnsTrueIfNonStackAction() {
    // Given
    var gameStatus = testUtils.testGameStatus();
    gameStatus.getTurn().setCurrentPhase(CL);
    gameStatus.getTurn().setTriggeredNonStackAction("NonStackAction");
    gameStatus.getPlayer1().getHand().getCards().clear();

    // When
    var result = autocontinueChecker.canPerformAnyAction(gameStatus);

    // Then
    assertThat(result).isTrue();
  }

  @Test
  public void canPerformAnyActionReturnsFalseIfUPAndNoCardsInHandOrBattlefield() {
    // Given
    var gameStatus = testUtils.testGameStatus();
    gameStatus.getTurn().setCurrentPhase(UP);
    gameStatus.getPlayer1().getHand().getCards().clear();

    // When
    var result = autocontinueChecker.canPerformAnyAction(gameStatus);

    // Then
    assertThat(result).isFalse();
  }

  @Test
  public void canPerformAnyActionReturnsTrueIfUPAndAffordableInstantInHand() {
    // Given
    var gameStatus = testUtils.testGameStatus();
    gameStatus.getTurn().setCurrentPhase(UP);
    gameStatus.getPlayer1().getHand().getCards().clear();
    gameStatus.getPlayer1().getBattlefield().addCard(cardInstanceFactory.create(gameStatus, 61, cards.get("Mountain"), "player-name", "player-name"));
    gameStatus.getPlayer1().getHand().addCard(cardInstanceFactory.create(gameStatus, 62, cards.get("Infuriate"), "player-name"));

    // When
    var result = autocontinueChecker.canPerformAnyAction(gameStatus);

    // Then
    assertThat(result).isTrue();
  }

  @Test
  public void canPerformAnyActionReturnsFalseIfUPAndNotAffordableInstantInHand() {
    // Given
    var gameStatus = testUtils.testGameStatus();
    gameStatus.getTurn().setCurrentPhase(UP);
    gameStatus.getPlayer1().getHand().getCards().clear();
    gameStatus.getPlayer1().getBattlefield().addCard(cardInstanceFactory.create(gameStatus, 61, cards.get("Mountain"), "player-name", "player-name"));
    gameStatus.getPlayer1().getBattlefield().getCards().get(0).getModifiers().setTapped(true);
    gameStatus.getPlayer1().getHand().addCard(cardInstanceFactory.create(gameStatus, 62, cards.get("Infuriate"), "player-name"));

    // When
    var result = autocontinueChecker.canPerformAnyAction(gameStatus);

    // Then
    assertThat(result).isFalse();
  }

  @Test
  public void canPerformAnyActionReturnsFalseIfUPAndCardWithTriggeredAbility() {
    // Given
    var gameStatus = testUtils.testGameStatus();
    gameStatus.getTurn().setCurrentPhase(UP);
    gameStatus.getPlayer1().getHand().getCards().clear();
    gameStatus.getPlayer1().getBattlefield().addCard(cardInstanceFactory.create(gameStatus, 62, cards.get("Exclusion Mage"), "player-name"));

    // When
    var result = autocontinueChecker.canPerformAnyAction(gameStatus);

    // Then
    assertThat(result).isFalse();
  }

  @Test
  public void canPerformAnyActionReturnsFalseIfUPAndCardWithNotAffordableActivatedAbility() {
    // Given
    var gameStatus = testUtils.testGameStatus();
    gameStatus.getTurn().setCurrentPhase(UP);
    gameStatus.getPlayer1().getHand().getCards().clear();
    gameStatus.getPlayer1().getBattlefield().addCard(cardInstanceFactory.create(gameStatus, 62, cards.get("Locthwain Gargoyle"), "player-name"));

    // When
    var result = autocontinueChecker.canPerformAnyAction(gameStatus);

    // Then
    assertThat(result).isFalse();
  }

  @Test
  public void canPerformAnyActionReturnsTrueIfUPAndCardAffordableActivatedAbility() {
    // Given
    var gameStatus = testUtils.testGameStatus();
    gameStatus.getTurn().setCurrentPhase(UP);
    gameStatus.getPlayer1().getHand().getCards().clear();
    gameStatus.getPlayer1().getBattlefield().addCard(cardInstanceFactory.create(gameStatus, 62, cards.get("Locthwain Gargoyle"), "player-name"));
    gameStatus.getPlayer1().getBattlefield().addCard(cardInstanceFactory.create(gameStatus, 63, cards.get("Plains"), "player-name"));
    gameStatus.getPlayer1().getBattlefield().addCard(cardInstanceFactory.create(gameStatus, 64, cards.get("Plains"), "player-name"));
    gameStatus.getPlayer1().getBattlefield().addCard(cardInstanceFactory.create(gameStatus, 65, cards.get("Plains"), "player-name"));
    gameStatus.getPlayer1().getBattlefield().addCard(cardInstanceFactory.create(gameStatus, 66, cards.get("Plains"), "player-name"));

    // When
    var result = autocontinueChecker.canPerformAnyAction(gameStatus);

    // Then
    assertThat(result).isTrue();
  }

  @Test
  public void canPerformAnyActionReturnsTrueEvenIfDAButSomethingIsOnTheStackUnacknowledged() {
    // Given
    var gameStatus = testUtils.testGameStatus();
    gameStatus.getTurn().setCurrentPhase(DA);
    gameStatus.getPlayer1().getHand().getCards().clear();
    CardInstance brazenWolves = cardInstanceFactory.create(gameStatus, 62, cards.get("Brazen Wolves"), "player-name");

    brazenWolves.acknowledgedBy("player1");
    gameStatus.getStack().add(brazenWolves);

    // When
    var result = autocontinueChecker.canPerformAnyAction(gameStatus);

    // Then
    assertThat(result).isTrue();
  }

  @Test
  public void canPerformAnyActionReturnsFalseIfDAAndSomethingIsOnTheStackAcknowledgedByBothPlayers() {
    // Given
    var gameStatus = testUtils.testGameStatus();
    gameStatus.getTurn().setCurrentPhase(DA);
    gameStatus.getPlayer1().getHand().getCards().clear();
    CardInstance brazenWolves = cardInstanceFactory.create(gameStatus, 62, cards.get("Brazen Wolves"), "player-name");

    brazenWolves.acknowledgedBy("player1");
    brazenWolves.acknowledgedBy("player2");
    gameStatus.getStack().add(brazenWolves);

    // When
    var result = autocontinueChecker.canPerformAnyAction(gameStatus);

    // Then
    assertThat(result).isFalse();
  }
}