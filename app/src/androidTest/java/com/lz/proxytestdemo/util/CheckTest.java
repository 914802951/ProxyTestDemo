package com.lz.proxytestdemo.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by Administrator on 2017/11/22.
 */
public class CheckTest {
    @Test
    public void emptyString() throws Exception {
        assertEquals(Check.emptyString(null), true);
        assertEquals(Check.emptyString("  "), true);
        assertEquals(Check.emptyString("\n"), true);
        assertEquals(Check.emptyString("a\n"), false);
    }

    @Test
    public void legalIP() throws Exception {
        assertEquals(Check.legalIP("1.1.1.1"), true);
        assertEquals(Check.legalIP("256.1.1.1"), false);
        assertEquals(Check.legalIP("1.1.1"), false);
        assertEquals(Check.legalIP("1.1.1.1 "), false);
        assertEquals(Check.legalIP("1.1*1.1"), false);
        assertEquals(Check.legalIP("1.1.1.1\n"), false);
    }

}