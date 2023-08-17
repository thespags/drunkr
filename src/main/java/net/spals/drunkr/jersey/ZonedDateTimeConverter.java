package net.spals.drunkr.jersey;

import javax.ws.rs.ext.ParamConverter;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import net.spals.appbuilder.annotations.service.AutoBindInMap;
import net.spals.drunkr.common.ZonedDateTimes;

/**
 * Converts {@link ZonedDateTime} from {@link DateTimeFormatter#ISO_OFFSET_DATE_TIME}.
 *
 * @author spags
 */
@AutoBindInMap(baseClass = ParamConverter.class, key = "java.time.ZonedDateTime")
class ZonedDateTimeConverter implements ParamConverter<ZonedDateTime> {

    ZonedDateTimeConverter() {
    }

    @Override
    public ZonedDateTime fromString(final String value) {
        return ZonedDateTimes.parseApi(value);
    }

    @Override
    public String toString(final ZonedDateTime value) {
        return ZonedDateTimes.formatApi(value);
    }
}