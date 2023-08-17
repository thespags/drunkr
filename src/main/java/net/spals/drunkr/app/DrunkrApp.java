package net.spals.drunkr.app;

import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.spals.appbuilder.app.dropwizard.DropwizardWebApp;
import net.spals.appbuilder.config.service.ServiceScan;
import net.spals.appbuilder.graph.model.ServiceGraphFormat;
import net.spals.appbuilder.keystore.core.KeyStore;

/**
 * Uses <a href='https://github.com/spals/appbuilder'>AppBuilder</a> to start the drunkr service.
 * AppBuilder is built on top of many things, the entry point here is Dropwizard.
 * Dropwizard, in turn, is built on top of many things. In Dropwizard, we rely heavily on are Jersey, JAX-RS (JSR 311 & JSR 339),
 * and Jackson.
 *
 * @author spags
 */
public class DrunkrApp extends Application<Configuration> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DrunkrApp.class);
    private static final String APP_CONFIG_FILE_NAME = "config/drunkr-app.yml";
    private static final String SERVICE_CONFIG_FILE_NAME = "config/drunkr.conf";
    private DropwizardWebApp.Builder webAppDelegateBuilder;
    private DropwizardWebApp webAppDelegate;

    public static void main(final String[] args) throws Throwable {
        new DrunkrApp().run("server", APP_CONFIG_FILE_NAME);
    }

    @Override
    public void initialize(final Bootstrap<Configuration> bootstrap) {
        webAppDelegateBuilder = new DropwizardWebApp.Builder(bootstrap, LOGGER)
            .enableServiceGraph(ServiceGraphFormat.TEXT)
            .setServiceConfigFromClasspath(SERVICE_CONFIG_FILE_NAME)
            .setServiceScan(
                new ServiceScan.Builder()
                    // Have the AppBuilder framework scan the net.spals.drunkr package for micro-services.
                    .addServicePackages("net.spals.drunkr")
                    .addDefaultServices(KeyStore.class)
                    //.addDefaultServices(MapStore.class)
                    .build()
            );
    }

    @Override
    public void run(final Configuration configuration, final Environment environment) {
        // We have to register this feature in Jersey, to use our custom ObjectMappers.
        environment.jersey().register(JacksonFeature.class);
        webAppDelegate = webAppDelegateBuilder.setEnvironment(environment).build();
    }
}
