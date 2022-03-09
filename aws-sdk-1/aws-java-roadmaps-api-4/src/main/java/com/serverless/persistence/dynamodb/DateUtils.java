package com.serverless.persistence.dynamodb;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;

public class DateUtils {

    public static Date asDateUTC(LocalDate localDate) {
        return Date.from(localDate.atStartOfDay().atZone(ZoneOffset.UTC).toInstant());
    }

    public static Date asDateUTC(LocalDateTime localDateTime) {
        return Date.from(localDateTime.atZone(ZoneOffset.UTC).toInstant());
    }

    public static LocalDate asLocalDateUTC(Date date) {
        return Instant.ofEpochMilli(date.getTime()).atZone(ZoneOffset.UTC).toLocalDate();
    }

    public static LocalDateTime asLocalDateTimeUTC(Date date) {
        return Instant.ofEpochMilli(date.getTime()).atZone(ZoneOffset.UTC).toLocalDateTime();
    }
}