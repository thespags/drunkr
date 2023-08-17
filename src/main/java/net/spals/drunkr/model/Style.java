package net.spals.drunkr.model;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.common.base.CaseFormat;
import com.google.common.base.Functions;

/**
 * Enum representing a style of serving which match to a standard-ish serving size for determining alcohol content.
 *
 * @author jspagnola
 */
public enum Style {

    NONE(12.0),
    TASTER(4.0),
    CAN(12.0),
    BOTTLE(12.0),
    DRAFT(16.0),
    NITRO(16.0),
    CASK(12.0),
    CROWLER(32.0),
    GROWLER(32.0),
    SHOT(1.5),
    WINE(5);

    private static final Map<String, Style> NAME_TO_STYLE = Arrays.stream(Style.values())
        .collect(
            Collectors.toMap(
                x -> CaseFormat.UPPER_UNDERSCORE.converterTo(CaseFormat.UPPER_CAMEL).convert(x.name()),
                Functions.identity()
            )
        );
    private static final Map<String, Style> NAME_TO_STYLE_INSENSITIVE = Arrays.stream(Style.values())
        .collect(Collectors.toMap(Enum::name, Functions.identity()));
    private final double servingSize;

    Style(final double servingSize) {
        this.servingSize = servingSize;
    }

    /**
     * Gets the style based on untappd casing, {@link CaseFormat#UPPER_CAMEL}.
     * If null or not found returns {@link #NONE}.
     *
     * @param style a nullable case sensitive string representation
     * @return the style if found otherwise none
     */
    public static Style get(final String style) {
        return NAME_TO_STYLE.getOrDefault(style, NONE);
    }

    /**
     * Gets the style based on any casing. If null or not found returns {@link #NONE}.
     *
     * @param style a nullable case insensitive string representation
     * @return the style if found otherwise none
     */
    public static Style getInsensitive(final String style) {
        if (style == null) {
            return NONE;
        }
        return NAME_TO_STYLE_INSENSITIVE.getOrDefault(style.toUpperCase(), NONE);
    }

    public double getServingSize() {
        return servingSize;
    }
}