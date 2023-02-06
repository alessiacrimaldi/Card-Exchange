package com.aadm.cardexchange.server;

import com.aadm.cardexchange.server.mapdb.MapDB;
import com.aadm.cardexchange.server.services.DeckServiceImpl;
import com.aadm.cardexchange.shared.exceptions.AuthException;
import com.aadm.cardexchange.shared.exceptions.BaseException;
import com.aadm.cardexchange.shared.exceptions.InputException;
import com.aadm.cardexchange.shared.models.*;
import org.easymock.IMocksControl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mapdb.Serializer;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import java.util.*;

import static org.easymock.EasyMock.*;

public class DeckServiceTest {
    private IMocksControl ctrl;
    private MapDB mockDB;
    ServletConfig mockConfig;
    ServletContext mockCtx;
    private DeckServiceImpl deckService;

    @BeforeEach
    public void initialize() throws ServletException {
        ctrl = createStrictControl();
        mockDB = ctrl.createMock(MapDB.class);
        deckService = new DeckServiceImpl(mockDB);
        mockConfig = ctrl.createMock(ServletConfig.class);
        mockCtx = ctrl.createMock(ServletContext.class);
        deckService.init(mockConfig);
    }

    @Test
    public void testIfDeckAlreadyExist() {
        Map<String, Map<String, Deck>> deckMap = new HashMap<>();
        Map<String, Deck> mockDecks = new HashMap<>() {{
            put("Owned", new Deck("Owned"));
            put("Wished", new Deck("Wished"));
        }};
        deckMap.put("test@test.it", mockDecks);
        ctrl.replay();
        Assertions.assertFalse(DeckServiceImpl.createDefaultDecks("test@test.it", deckMap));
        ctrl.verify();
    }

    @Test
    public void testDefaultDeckCreation() {
        Map<String, Map<String, Deck>> deckMap = new HashMap<>();
        ctrl.replay();
        Assertions.assertTrue(DeckServiceImpl.createDefaultDecks("testbis@test.it", deckMap));
        ctrl.verify();
        // check if default decks "Owned", "Wished" are present
        Assertions.assertAll(() -> {
            Assertions.assertNotNull(deckMap.get("testbis@test.it").get("Owned"));
            Assertions.assertNotNull(deckMap.get("testbis@test.it").get("Wished"));
        });
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"invalidToken"})
    public void testAddPhysicalCardToDeckForInvalidToken(String input) {
        Map<String, LoginInfo> mockLoginMap = new HashMap<>() {{
            put("validToken1", new LoginInfo("test@test1.it", System.currentTimeMillis() - 10000));
            put("validToken2", new LoginInfo("test@test2.it", System.currentTimeMillis() - 20000));
            put("validToken3", new LoginInfo("test@test3.it", System.currentTimeMillis() - 30000));
        }};
        expect(mockConfig.getServletContext()).andReturn(mockCtx);
        expect(mockDB.getPersistentMap(isA(ServletContext.class), anyString(), isA(Serializer.class), isA(Serializer.class)))
                .andReturn(mockLoginMap);
        ctrl.replay();
        Assertions.assertThrows(AuthException.class, () ->
                deckService.addPhysicalCardToDeck(input, Game.MAGIC, "Owned", 111, Status.Damaged, "This is a valid description.")
        );
        ctrl.verify();
    }

    private void setupForValidToken() {
        Map<String, LoginInfo> mockLoginMap = new HashMap<>() {{
            put("validToken", new LoginInfo("test@test.it", System.currentTimeMillis() - 10000));
        }};
        expect(mockConfig.getServletContext()).andReturn(mockCtx);
        expect(mockDB.getPersistentMap(isA(ServletContext.class), anyString(), isA(Serializer.class), isA(Serializer.class)))
                .andReturn(mockLoginMap);
    }

    @Test
    public void testAddPhysicalCardToDeckForInvalidGame() {
        setupForValidToken();
        ctrl.replay();
        Assertions.assertThrows(InputException.class, () -> {
            deckService.addPhysicalCardToDeck("validToken", null, "Owned", 111, Status.Excellent, "This is a valid description.");
        });
        ctrl.verify();
    }

    @ParameterizedTest
    @NullAndEmptySource
    public void testAddPhysicalCardToDeckForInvalidDeckName(String input) {
        setupForValidToken();
        ctrl.replay();
        Assertions.assertThrows(InputException.class, () ->
                deckService.addPhysicalCardToDeck("validToken", Game.MAGIC, input, 111, Status.Excellent, "This is a valid description.")
        );
        ctrl.verify();
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 0})
    public void testAddPhysicalCardToDeckForInvalidCardId(int input) {
        setupForValidToken();
        ctrl.replay();
        Assertions.assertThrows(InputException.class, () ->
                deckService.addPhysicalCardToDeck("validToken", Game.MAGIC, "Owned", input, Status.Excellent, "This is a valid description.")
        );
        ctrl.verify();
    }

    @Test
    public void testAddPhysicalCardToDeckForInvalidStatus() {
        setupForValidToken();
        ctrl.replay();
        Assertions.assertThrows(InputException.class, () ->
                deckService.addPhysicalCardToDeck("validToken", Game.MAGIC, "Owned", 111, null, "This is a valid description.")
        );
        ctrl.verify();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"not valid descr", "1234567890123456789", "1 3 5 7 9 1 3 5 7 9 "})
    public void testAddPhysicalCardToDeckForInvalidDescription(String input) {
        setupForValidToken();
        ctrl.replay();
        Assertions.assertThrows(InputException.class, () ->
                deckService.addPhysicalCardToDeck("validToken", Game.MAGIC, "Owned", 111, Status.Excellent, input)
        );
        ctrl.verify();
    }

    @Test
    public void testAddPhysicalCardToDeckForNotExistingDeckMap() {
        setupForValidToken();
        Map<String, Map<String, Deck>> mockDeckMap = new HashMap<>() {{
            put("test@test.it", null);
        }};
        expect(mockConfig.getServletContext()).andReturn(mockCtx);
        expect(mockDB.getPersistentMap(isA(ServletContext.class), anyString(), isA(Serializer.class), isA(Serializer.class)))
                .andReturn(mockDeckMap);
        ctrl.replay();
        Assertions.assertThrows(RuntimeException.class, () ->
                deckService.addPhysicalCardToDeck("validToken", Game.MAGIC, "Owned", 111, Status.Excellent, "This is a valid description.")
        );
        ctrl.verify();
    }

    @Test
    public void testAddPhysicalCardToDeckForNotExistingDeck() throws BaseException {
        setupForValidToken();
        Map<String, Deck> deckMap = new HashMap<>();
        Map<String, Map<String, Deck>> mockDeckMap = new HashMap<>() {{
            put("test@test.it", deckMap);
        }};
        expect(mockConfig.getServletContext()).andReturn(mockCtx);
        expect(mockDB.getPersistentMap(isA(ServletContext.class), anyString(), isA(Serializer.class), isA(Serializer.class)))
                .andReturn(mockDeckMap);
        ctrl.replay();
        Assertions.assertFalse(deckService.addPhysicalCardToDeck("validToken", Game.MAGIC, "Owned", 111, Status.Excellent, "This is a valid description."));
        ctrl.verify();
    }

    @Test
    public void testAddPhysicalCardToDeckForCardAddition() throws BaseException {
        setupForValidToken();
        Map<String, Deck> deckMap = new HashMap<>() {{
            put("Owned", new Deck("Owned", true));
        }};
        Map<String, Map<String, Deck>> mockDeckMap = new HashMap<>() {{
            put("test@test.it", deckMap);
        }};
        expect(mockConfig.getServletContext()).andReturn(mockCtx);
        expect(mockDB.getPersistentMap(isA(ServletContext.class), anyString(), isA(Serializer.class), isA(Serializer.class)))
                .andReturn(mockDeckMap);
        ctrl.replay();
        Assertions.assertTrue(deckService.addPhysicalCardToDeck("validToken", Game.MAGIC, "Owned", 111, Status.Excellent, "This is a valid description."));
        ctrl.verify();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"invalidToken"})
    public void testAddDeckForInvalidToken(String input) {
        Map<String, LoginInfo> loginInfoMap = new HashMap<>() {{
            put("validToken1", new LoginInfo("test@test.it", System.currentTimeMillis() - 10000));
            put("validToken2", new LoginInfo("test2@test.it", System.currentTimeMillis() - 20000));
            put("validToken3", new LoginInfo("test3@test.it", System.currentTimeMillis() - 30000));
        }};
        expect(mockConfig.getServletContext()).andReturn(mockCtx);
        expect(mockDB.getPersistentMap(isA(ServletContext.class), anyString(), isA(Serializer.class), isA(Serializer.class)))
                .andReturn(loginInfoMap);
        ctrl.replay();
        Assertions.assertThrows(AuthException.class, () -> deckService.addDeck(input, "testDeckName"));
        ctrl.verify();
    }

    @Test
    public void testAddDeckForAlreadyExistingDeck() throws AuthException {
        setupForValidToken();
        String userEmail = "test@test.it";
        String deckName = "testDeckName";
        Map<String, Deck> decks = new HashMap<>();
        decks.put(deckName, new Deck(deckName, false));
        Map<String, Map<String, Deck>> mockDeckMap = new HashMap<>() {{
            put(userEmail, decks);
        }};
        expect(mockConfig.getServletContext()).andReturn(mockCtx);
        expect(mockDB.getPersistentMap(isA(ServletContext.class), anyString(), isA(Serializer.class), isA(Serializer.class)))
                .andReturn(mockDeckMap);
        ctrl.replay();
        Assertions.assertFalse(deckService.addDeck("validToken", deckName));
        ctrl.verify();
    }

    @Test
    public void testAddDeckForNotAlreadyExistingDeck() throws AuthException {
        setupForValidToken();
        String customDeckName = "testDeckName";
        Map<String, Deck> decks = new HashMap<>() {{
            put("Owned", new Deck("Owned", true));
            put("Wished", new Deck("Wished", true));
        }};
        Map<String, Map<String, Deck>> mockDeckMap = new HashMap<>() {{
            put("test@test.it", decks);
        }};
        expect(mockConfig.getServletContext()).andReturn(mockCtx);
        expect(mockDB.getPersistentMap(isA(ServletContext.class), anyString(), isA(Serializer.class), isA(Serializer.class)))
                .andReturn(mockDeckMap);
        ctrl.replay();
        Assertions.assertTrue(deckService.addDeck("validToken", customDeckName));
        ctrl.verify();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"invalidToken"})
    public void testGetUserDeckNamesForInvalidToken(String input) {
        Map<String, LoginInfo> loginInfoMap = new HashMap<>() {{
            put("validToken1", new LoginInfo("test@test.it", System.currentTimeMillis() - 10000));
            put("validToken2", new LoginInfo("test2@test.it", System.currentTimeMillis() - 20000));
            put("validToken3", new LoginInfo("test3@test.it", System.currentTimeMillis() - 30000));
        }};
        expect(mockConfig.getServletContext()).andReturn(mockCtx);
        expect(mockDB.getPersistentMap(isA(ServletContext.class), anyString(), isA(Serializer.class), isA(Serializer.class)))
                .andReturn(loginInfoMap);
        ctrl.replay();
        Assertions.assertThrows(AuthException.class, () -> deckService.getUserDeckNames(input));
        ctrl.verify();
    }

    @Test
    public void testGetUserDeckNamesForNotExistingDeckMap() throws AuthException {
        setupForValidToken();
        Map<String, Map<String, Deck>> mockDeckMap = new HashMap<>() {{
            put("test@test.it", null);
        }};
        expect(mockConfig.getServletContext()).andReturn(mockCtx);
        expect(mockDB.getPersistentMap(isA(ServletContext.class), anyString(), isA(Serializer.class), isA(Serializer.class)))
                .andReturn(mockDeckMap);
        ctrl.replay();
        Assertions.assertTrue(deckService.getUserDeckNames("validToken").isEmpty());
        ctrl.verify();
    }

    @Test
    public void testGetUserDeckNamesForReturnCorrectDeckNames() throws AuthException {
        List<String> mockDeckNames = Arrays.asList("Owned", "Wished", "Test");
        setupForValidToken();
        Map<String, Deck> decks = new HashMap<>() {{
            put("Owned", new Deck("Owned", true));
            put("Wished", new Deck("Wished", true));
            put("Test", new Deck("Test", false));
        }};
        Map<String, Map<String, Deck>> mockDeckMap = new HashMap<>() {{
            put("test@test.it", decks);
        }};
        expect(mockConfig.getServletContext()).andReturn(mockCtx);
        expect(mockDB.getPersistentMap(isA(ServletContext.class), anyString(), isA(Serializer.class), isA(Serializer.class)))
                .andReturn(mockDeckMap);
        ctrl.replay();
        List<String> deckNames = deckService.getUserDeckNames("validToken");
        ctrl.verify();
        Assertions.assertAll(() -> {
            Assertions.assertEquals(mockDeckNames.size(), deckNames.size());
            Assertions.assertIterableEquals(mockDeckNames, deckNames);
        });
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"invalidToken"})
    public void testGetDeckByNameForInvalidToken(String input) {
        Map<String, LoginInfo> loginInfoMap = new HashMap<>() {{
            put("validToken1", new LoginInfo("test@test.it", System.currentTimeMillis() - 10000));
            put("validToken2", new LoginInfo("test2@test.it", System.currentTimeMillis() - 20000));
            put("validToken3", new LoginInfo("test3@test.it", System.currentTimeMillis() - 30000));
        }};
        expect(mockConfig.getServletContext()).andReturn(mockCtx);
        expect(mockDB.getPersistentMap(isA(ServletContext.class), anyString(), isA(Serializer.class), isA(Serializer.class)))
                .andReturn(loginInfoMap);

        ctrl.replay();
        Assertions.assertThrows(AuthException.class, () -> deckService.getMyDeck(input, "Owned"));
        ctrl.verify();
    }

    @Test
    public void testGetDeckByNameForNotExistingDeck() {
        setupForValidToken();
        Map<String, Deck> deckMap = new HashMap<>();
        Map<String, Map<String, Deck>> mockDeckMap = new HashMap<>() {{
            put("test@test.it", deckMap);
        }};
        expect(mockConfig.getServletContext()).andReturn(mockCtx);
        expect(mockDB.getPersistentMap(isA(ServletContext.class), anyString(), isA(Serializer.class), isA(Serializer.class)))
                .andReturn(mockDeckMap);
        ctrl.replay();
        Assertions.assertThrows(NullPointerException.class, () -> deckService.getMyDeck("validToken", "Owned"));
        ctrl.verify();
    }

    private void setupForValidCardsAndUserDecks() {
        // init Mocks
        PhysicalCard mockPCard1 = new PhysicalCard(Game.MAGIC, 1111, Status.Excellent, "This is a valid description.");
        PhysicalCard mockPCard2 = new PhysicalCard(Game.MAGIC, 2222, Status.Fair, "This is a valid bis description.");
        Card mockCard1 = MockCardData.createPokemonDummyCard();
        Card mockCard2 = MockCardData.createYuGiOhDummyCard();
        Map<Integer, Card> cardMap = new HashMap<>() {{
            put(1111, mockCard1);
            put(2222, mockCard2);
        }};
        Map<String, Deck> userDecks = new HashMap<>() {{
            put("Owned", new Deck("Owned", true));
        }};
        Map<String, Map<String, Deck>> deckMap = new HashMap<>() {{
            put("test@test.it", userDecks);
        }};
        userDecks.get("Owned").addPhysicalCard(mockPCard1);
        userDecks.get("Owned").addPhysicalCard(mockPCard2);

        // what I expect
        expect(mockConfig.getServletContext()).andReturn(mockCtx);
        expect(mockDB.getPersistentMap(isA(ServletContext.class), anyString(), isA(Serializer.class), isA(Serializer.class)))
                .andReturn(deckMap);
        expect(mockConfig.getServletContext()).andReturn(mockCtx);
        expect(mockDB.getCachedMap(isA(ServletContext.class), anyString(), isA(Serializer.class), isA(Serializer.class)))
                .andReturn(cardMap);
        expect(mockConfig.getServletContext()).andReturn(mockCtx);
        expect(mockDB.getCachedMap(isA(ServletContext.class), anyString(), isA(Serializer.class), isA(Serializer.class)))
                .andReturn(cardMap);
    }

    @Test
    public void testGetDeckByNameForValidEntry() throws AuthException {
        setupForValidToken();
        setupForValidCardsAndUserDecks();
        ctrl.replay();
        List<PhysicalCardWithName> decoratedCards = deckService.getMyDeck("validToken", "Owned");
        ctrl.verify();
        Assertions.assertAll(() -> {
            Assertions.assertEquals(2, decoratedCards.size());
            Assertions.assertEquals("Charizard", decoratedCards.get(0).getName());
            Assertions.assertEquals("Exodia the Forbidden One", decoratedCards.get(1).getName());
        });
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"test", "test@", "test@test", "test@test."})
    public void testGetUserOwnedDeckForInvalidEmail(String input) {
        ctrl.replay();
        Assertions.assertThrows(AuthException.class, () -> deckService.getUserOwnedDeck(input));
        ctrl.verify();
    }

    @Test
    public void testGetUserOwnedDeckForValidEmail() throws AuthException {
        setupForValidCardsAndUserDecks();
        ctrl.replay();
        List<PhysicalCardWithName> decoratedCards = deckService.getUserOwnedDeck("test@test.it");
        ctrl.verify();
        Assertions.assertAll(() -> {
            Assertions.assertEquals(2, decoratedCards.size());
            Assertions.assertEquals("Charizard", decoratedCards.get(0).getName());
            Assertions.assertEquals("Exodia the Forbidden One", decoratedCards.get(1).getName());
        });
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 0})
    public void testGetOwnedPhysicalCardsByCardIdForInvalidId(int input) {
        ctrl.replay();
        Assertions.assertThrows(InputException.class, () -> deckService.getOwnedPhysicalCardsByCardId(input));
        ctrl.verify();
    }

    @Test
    public void testGetOwnedPhysicalCardsByCardIdForValidId() throws InputException {
        // init Mocks
        PhysicalCard mockPCard1 = new PhysicalCard(Game.MAGIC, 1111, Status.Excellent, "This is a valid description.");
        PhysicalCard mockPCard2 = new PhysicalCard(Game.MAGIC, 1111, Status.Good, "This is a valid description.");
        PhysicalCard mockPCard3 = new PhysicalCard(Game.POKEMON, 2222, Status.Fair, "This is a valid description.");
        PhysicalCard mockPCard4 = new PhysicalCard(Game.YUGIOH, 3333, Status.Damaged, "This is a valid description.");

        Deck test1OwnedDeck = new Deck("Owned");
        Deck test2OwnedDeck = new Deck("Owned");
        test1OwnedDeck.addPhysicalCard(mockPCard1);
        test1OwnedDeck.addPhysicalCard(mockPCard3);
        test2OwnedDeck.addPhysicalCard(mockPCard2);
        test2OwnedDeck.addPhysicalCard(mockPCard4);
        Map<String, Deck> test1Decks = new HashMap<>() {{
            put("Owned", test1OwnedDeck);
        }};
        Map<String, Deck> test2Decks = new HashMap<>() {{
            put("Owned", test2OwnedDeck);
        }};
        Map<String, Map<String, Deck>> deckMap = new LinkedHashMap<>() {{
            put("test1@test.it", test1Decks);
            put("test2@test.it", test2Decks);
        }};

        // what I expect
        expect(mockConfig.getServletContext()).andReturn(mockCtx);
        expect(mockDB.getPersistentMap(isA(ServletContext.class), anyString(), isA(Serializer.class), isA(Serializer.class)))
                .andReturn(deckMap);

        ctrl.replay();
        List<PhysicalCardWithEmail> decoratedCards = deckService.getOwnedPhysicalCardsByCardId(1111);
        ctrl.verify();
        Assertions.assertAll(() -> {
            Assertions.assertEquals(2, decoratedCards.size());
            Assertions.assertEquals("test1@test.it", decoratedCards.get(0).getEmail());
            Assertions.assertEquals("test2@test.it", decoratedCards.get(1).getEmail());
        });
    }

    @ParameterizedTest
    @NullAndEmptySource
    public void testRemovePhysicalCardFromDeckForInvalidDeckName(String input) {
        setupForValidToken();
        PhysicalCard mockPCard1 = new PhysicalCard(Game.MAGIC, 1111, Status.Excellent, "This is a valid description.");
        ctrl.replay();
        Assertions.assertThrows(InputException.class, () ->
                deckService.removePhysicalCardFromDeck("validToken", input, mockPCard1)
        );
        ctrl.verify();
    }

    @Test
    public void testRemovePhysicalCardFromDeckForNotExistingDeckMap() {
        setupForValidToken();
        PhysicalCard mockPCard1 = new PhysicalCard(Game.MAGIC, 1111, Status.Excellent, "This is a valid description.");

        Map<String, Map<String, Deck>> mockDeckMap = new HashMap<>() {{
            put("test@test.it", null);
        }};
        expect(mockConfig.getServletContext()).andReturn(mockCtx);
        expect(mockDB.getPersistentMap(isA(ServletContext.class), anyString(), isA(Serializer.class), isA(Serializer.class)))
                .andReturn(mockDeckMap);
        ctrl.replay();
        Assertions.assertThrows(RuntimeException.class, () ->
                deckService.removePhysicalCardFromDeck("validToken", "Owned", mockPCard1)
        );
        ctrl.verify();
    }

    @Test
    public void testRemovePhysicalCardFromDeckForNotExistingDeck() throws AuthException, InputException {
        setupForValidToken();
        PhysicalCard mockPCard1 = new PhysicalCard(Game.MAGIC, 1111, Status.Excellent, "This is a valid description.");
        Map<String, Deck> deckMap = new HashMap<>() {{
            put("Wished", new Deck("Wished", true));
        }};
        Map<String, Map<String, Deck>> mockDeckMap = new HashMap<>() {{
            put("test@test.it", deckMap);
        }};
        expect(mockConfig.getServletContext()).andReturn(mockCtx);
        expect(mockDB.getPersistentMap(isA(ServletContext.class), anyString(), isA(Serializer.class), isA(Serializer.class)))
                .andReturn(mockDeckMap);
        ctrl.replay();
        Assertions.assertFalse(deckService.removePhysicalCardFromDeck("validToken", "Owned", mockPCard1));
        ctrl.verify();
    }

    @Test
    public void testRemovePhysicalCardFromDeckForNotExistingPhysicalCard() throws AuthException, InputException {
        setupForValidToken();
        PhysicalCard mockPCard1 = new PhysicalCard(Game.MAGIC, 1111, Status.Excellent, "This is a valid description.");
        PhysicalCard mockPCard2 = new PhysicalCard(Game.MAGIC, 1221, Status.Fair, "This is a valid bis description.");
        Map<String, Deck> deckMap = new HashMap<>() {{
            put("Owned", new Deck("Owned", true));
        }};
        Map<String, Map<String, Deck>> mockDeckMap = new HashMap<>() {{
            put("test@test.it", deckMap);
        }};
        deckMap.get("Owned").addPhysicalCard(mockPCard2);

        expect(mockConfig.getServletContext()).andReturn(mockCtx);
        expect(mockDB.getPersistentMap(isA(ServletContext.class), anyString(), isA(Serializer.class), isA(Serializer.class)))
                .andReturn(mockDeckMap);
        ctrl.replay();
        //.remove method of Set.java return false is object to remove is not present
        Assertions.assertFalse(deckService.removePhysicalCardFromDeck("validToken", "Owned", mockPCard1));
        ctrl.verify();
    }

    @Test
    public void testRemovePhysicalCardFromDeckSuccess() throws AuthException, InputException {
        setupForValidToken();
        PhysicalCard mockPCard1 = new PhysicalCard(Game.MAGIC, 1111, Status.Excellent, "This is a valid description.");
        Map<String, Deck> deckMap = new HashMap<>() {{
            put("Owned", new Deck("Owned", true));
        }};
        Map<String, Map<String, Deck>> mockDeckMap = new HashMap<>() {{
            put("test@test.it", deckMap);
        }};
        deckMap.get("Owned").addPhysicalCard(mockPCard1);

        expect(mockConfig.getServletContext()).andReturn(mockCtx);
        expect(mockDB.getPersistentMap(isA(ServletContext.class), anyString(), isA(Serializer.class), isA(Serializer.class)))
                .andReturn(mockDeckMap);
        ctrl.replay();
        Assertions.assertTrue(deckService.removePhysicalCardFromDeck("validToken", "Owned", mockPCard1));
        ctrl.verify();
    }

}