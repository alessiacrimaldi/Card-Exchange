package com.aadm.cardexchange.server;

import com.aadm.cardexchange.shared.models.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Create mocks cards with distinct fields for testing purposes
public class MockCardData {
    public static Map<Integer, MagicCardDecorator> createMagicDummyMap() {
        return new HashMap<>() {{
            put(0, new MagicCardDecorator(new CardImpl("Lightning Bolt", "Deal 3 damage to any target", "Cast"),
                    "Christopher Rush", "Rare", true, false, false, false, false
            ));
            put(1, new MagicCardDecorator(new CardImpl("Counterspell", "Counter target spell", "Instant"),
                    "Mark Poole", "Uncommon", false, true, false, false, false
            ));
            put(2, new MagicCardDecorator(new CardImpl("Plague Wind", "Deal X damage to each creature and each player, where X is the number of creatures you control.", "Sorcery"),
                    "Ron Spencer", "Mythic Rare", false, false, true, false, false
            ));
            put(3, new MagicCardDecorator(new CardImpl("Angel of Mercy", "When Angel of Mercy enters the battlefield, you gain 3 life", "Creature"),
                    "Volkan Baa", "Mega Rare", false, false, false, true, false
            ));
            put(4, new MagicCardDecorator(new CardImpl("Heart of Light", "Enchant creature (Target a creature as you cast this. This card enters the battlefield attached to that creature.)", "Counter"),
                    "Luca Zontini", "Unique", false, false, false, false, true
            ));
        }};
    }


    public static Map<Integer, PokemonCardDecorator> createPokemonDummyMap() {
        return new HashMap<>() {{
            put(0, new PokemonCardDecorator(new CardImpl("Pikachu", "The electric mouse Pokemon", "Electric"),
                    "Atsuko Nishida", "http://www.pikachu-image.jpg", "Common", true, false, false, false, false
            ));
            put(1, new PokemonCardDecorator(new CardImpl("Charizard", "The flame Pokemon", "Flame"),
                    "Ken Sugimori", "http://www.charizard-image.jpg", "Rare", false, true, false, false, false
            ));
            put(2, new PokemonCardDecorator(new CardImpl("Blastoise", "The shellfish Pokemon", "Water"),
                    "Mitsuhiro Arita", "http://www.blastoise-image.jpg", "Uncommon", false, false, true, false, false
            ));
            put(3, new PokemonCardDecorator(new CardImpl("Mewtwo", "The Genetic Pokemon", "Psychic"),
                    "Akira Egawa", "http://www.mewtwo-image.jpg", "Ultra Rare", false, false, false, true, false
            ));
            put(4, new PokemonCardDecorator(new CardImpl("Hitmonlee", "The legs freely contract and stretch", "Fighting"),
                    "Shigenori Negishi", "https://assets.tcgdex.net/en/swsh/swsh1/94", "Unique", false, false, false, false, true
            ));
        }};
    }

    public static Map<Integer, CardDecorator> createYuGiOhDummyMap() {
        return new HashMap<>() {
            {
                put(0, new YuGiOhCardDecorator(new CardImpl("Dark Magician", "A powerful sorcerer", "Monster"),
                        "Spellcaster", "http://www.darkmagician-image.jpg", "http://www.darkmagician-thumbnail.jpg"
                ));
                put(1, new YuGiOhCardDecorator(new CardImpl("Exodia the Forbidden One", "One of the most powerful monsters in the game", "Creature"),
                        "Divine-Beast", "http://www.exodia-image.jpg", "http://www.exodia-thumbnail.jpg"
                ));
                put(2, new YuGiOhCardDecorator(new CardImpl("Monster Reborn", "Special Summon a monster from either player's graveyard", "Spell"),
                        "Monster", "http://www.monsterreborn-image.jpg", "http://www.monsterreborn-thumbnail.jpg"
                ));
                put(3, new YuGiOhCardDecorator(new CardImpl("Pot of Greed", "Draw 2 cards", "Draw"),
                        "Pot", "http://www.potofgreed-image.jpg", "http://www.potofgreed-thumbnail.jpg"
                ));
            }
        };
    }

    public static List<CardDecorator> createMagicDummyList() {
        return new ArrayList<>(createMagicDummyMap().values());
    }

    public static List<CardDecorator> createPokemonDummyList() {
        return new ArrayList<>(createPokemonDummyMap().values());
    }

    public static List<CardDecorator> createYuGiOhDummyList() {
        return new ArrayList<>(createYuGiOhDummyMap().values());
    }
}