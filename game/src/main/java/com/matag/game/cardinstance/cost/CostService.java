package com.matag.game.cardinstance.cost;

import com.matag.cards.Card;
import com.matag.cards.properties.Cost;
import com.matag.game.cardinstance.CardInstance;
import com.matag.game.cardinstance.CardInstanceSearch;
import com.matag.game.cardinstance.ability.CardInstanceAbility;
import com.matag.game.status.GameStatus;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static com.matag.cards.ability.type.AbilityType.TAP_ADD_MANA;
import static com.matag.cards.ability.type.AbilityType.abilityType;
import static com.matag.cards.properties.Cost.COLORLESS;
import static java.util.stream.Collectors.toList;

@Component
public class CostService {
  public boolean isCastingCostFulfilled(CardInstance cardInstance, String ability, List<Cost> manaPaid) {
    ArrayList<Cost> manaPaidCopy = new ArrayList<>(manaPaid);

    for (Cost cost : getCost(cardInstance.getCard(), ability)) {
      boolean removed = false;

      if (cost == COLORLESS) {
        if (manaPaidCopy.size() > 0) {
          manaPaidCopy.remove(0);
          removed = true;
        }
      } else {
        removed = manaPaidCopy.remove(cost);
      }


      if (!removed) {
        return false;
      }
    }

    return true;
  }

  public boolean canAfford(CardInstance cardInstance, String ability, GameStatus gameStatus) {
    List<CardInstance> cardsThatCanGenerateMana = new CardInstanceSearch(gameStatus.getCurrentPlayer().getBattlefield().getCards())
      .untapped()
      .withFixedAbility(TAP_ADD_MANA)
      .getCards();

    List<List<Cost>> manaOptions = generatePossibleManaOptions(cardsThatCanGenerateMana);

    // try all options
    for (List<Cost> manaOption : manaOptions) {
      if (isCastingCostFulfilled(cardInstance, ability, manaOption)) {
        return true;
      }
    }

    return false;
  }

  public List<List<Cost>> generatePossibleManaOptions(List<CardInstance> cardsThatCanGenerateMana) {
    // calculate number of choices
    int choices = cardsThatCanGenerateMana.stream()
      .map(ci -> ci.getAbilitiesByType(TAP_ADD_MANA))
      .map(List::size)
      .reduce((left, right) -> left * right).orElse(1);

    // populate empty costs
    List<List<Cost>> manaOptions = new ArrayList<>(choices);
    for (int j = 0; j < choices; j++) {
      manaOptions.add(new ArrayList<>());
    }

    // populate choices
    int inverseCumulativeSizes = choices;
    for (int i = 0; i < cardsThatCanGenerateMana.size(); i++) {
      CardInstance instance = cardsThatCanGenerateMana.get(i);
      List<CardInstanceAbility> addManaAbilities = instance.getAbilitiesByType(TAP_ADD_MANA);
      inverseCumulativeSizes /= addManaAbilities.size();

      for (int j = 0; j < choices; j++) {
        int index = (j / inverseCumulativeSizes) % addManaAbilities.size();
        List<Cost> mana = addManaAbilities.get(index).getParameters().stream()
          .map(Cost::valueOf)
          .collect(toList());
        manaOptions.get(j).addAll(mana);
      }
    }

    return manaOptions;
  }

  private static List<Cost> getCost(Card card, String ability) {
    if (ability == null || getAbilityCost(card, ability) == null) {
      return card.getCost();

    } else {
      return getAbilityCost(card, ability);
    }
  }

  private static List<Cost> getAbilityCost(Card card, String ability) {
    return card.getAbilities().stream()
      .findFirst()
      .filter(c -> c.getAbilityType().equals(abilityType(ability)))
      .orElseThrow(() -> new RuntimeException("ability " + ability + " not found on card " + card.getName()))
      .getTrigger()
      .getCost();
  }
}