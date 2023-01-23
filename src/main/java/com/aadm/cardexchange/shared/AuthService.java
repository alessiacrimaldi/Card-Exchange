package com.aadm.cardexchange.shared;

import com.aadm.cardexchange.shared.models.AuthException;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("users")
public interface AuthService extends RemoteService {

    String signup(String email, String password) throws AuthException;

    String signin(String email, String password) throws AuthException;

    Boolean logout(String token) throws AuthException;
}