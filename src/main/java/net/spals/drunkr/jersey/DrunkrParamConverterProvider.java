package net.spals.drunkr.jersey;

import javax.ws.rs.ext.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Map;

import com.google.inject.Inject;

import net.spals.appbuilder.annotations.service.AutoBindSingleton;

/**
 * Allows for seamless injection of complex in our resources. As an example this helps us having each internal API
 * do a user lookup. Instead Jersey can provide that for us in the external API.
 *
 * @author spags
 */
@AutoBindSingleton
@Provider
class DrunkrParamConverterProvider implements ParamConverterProvider {

    private final Map<String, ParamConverter> converters;

    @Inject
    DrunkrParamConverterProvider(final Map<String, ParamConverter> converters) {
        this.converters = converters;
    }

    @Override
    public <T> ParamConverter<T> getConverter(
        final Class<T> clazz,
        final Type genericType,
        final Annotation[] annotations
    ) {
        //noinspection unchecked
        return (ParamConverter<T>) converters.get(clazz.getName());
    }
}