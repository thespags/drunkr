package net.spals.drunkr.jersey;

import javax.ws.rs.*;
import javax.ws.rs.ext.ParamConverter;

import com.google.inject.Inject;

import net.spals.appbuilder.annotations.service.AutoBindInMap;
import net.spals.drunkr.db.DatabaseService;
import net.spals.drunkr.i18n.I18nSupport;
import net.spals.drunkr.model.JobOptions;

/**
 * Ability to convert an id from a {@link PathParam}, {@link QueryParam}, etc to a {@link JobOptions} seamlessly in a resource.
 * Throws a {@link NotFoundException} if the id is invalid.
 *
 * @author spags
 */
@AutoBindInMap(baseClass = ParamConverter.class, key = "net.spals.drunkr.model.JobOptions")
class JobConverter implements ParamConverter<JobOptions> {

    private final DatabaseService dbService;
    private final I18nSupport i18nSupport;

    @Inject
    JobConverter(final DatabaseService dbService, final I18nSupport i18nSupport) {
        this.dbService = dbService;
        this.i18nSupport = i18nSupport;
    }

    @Override
    public JobOptions fromString(final String jobId) {
        return dbService.getJob(jobId)
            .orElseThrow(() -> new NotFoundException(i18nSupport.getLabel("invalid.job", jobId)));
    }

    @Override
    public String toString(final JobOptions value) {
        return value.id().toHexString();
    }
}