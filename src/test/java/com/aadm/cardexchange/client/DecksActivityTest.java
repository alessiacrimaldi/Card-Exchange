package com.aadm.cardexchange.client;

import com.aadm.cardexchange.client.auth.AuthSubject;
import com.aadm.cardexchange.client.presenters.DecksActivity;
import com.aadm.cardexchange.client.utils.BaseAsyncCallback;
import com.aadm.cardexchange.client.views.DecksView;
import com.aadm.cardexchange.shared.DeckServiceAsync;
import com.aadm.cardexchange.shared.models.Game;
import com.aadm.cardexchange.shared.models.PhysicalCard;
import com.aadm.cardexchange.shared.models.PhysicalCardWithName;
import com.aadm.cardexchange.shared.models.Status;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.easymock.IMocksControl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.easymock.EasyMock.*;

public class DecksActivityTest {

    IMocksControl ctrl;
    DecksView mockDecksView;
    DecksActivity decksActivity;
    DeckServiceAsync mockRpcService;

    @BeforeEach
    public void initialize() {
        ctrl = createStrictControl();
        mockDecksView = ctrl.createMock(DecksView.class);
        mockRpcService = ctrl.mock(DeckServiceAsync.class);
        AuthSubject authSubject = new AuthSubject();
        decksActivity = new DecksActivity(mockDecksView, mockRpcService, authSubject);
    }

    @Test
    public void testFetchUserDeckForSuccess() {
        mockRpcService.getMyDeck(anyString(), anyString(), isA(BaseAsyncCallback.class));
        List<PhysicalCardWithName> pcards = new ArrayList<>() {{
            add(new PhysicalCardWithName(
                    new PhysicalCard(Game.MAGIC, 111, Status.Good, "This is a valid description"),
                    "Test Card"));
            add(new PhysicalCardWithName(
                    new PhysicalCard(Game.MAGIC, 111, Status.Good, "This is a valid description"),
                    "Test Card"));
        }};

        expectLastCall().andAnswer(() -> {
            Object[] args = getCurrentArguments();
            AsyncCallback<List<PhysicalCardWithName>> callback = (AsyncCallback<List<PhysicalCardWithName>>) args[args.length - 1];
            callback.onSuccess(pcards);
            return null;
        });
        ctrl.replay();
        decksActivity.fetchUserDeck("Owned", Assertions::assertNotNull);
        ctrl.verify();
    }
}