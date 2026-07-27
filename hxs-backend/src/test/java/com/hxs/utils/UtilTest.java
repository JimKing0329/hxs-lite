package com.hxs.utils;


import org.junit.jupiter.api.Test;



public class UtilTest {

    @Test
    public void test() throws Exception {
        String s = "+jYslp2kaN4oLXVg9UDtp2CKTXk0uDpF18Rj18=";

        System.out.println(AesUtil.decrypt( s));
    }
}
