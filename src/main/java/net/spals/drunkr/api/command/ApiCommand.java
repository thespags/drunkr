package net.spals.drunkr.api.command;

import javax.ws.rs.core.Response;
import java.util.Map;

/**
 * API Command that links legacy commands with API
 *
 * @author jbrock
 */
public interface ApiCommand {

    Response run(Map<String, Object> request);
}
