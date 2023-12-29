package org.psc.concurrency;

import java.util.logging.Logger;

class DoubleCheckedLocking {
    private volatile Logger logger;

    public Logger logger() {
        Logger v = logger;
        if (v == null) {
            synchronized (this) {
                v = logger;
                if (v == null) {
                    // reassigned local variable
                    logger = v = Logger.getLogger("com.foo.Bar");
                }
            }
        }
        return v;
    }

}
