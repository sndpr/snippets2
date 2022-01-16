package org.psc.lang;

import org.apache.commons.lang3.StringUtils;

public class LangApplication {
    public static void main(String[] args) {
        var in = "0000123456";
        var s = StringUtils.stripStart(in, "0");
        System.out.println(s);
    }
}
