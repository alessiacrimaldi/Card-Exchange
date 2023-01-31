package com.aadm.cardexchange;

import com.aadm.cardexchange.client.*;
import com.aadm.cardexchange.server.*;
import com.aadm.cardexchange.shared.*;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectClasses({
        CardTest.class,
        UserTest.class,
        LoginInfoTest.class,
        CardDecoratorTest.class,
        YuGiOhCardDecoratorTest.class,
        PokemonCardDecoratorTest.class,
        MagicCardDecoratorTest.class,
        PhysicalCardDecoratorTest.class,
        PhysicalCardTest.class,
        DeckTest.class,
        MapDBTest.class,
        GsonSerializerTest.class,
        JSONParserTest.class,
        CardServiceTest.class,
        AuthServiceTest.class,
        DeckServiceTest.class,
        ListenerImplTest.class,
        HomeActivityTest.class,
        CardActivityTest.class,
        AuthActivityTest.class,
        DecksActivityTest.class,
        AuthSubjectTest.class,
})
public class CardExchangeSuite {
}
