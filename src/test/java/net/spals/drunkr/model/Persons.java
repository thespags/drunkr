package net.spals.drunkr.model;

/**
 * Hard coded list of persons for testing purposes.
 *
 * @author spags
 */
public class Persons {

    public static final String SPAGS_NUMBER = "+14122513259";
    public static final Person SPAGS = new Person.Builder()
        .userName("spags")
        .phoneNumber(SPAGS_NUMBER)
        .weight(185)
        .gender(Gender.MALE)
        .build();
    public static final String BROCKS_NUMBER = "+15754304788";
    public static final Person BROCK = new Person.Builder()
        .userName("jb")
        .phoneNumber(BROCKS_NUMBER)
        .weight(145)
        .gender(Gender.MALE)
        .build();
}
