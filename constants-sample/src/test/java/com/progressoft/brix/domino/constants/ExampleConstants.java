package com.progressoft.brix.domino.constants;

import com.google.gwt.i18n.client.Constants;

@com.progressoft.brix.domino.constants.Constants(locales = {"en","fr"}, defaultLocale = "en")
public interface ExampleConstants extends Constants {

    ExampleConstants INSTANCE= ExampleConstants_factory.create();

    @DefaultStringValue("StockWatcher")
    String stockWatcher();

    @DefaultStringValue("Symbol")
    String symbol();

    @DefaultStringValue("Price")
    String price();

    @DefaultStringValue("Change")
    String change();

    @DefaultStringValue("Remove")
    String remove();

    @DefaultStringValue("Add")
    String add();

    @DefaultIntValue(10)
    int count();

    boolean enabled();

    String[] stringArray();

}
