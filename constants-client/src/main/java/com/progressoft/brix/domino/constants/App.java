package com.progressoft.brix.domino.constants;

import com.google.gwt.core.client.EntryPoint;

import java.util.logging.Logger;

public class App implements EntryPoint {

	private static final Logger LOGGER=Logger.getLogger(App.class.getName());

	public void onModuleLoad() {
		LOGGER.info(SampleConstants.INSTANCE.add());
		LOGGER.info(SampleConstants.INSTANCE.price());
		LOGGER.info(SampleConstants.INSTANCE.inenonly()+"");
		LOGGER.info(SampleConstants.INSTANCE.inannotationonly()+"");
		LOGGER.info(SampleConstants.INSTANCE.inenandannotation()+"");
		LOGGER.info(SampleConstants.INSTANCE.indefaultonly()+"");
		LOGGER.info(SampleConstants.INSTANCE.indefaultandannotation()+"");

		LOGGER.info(TestConstants.INSTANCE.getString());
		LOGGER.info(TestConstants.INSTANCE.stringDoesNotTrimTrailingThreeSpaces());
		LOGGER.info(TestConstants.INSTANCE.stringEmpty()+"");
		LOGGER.info(TestConstants.INSTANCE.stringJapaneseBlue()+"");
		LOGGER.info(TestConstants.INSTANCE.stringJapaneseRed()+"");
		LOGGER.info(TestConstants.INSTANCE.booleanFalse()+"");
		LOGGER.info(TestConstants.INSTANCE.doubleNegOne()+"");

	}
}
