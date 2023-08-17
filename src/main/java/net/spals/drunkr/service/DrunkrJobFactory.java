package net.spals.drunkr.service;

import com.google.inject.assistedinject.Assisted;

import net.spals.appbuilder.annotations.service.AutoBindFactory;
import net.spals.drunkr.model.JobOptions;

/**
 * Factory for creating {@link DrunkrJob} with auto binding to services.
 * Anything not marked with {@link Assisted} in {@link DrunkrJob}'s constructors are injected by app builder.
 * Otherwise the caller, via this factory, provides the values.
 *
 * @author spags
 */
@AutoBindFactory
public interface DrunkrJobFactory {

    /**
     * Starts scraping for BAC of the given person, considering the started drinking time as the one provided.
     *
     * @param options holder a {@link JobOptions} for how to run a drunkr job.
     * @return the task
     */
    DrunkrJob createJob(JobOptions options);
}
