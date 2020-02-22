package com.aa.mtg.cards;

import com.aa.mtg.cards.properties.Color;
import com.aa.mtg.cards.properties.Rarity;
import com.aa.mtg.cards.properties.Type;

import java.util.List;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static com.aa.mtg.cards.ability.type.AbilityType.TAP_ADD_MANA;
import static com.aa.mtg.cards.properties.Cost.COLORLESS;
import static java.util.Collections.emptyList;

public class CardUtils {
  public static Card hiddenCard() {
    return new Card("card", new TreeSet<>(), emptyList(), new TreeSet<>(), new TreeSet<>(), Rarity.COMMON, "", 0, 0, emptyList());
  }

  public static List<Color> colorsOfManaThatCanGenerate(Card card) {
    return card.getAbilities().stream()
            .filter(ability -> ability.getAbilityType().equals(TAP_ADD_MANA))
            .flatMap(ability -> ability.getParameters().stream())
            .map(Color::valueOf)
            .collect(Collectors.toList());
  }

  public static boolean isColorless(Card card) {
    return card.getCost().stream().noneMatch(cost -> cost != COLORLESS);
  }

  public static boolean isOfType(Card card, Type type) {
    return card.getTypes().contains(type);
  }

  public static boolean isNotOfType(Card card, Type type) {
    return !isOfType(card, type);
  }

  public static boolean isOfColor(Card card, Color color) {
    return card.getColors().contains(color);
  }

  public static boolean ofAnyOfTheColors(Card card, List<Color> colors) {
    for (Color color : colors) {
      if (isOfColor(card, color)) {
        return true;
      }
    }
    return false;
  }
}