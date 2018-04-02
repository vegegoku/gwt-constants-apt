package com.progressoft.brix.domino.constants;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.Constants;

//@com.progressoft.brix.domino.constants.Constants(locales = {"en","fr"})
public interface SampleConstants extends Constants {

    SampleConstants INSTANCE = GWT.create(SampleConstants.class);

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

    String[] stringArray();

    String inenonly();

    @DefaultStringValue("from annotation")
    String inenandannotation();

    @DefaultStringValue("from annotation only")
    String inannotationonly();

    String indefaultonly();

    @DefaultStringValue("from annotation")
    String indefaultandannotation();

}
