package com.stc.apm.utilities;

import java.time.Instant;
import java.util.Date;

public class ApiSystemTime {

    public static Instant getInstantTimeAsInstant () {
        return Instant.now();
    }

    public static String getInstantTimeAsString () {
        return getInstantTimeAsInstant().toString();
    }

    public static Date getInstantTimeAsUTCDate() {
        Instant instant = Instant.parse(getInstantTimeAsString());
        return Date.from(instant);
    }

}
