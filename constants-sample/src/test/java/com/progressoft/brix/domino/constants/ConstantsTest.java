package com.progressoft.brix.domino.constants;

import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

public class ConstantsTest {

    @Before
    public void setUp() {
        System.setProperty("locale", "default");
    }

    @Test
    public void testConstantBooleans() {
        TestConstants types = new TestConstants_factory().create();
        assertThat(types.booleanFalse()).isFalse();
        assertThat(types.booleanTrue()).isTrue();
    }

    @Test
    public void testConstantDoubles() {
        TestConstants types = new TestConstants_factory().create();
        double delta = 0.0000001;
        assertThat(types.doublePi()).isEqualTo(3.14159, offset(delta));
        assertThat(types.doubleZero()).isEqualTo(0.0, offset(delta));
        assertThat(types.doubleOne()).isEqualTo(1.0, offset(delta));
        assertThat(types.doubleNegOne()).isEqualTo(-1.0, offset(delta));
        assertThat(types.doublePosMax()).isEqualTo(Double.MAX_VALUE, offset(delta));
        assertThat(types.doublePosMin()).isEqualTo(Double.MIN_VALUE, offset(delta));
        assertThat(types.doubleNegMax()).isEqualTo(-Double.MAX_VALUE, offset(delta));
        assertThat(types.doubleNegMin()).isEqualTo(-Double.MIN_VALUE, offset(delta));
    }

    @Test
    public void testConstantFloats() {
        TestConstants types = new TestConstants_factory().create();
        float delta = 0.0000001f;
        assertThat(types.floatPi()).isEqualTo(3.14159f, offset(delta));
        assertThat(types.floatZero()).isEqualTo(0.0f, offset(delta));
        assertThat(types.floatOne()).isEqualTo(1.0f, offset(delta));
        assertThat(types.floatNegOne()).isEqualTo(-1.0f, offset(delta));
        assertThat(types.floatPosMax()).isEqualTo(Float.MAX_VALUE, offset(delta));
        assertThat(types.floatPosMin()).isEqualTo(Float.MIN_VALUE, offset(delta));
        assertThat(types.floatNegMax()).isEqualTo(-Float.MAX_VALUE, offset(delta));
        assertThat(types.floatNegMin()).isEqualTo(-Float.MIN_VALUE, offset(delta));
    }

    @Test
    public void testIntConstant() {
        TestConstants types = new TestConstants_factory().create();
        assertThat(0).isEqualTo(types.intZero());
        assertThat(1).isEqualTo(types.intOne());
        assertThat(-1).isEqualTo(types.intNegOne());
        assertThat(Integer.MAX_VALUE).isEqualTo(types.intMax());
        assertThat(Integer.MIN_VALUE).isEqualTo(types.intMin());
    }

    /**
     * Exercises ConstantMap more than the other map tests.
     */
    @Test
    public void testConstantMapABCD() {
        TestConstants types = new TestConstants_factory().create();

        Map<String, String> map = types.mapABCD();
        Map<String, String> expectedMap = getMapFromArrayUsingASimpleRule(new String[]{
                "A", "B", "C", "D"});
        assertThat(map.get("bogus")).isNull();
        compareMapsComprehensively(map, expectedMap);

        /*
         * Test if the returned map can be modified in any way. Things are working
         * as expected if exceptions are thrown in each case.
         */
        String failureMessage = "Should have thrown UnsupportedOperationException";
        /* test map operations */
        try {
            map.remove("keyA");
            fail(failureMessage + " on map.remove");
        } catch (UnsupportedOperationException e) {
        }
        try {
            map.put("keyA", "allA");
            fail(failureMessage + "on map.put of existing key");
        } catch (UnsupportedOperationException e) {
        }
        try {
            map.put("keyZ", "allZ");
            fail(failureMessage + "on map.put of new key");
        } catch (UnsupportedOperationException e) {
        }
        try {
            map.clear();
            fail(failureMessage + " on map.clear");
        } catch (UnsupportedOperationException e) {
        }

        /* test map.keySet() operations */
        try {
            map.keySet().add("keyZ");
            fail(failureMessage + " on map.keySet().add");
        } catch (UnsupportedOperationException e) {
        }
        try {
            map.keySet().remove("keyA");
            fail(failureMessage + " on map.keySet().remove");
        } catch (UnsupportedOperationException e) {
        }
        try {
            map.keySet().clear();
            fail(failureMessage + " on map.keySet().clear");
        } catch (UnsupportedOperationException e) {
        }

        /* test map.values() operations */
        try {
            map.values().add("valueZ");
            fail(failureMessage + " on map.values().add");
        } catch (UnsupportedOperationException e) {
        }
        try {
            map.values().remove("valueA");
            fail(failureMessage + " on map.values().clear()");
        } catch (UnsupportedOperationException e) {
        }
        try {
            map.values().clear();
            fail(failureMessage + " on map.values().clear()");
        } catch (UnsupportedOperationException e) {
        }

        /* test map.entrySet() operations */
        Map.Entry<String, String> firstEntry = map.entrySet().iterator().next();
        try {
            map.entrySet().clear();
            fail(failureMessage + "on map.entrySet().clear");
        } catch (UnsupportedOperationException e) {
        }
        try {
            map.entrySet().remove(firstEntry);
            fail(failureMessage + " on map.entrySet().remove");
        } catch (UnsupportedOperationException e) {
        }
        try {
            map.entrySet().add(firstEntry);
            fail(failureMessage + "on map.entrySet().add");
        } catch (UnsupportedOperationException e) {
        }
        try {
            firstEntry.setValue("allZ");
            fail(failureMessage + "on firstEntry.setValue");
        } catch (UnsupportedOperationException e) {
        }
        try {
            map.clear();
            fail(failureMessage + " on map.clear");
        } catch (UnsupportedOperationException e) {
        }
    }

    /**
     * Tests exercise the cache.
     */
    @Test
    public void testConstantMapBACD() {
        TestConstants types = new TestConstants_factory().create();
        Map<String, String> map = types.mapBACD();
        Map<String, String> expectedMap = getMapFromArrayUsingASimpleRule(new String[]{
                "B", "A", "C", "D"});
        compareMapsComprehensively(map, expectedMap);
    }

    /**
     * Tests exercise the cache.
     */
    @Test
    public void testConstantMapBBB() {
        TestConstants types = new TestConstants_factory().create();
        Map<String, String> map = types.mapBBB();
        Map<String, String> expectedMap = getMapFromArrayUsingASimpleRule(new String[]{"B"});
        compareMapsComprehensively(map, expectedMap);
    }

    /**
     * Tests exercise the cache and check if Map works as the declared return
     * type.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void testConstantMapDCBA() {
        TestConstants types = new TestConstants_factory().create();
        Map<String, String> map = types.mapDCBA();
        Map<String, String> expectedMap = getMapFromArrayUsingASimpleRule(new String[]{
                "D", "C", "B", "A"});
        compareMapsComprehensively(map, expectedMap);
    }

    /**
     * Tests focus on correctness of entries, since ABCD exercises the map.
     */
    @Test
    public void testConstantMapEmpty() {
        TestConstants types = new TestConstants_factory().create();
        Map<String, String> map = types.mapEmpty();
        Map<String, String> expectedMap = new HashMap<String, String>();
        compareMapsComprehensively(map, expectedMap);
    }

    /**
     * Tests exercise the cache and check if Map works as the declared return
     * type.
     */
    @Test
    public void testConstantMapXYZ() {
        TestConstants types = new TestConstants_factory().create();
        Map<String, String> map = types.mapXYZ();
        Map<String, String> expectedMap = new HashMap<String, String>();
        expectedMap.put("keyX", "valueZ");
        expectedMap.put("keyY", "valueZ");
        expectedMap.put("keyZ", "valueZ");
        compareMapsComprehensively(map, expectedMap);
    }

    @Test
    public void testConstantStringArrays() {
        TestConstants types = new TestConstants_factory().create();
        String[] s;

        s = types.stringArrayABCDEFG();
        assertArrayEquals(new String[]{"A", "B", "C", "D", "E", "F", "G"}, s);

        s = types.stringArraySizeOneEmptyString();
        assertArrayEquals(new String[]{""}, s);

        s = types.stringArraySizeOneX();
        assertArrayEquals(new String[]{"X"}, s);

        s = types.stringArraySizeTwoBothEmpty();
        assertArrayEquals(new String[]{"", ""}, s);

        s = types.stringArraySizeThreeAllEmpty();
        assertArrayEquals(new String[]{"", "", ""}, s);

        s = types.stringArraySizeTwoWithEscapedComma();
        assertArrayEquals(new String[]{"X", ", Y"}, s);

        s = types.stringArraySizeOneWithBackslashX();
        assertArrayEquals(new String[]{"\\X"}, s);

        s = types.stringArraySizeThreeWithDoubleBackslash();
        assertArrayEquals(new String[]{"X", "\\", "Y"}, s);
    }


    @Test
    public void testConstantStrings() {
        TestConstants types = new TestConstants_factory().create();
        assertThat("string").isEqualTo(types.getString());
        assertThat("stringTrimsLeadingWhitespace").isEqualTo(
                types.stringTrimsLeadingWhitespace());
        assertThat("stringDoesNotTrimTrailingThreeSpaces   ").isEqualTo(
                types.stringDoesNotTrimTrailingThreeSpaces());
        assertThat("").isEqualTo(types.stringEmpty());
        String jaBlue = types.stringJapaneseBlue();
        assertThat("あお").isEqualTo(jaBlue);
        String jaGreen = types.stringJapaneseGreen();
        assertThat("みどり").isEqualTo(jaGreen);
        String jaRed = types.stringJapaneseRed();
        assertThat("あか").isEqualTo(jaRed);
    }

    @Test(expected = CouldNotLoadConstantsException.class)
    public void givenLocaleNotDefined_throwException() {
        System.setProperty("locale", "");
        WithDefaultLocale_factory.create();
    }

    @Test
    public void givenConstantsFactory_shouldCreateCurrentLocaleConstants() {
        System.setProperty("locale", "default");

        WithDefaultLocale defaultConstants = WithDefaultLocale_factory.create();

        assertThat(defaultConstants.add()).isEqualTo("add file en");
        assertThat(defaultConstants.remove()).isEqualTo("remove file en");
        assertThat(defaultConstants.change()).isEqualTo("change file en");
        assertThat(defaultConstants.price()).isEqualTo("price file en");
        assertThat(defaultConstants.symbol()).isEqualTo("symbol file en");
        assertThat(defaultConstants.stockWatcher()).isEqualTo("stcok watcher file en");
        assertThat(defaultConstants.count()).isEqualTo(20);
        assertThat(defaultConstants.enabled()).isEqualTo(true);


        System.setProperty("locale", "en");

        WithDefaultLocale enConstants = WithDefaultLocale_factory.create();

        assertThat(enConstants.add()).isEqualTo("add file en");
        assertThat(enConstants.remove()).isEqualTo("remove file en");
        assertThat(enConstants.change()).isEqualTo("change file en");
        assertThat(enConstants.price()).isEqualTo("price file en");
        assertThat(enConstants.symbol()).isEqualTo("symbol file en");
        assertThat(enConstants.stockWatcher()).isEqualTo("stcok watcher file en");
        assertThat(enConstants.count()).isEqualTo(20);
        assertThat(enConstants.enabled()).isEqualTo(true);

        System.setProperty("locale", "fr");

        WithDefaultLocale frConstants = WithDefaultLocale_factory.create();

        assertThat(frConstants.add()).isEqualTo("add file fr");
        assertThat(frConstants.remove()).isEqualTo("remove file fr");
        assertThat(frConstants.change()).isEqualTo("change file fr");
        assertThat(frConstants.price()).isEqualTo("price file fr");
        assertThat(frConstants.count()).isEqualTo(20);
        assertThat(frConstants.enabled()).isEqualTo(true);
    }

    @Test
    public void givenNoDefaultLocaleSpecified_thenDefaultShouldReturnConstantsFromNoLocaleProperties() {
        System.setProperty("locale", "default");

        WithoutDefaultLocale noDefaultLocaleConstants = WithoutDefaultLocale_factory.create();
        assertThat(noDefaultLocaleConstants.add()).isEqualTo("add file");
        assertThat(noDefaultLocaleConstants.remove()).isEqualTo("remove file");
        assertThat(noDefaultLocaleConstants.change()).isEqualTo("change file");
        assertThat(noDefaultLocaleConstants.price()).isEqualTo("price file");
        assertThat(noDefaultLocaleConstants.count()).isEqualTo(20);
        assertThat(noDefaultLocaleConstants.enabled()).isEqualTo(true);

    }

    private Map<String, String> getMapFromArrayUsingASimpleRule(String array[]) {
        Map<String, String> map = new HashMap<String, String>();
        for (String str : array) {
            map.put("key" + str, "value" + str);
        }
        return map;
    }

    // compare the map, entrySet, keySet, and values
    private void compareMapsComprehensively(Map<String, String> map,
                                            Map<String, String> expectedMap) {
        // checking both directions to verify that the equals implementation is
        // correct both ways
        assertThat(expectedMap).isEqualTo(map);
        assertThat(map).isEqualTo(expectedMap);
        assertThat(expectedMap.entrySet()).isEqualTo(map.entrySet());
        assertThat(map.entrySet()).isEqualTo(expectedMap.entrySet());
        assertThat(expectedMap.keySet()).isEqualTo(map.keySet());
        assertThat(map.keySet()).isEqualTo(expectedMap.keySet());
        assertThat(compare(expectedMap.values(), map.values())).isTrue();
        assertThat(compare(map.values(), expectedMap.values())).isTrue();
    }


    private <T> boolean compare(Collection<T> collection1,
                                Collection<T> collection2) {
        if (collection1 == null) {
            return (collection2 == null);
        }
        if (collection2 == null) {
            return false;
        }
        if (collection1.size() != collection2.size()) {
            return false;
        }
        for (T element1 : collection1) {
            boolean found = false;
            for (T element2 : collection2) {
                if (element1.equals(element2)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                return false;
            }
        }
        return true;
    }

    private void assertArrayEquals(String[] shouldBe, String[] test) {
        assertThat(shouldBe.length).isEqualTo(test.length);
        for (int i = 0; i < test.length; i++) {
            assertThat(shouldBe[i]).isEqualTo(test[i]);
        }
    }


}
