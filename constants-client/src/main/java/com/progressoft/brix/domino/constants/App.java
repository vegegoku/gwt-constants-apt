package com.progressoft.brix.domino.constants;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.datepicker.client.DateBox;

import java.util.Date;
import java.util.logging.Logger;

public class App implements EntryPoint {

	private static final Logger LOGGER=Logger.getLogger(App.class.getName());

	public void onModuleLoad() {
		LOGGER.info(SampleConstants.INSTANCE.add());
		LOGGER.info(SampleConstants.INSTANCE.price());
		LOGGER.info(SampleConstants.INSTANCE.count()+"");

		LOGGER.info(ExampleConstants.INSTANCE.add());
		LOGGER.info(ExampleConstants.INSTANCE.change());
		LOGGER.info(ExampleConstants.INSTANCE.price());
		LOGGER.info(ExampleConstants.INSTANCE.remove());
		LOGGER.info(ExampleConstants.INSTANCE.stockWatcher());
		LOGGER.info(ExampleConstants.INSTANCE.symbol());
		LOGGER.info(ExampleConstants.INSTANCE.count()+"");
		LOGGER.info(ExampleConstants.INSTANCE.enabled()+"");
		for (String s : ExampleConstants.INSTANCE.stringArray()) {
			LOGGER.info(s);
		}



//		DateBox compareDateBox=new DateBox();
//		RootPanel.get().add(compareDateBox);
//
//		Date test = new Date(DateTimeFormat.getFormat("yyyy-MM-dd HH:mm:ss").parse("2017-01-01 00:00:00").getTime());
//		GWT.log("<<<< TEST: " + DateTimeFormat.getFormat("yyyy-MM-dd HH:mm:ss").format(test));
//		compareDateBox.setValue(test);
//		Date test2 = new Date(compareDateBox.getValue().getTime());
//		GWT.log(">>>> TEST: " + DateTimeFormat.getFormat("yyyy-MM-dd HH:mm:ss").format(test2));
	}
}
