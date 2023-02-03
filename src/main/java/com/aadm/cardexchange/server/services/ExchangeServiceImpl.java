package com.aadm.cardexchange.server.services;

import com.aadm.cardexchange.server.gsonserializer.GsonSerializer;
import com.aadm.cardexchange.server.mapdb.MapDB;
import com.aadm.cardexchange.server.mapdb.MapDBConstants;
import com.aadm.cardexchange.server.mapdb.MapDBImpl;
import com.aadm.cardexchange.shared.DeckService;
import com.aadm.cardexchange.shared.ExchangeService;
import com.aadm.cardexchange.shared.exceptions.AuthException;
import com.aadm.cardexchange.shared.exceptions.InputException;
import com.aadm.cardexchange.shared.models.*;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import org.mapdb.Serializer;

import java.lang.reflect.Type;
import java.util.*;

import static com.aadm.cardexchange.server.services.AuthServiceImpl.validateEmail;


public class ExchangeServiceImpl extends RemoteServiceServlet implements ExchangeService, MapDBConstants {
    private static final long serialVersionUID = 5868088467963819042L;
    private final MapDB db;
    private final Gson gson = new Gson();
    public ExchangeServiceImpl() {
        db = new MapDBImpl();
    }
    public ExchangeServiceImpl(MapDB mockDB) {
        db = mockDB;
    }
    private boolean checkEmailExistance(String email) {
        Map<String, User> userMap = db.getPersistentMap(
                getServletContext(), USER_MAP_NAME, Serializer.STRING, new GsonSerializer<>(gson));
        System.out.println(userMap.get(email) );
        if (userMap.get(email) != null) {
            return true;
        } else return false;
    }
    private boolean checkPcardListConsistency(List<PhysicalCardImpl> physicalCards) {
       if (physicalCards == null) return false;
       else return !physicalCards.isEmpty();
    }
    @Override
    public boolean addProposal(String token, String receiverUserEmail, List<PhysicalCardImpl> senderPhysicalCards, List<PhysicalCardImpl> receiverPhysicalCards) throws AuthException, InputException {
        String email = AuthServiceImpl.checkTokenValidity(token,
                db.getPersistentMap(getServletContext(), LOGIN_MAP_NAME, Serializer.STRING, new GsonSerializer<>(gson)));
        if (receiverUserEmail == null || receiverUserEmail.isEmpty() || !checkEmailExistance(receiverUserEmail)) {
            throw new InputException("Invalid receiverUserEmail name");
        } else if (!checkPcardListConsistency(senderPhysicalCards)) {
            throw new InputException("Invalid senderPhysicalCards list");
        } else if (!checkPcardListConsistency(receiverPhysicalCards)) {
            throw new InputException("Invalid senderPhysicalCards list");
        } else {
            Proposal newProposal = new Proposal(email, receiverUserEmail, senderPhysicalCards, receiverPhysicalCards);
            Map<Integer, Proposal> proposalMap = db.getPersistentMap(getServletContext(), PROPOSAL_MAP_NAME, Serializer.INTEGER, new GsonSerializer<>(gson));
            if (proposalMap.putIfAbsent(newProposal.getId(), newProposal) == null) return true;
            else return false;
        }
    }
}

