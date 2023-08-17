package net.spals.drunkr.jersey;

import javax.ws.rs.*;
import javax.ws.rs.ext.ParamConverter;

import com.google.inject.Inject;

import net.spals.appbuilder.annotations.service.AutoBindInMap;
import net.spals.drunkr.common.PhoneNumbers;
import net.spals.drunkr.db.DatabaseService;
import net.spals.drunkr.i18n.I18nSupport;
import net.spals.drunkr.model.Person;

/**
 * Ability to convert an id from a {@link PathParam}, {@link QueryParam}, etc to a {@link Person} seamlessly in a resource.
 * Throws a {@link NotFoundException} if the id is invalid.
 *
 * @author spags
 */
@AutoBindInMap(baseClass = ParamConverter.class, key = "net.spals.drunkr.model.Person")
class PersonConverter implements ParamConverter<Person> {

    private final DatabaseService dbService;
    private final I18nSupport i18nSupport;

    @Inject
    PersonConverter(final DatabaseService dbService, final I18nSupport i18nSupport) {
        this.dbService = dbService;
        this.i18nSupport = i18nSupport;
    }

    @Override
    public Person fromString(final String userIdNameOrPhone) {
        return dbService.getPerson(PhoneNumbers.tryParse(userIdNameOrPhone))
            .orElseThrow(() -> new NotFoundException(i18nSupport.getLabel("invalid.user", userIdNameOrPhone)));
    }

    @Override
    public String toString(final Person value) {
        return value.id().toHexString();
    }
}
