package com.aadm.cardexchange.server.services;

import com.aadm.cardexchange.server.gsonserializer.GsonSerializer;
import com.aadm.cardexchange.server.mapdb.MapDB;
import com.aadm.cardexchange.server.mapdb.MapDBConstants;
import com.aadm.cardexchange.server.mapdb.MapDBImpl;
import com.aadm.cardexchange.shared.DeckService;
import com.aadm.cardexchange.shared.models.*;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import org.mapdb.Serializer;

import java.lang.reflect.Type;
import java.util.*;


public class DeckServiceImpl extends RemoteServiceServlet implements DeckService, MapDBConstants {
    private static final long serialVersionUID = 5868007467963819042L;
    private final MapDB db;
    private final Gson gson = new Gson();
    private final Type type = new TypeToken<Map<String, Deck>>() {
    }.getType();

    public DeckServiceImpl() {
        db = new MapDBImpl();
    }

    public DeckServiceImpl(MapDB mockDB) {
        db = mockDB;
    }

    private static boolean addDeck(String email, String deckName, boolean isDefault, Map<String, Map<String, Deck>> deckMap) {
        Map<String, Deck> userDecks = deckMap.computeIfAbsent(email, k -> new HashMap<>());
        // if deck already exists in decks container do nothing
        if (userDecks.get(deckName) != null) {
            return false;
        }
        userDecks.put(deckName, new Deck(deckName, isDefault));
        deckMap.put(email, userDecks);
        return true;
    }

    @Override
    public boolean addDeck(String token, String deckName) throws AuthException {
        String email = AuthServiceImpl.checkTokenValidity(token,
                db.getPersistentMap(getServletContext(), LOGIN_MAP_NAME, Serializer.STRING, new GsonSerializer<>(gson)));
        Map<String, Map<String, Deck>> deckMap = db.getPersistentMap(getServletContext(), DECK_MAP_NAME, Serializer.STRING, new GsonSerializer<>(gson, type));
        return addDeck(email, deckName, false, deckMap);
    }

    public static boolean createDefaultDecks(String email, Map<String, Map<String, Deck>> deckMap) {
        return addDeck(email, "Owned", true, deckMap) && addDeck(email, "Wished", true, deckMap);
    }

    private boolean checkDescriptionValidity(String description) {
        int count = 0;
        for (int i = 0; i < description.length(); i++) {
            if (description.charAt(i) != ' ') {
                count++;
            }
            if (count >= 20) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean addPhysicalCardToDeck(String token, Game game, String deckName, int cardId, Status status, String description) throws AuthException {
        /* PARAMETERS CHECK */
        String userEmail = AuthServiceImpl.checkTokenValidity(token, db.getPersistentMap(getServletContext(), LOGIN_MAP_NAME, Serializer.STRING, new GsonSerializer<>(gson)));
        if (game == null) {
            throw new IllegalArgumentException("Invalid game");
        }
        if (deckName == null || deckName.isEmpty()) {
            throw new IllegalArgumentException("Invalid deck name");
        }
        if (cardId <= 0) {
            throw new IllegalArgumentException("Invalid card id");
        }
        if (status == null) {
            throw new IllegalArgumentException("Invalid status");
        }
        if (description == null || !checkDescriptionValidity(description)) {
            throw new IllegalArgumentException("Invalid description");
        }
        /* PHYSICAL CARD ADDITION TO DECK*/
        Map<String, Map<String, Deck>> deckMap = db.getPersistentMap(getServletContext(), DECK_MAP_NAME, Serializer.STRING, new GsonSerializer<>(gson, type));
        Map<String, Deck> decks = deckMap.get(userEmail);
        if (decks == null) {
            throw new RuntimeException("Not existing decks");
        }
        // selection of deck with deckName
        Deck foundDeck = decks.get(deckName);
        if (foundDeck == null) {
            return false;
        }
        // physical card addition
        return foundDeck.addPhysicalCard(new PhysicalCardImpl(game, cardId, status, description));
    }

    @Override
    public List<String> getUserDecks(String token) throws AuthException {
        String userEmail = AuthServiceImpl.checkTokenValidity(token, db.getPersistentMap(getServletContext(), LOGIN_MAP_NAME, Serializer.STRING, new GsonSerializer<>(gson)));
        Map<String, Map<String, Deck>> deckMap = db.getPersistentMap(getServletContext(), DECK_MAP_NAME, Serializer.STRING, new GsonSerializer<>(gson, type));
        Map<String, Deck> decks = deckMap.get(userEmail);
        return decks == null ? Collections.emptyList() : new ArrayList<>(decks.keySet());
    }
}
