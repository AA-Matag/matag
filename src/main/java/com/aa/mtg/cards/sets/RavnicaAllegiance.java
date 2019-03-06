package com.aa.mtg.cards.sets;

import com.aa.mtg.cards.Card;
import com.aa.mtg.cards.properties.Color;
import com.aa.mtg.cards.properties.Cost;
import com.aa.mtg.cards.properties.Type;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

public class RavnicaAllegiance {

    public static Card FERAL_MAAKA = new Card("Feral Maaka", singletonList(Color.RED), asList(Cost.RED, Cost.COLORLESS), singletonList(Type.CREATURE), singletonList("Cat"), "", 2, 2);
    public static Card AXEBANE_BEAST = new Card("Axebane Beast", singletonList(Color.GREEN), asList(Cost.GREEN, Cost.COLORLESS, Cost.COLORLESS, Cost.COLORLESS), singletonList(Type.CREATURE), singletonList("Beast"), "", 3, 4);

}