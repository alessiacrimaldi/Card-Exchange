package com.aadm.cardexchange.client.presenters;

import com.aadm.cardexchange.client.auth.AuthSubject;
import com.aadm.cardexchange.client.utils.BaseAsyncCallback;
import com.aadm.cardexchange.client.views.DecksView;
import com.aadm.cardexchange.shared.DeckServiceAsync;
import com.aadm.cardexchange.shared.models.PhysicalCardWithName;
import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

import java.util.List;
import java.util.function.BiConsumer;

public class DecksActivity extends AbstractActivity implements DecksView.Presenter {
    private final DecksView view;
    private final DeckServiceAsync rpcService;
    private final AuthSubject authSubject;

    public DecksActivity(DecksView view, DeckServiceAsync rpcService, AuthSubject authSubject) {
        this.view = view;
        this.rpcService = rpcService;
        this.authSubject = authSubject;
    }

    @Override
    public void start(AcceptsOneWidget acceptsOneWidget, EventBus eventBus) {
        view.setPresenter(this);
        acceptsOneWidget.setWidget(view.asWidget());
        fetchUserDeckNames();
    }

    @Override
    public void onStop() {
        view.resetData();
    }

    @Override
    public void fetchUserDeck(String deckName, BiConsumer<List<PhysicalCardWithName>, String> setDeckData) {
        rpcService.getMyDeck(authSubject.getToken(), deckName, new BaseAsyncCallback<List<PhysicalCardWithName>>() {
            @Override
            public void onSuccess(List<PhysicalCardWithName> result) {
                setDeckData.accept(result, null);
            }
        });
    }

    private void fetchUserDeckNames() {
        rpcService.getUserDeckNames(authSubject.getToken(), new BaseAsyncCallback<List<String>>() {
            @Override
            public void onSuccess(List<String> result) {
                view.setData(result);
            }
        });
    }
}