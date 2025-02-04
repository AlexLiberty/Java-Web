package itstep.learning.services.time;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class CurrentTimeService implements TimeService{
    @Override
    public long getTimestamp() {
        return System.currentTimeMillis();
    }

    @Override
    public String getIsoTime() {
        return Instant.ofEpochMilli(getTimestamp())
                .atZone(ZoneId.systemDefault())
                .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }
}
