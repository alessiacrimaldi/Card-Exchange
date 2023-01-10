package com.aadm.cardexchange.shared.models;

public abstract class CardDecorator implements Card {
    private static final long serialVersionUID = -6914752354287411438L;
    private Card wrappee;

    protected CardDecorator(Card card) {
        wrappee = card;
    }

    protected CardDecorator() {
    }

    @Override
    public String getName() {
        return wrappee.getName();
    }

    @Override
    public String getDescription() {
        return wrappee.getDescription();
    }

    @Override
    public String getType() {
        return wrappee.getType();
    }
}
