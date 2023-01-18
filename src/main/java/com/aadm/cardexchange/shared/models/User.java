package com.aadm.cardexchange.shared.models;

import org.mindrot.jbcrypt.BCrypt;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;

public class User implements Serializable {
    private static final long serialVersionUID = 2004822123627539475L;
    private static final AtomicInteger uniqueId = new AtomicInteger();

    private int id;
    private String username;
    private String password;

    public User() {
    }

    public User(String username, String password) {
        this.id = uniqueId.getAndIncrement();
        this.username = username;
        this.password = BCrypt.hashpw(password, BCrypt.gensalt());
    }

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
