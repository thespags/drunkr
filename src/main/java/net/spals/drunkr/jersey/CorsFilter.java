package net.spals.drunkr.jersey;

import javax.ws.rs.container.*;
import javax.ws.rs.ext.Provider;

import net.spals.appbuilder.annotations.service.AutoBindSingleton;

/**
 * Cross-Origin Resource Sharing, <a href='https://en.wikipedia.org/wiki/Cross-origin_resource_sharing'>CORS</a>
 * filter for Jersey.
 *
 * @author spags
 */
@AutoBindSingleton
@Provider
class CorsFilter implements ContainerResponseFilter {

    CorsFilter() {
    }

    @Override
    public void filter(
        final ContainerRequestContext requestContext,
        final ContainerResponseContext responseContext
    ) {
        responseContext.getHeaders().add("Access-Control-Allow-Origin", "*");
        responseContext.getHeaders().add("Access-Control-Allow-Credentials", "true");
        responseContext.getHeaders().add("Access-Control-Allow-Headers", "content-type");
        responseContext.getHeaders().add("Access-Control-Allow-Methods", "HEAD, GET, OPTIONS, PATCH, POST, DELETE, PUT");
    }
}