package de.corux.scm.plugins.fisheye;

import com.google.inject.Inject;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import sonia.scm.security.Role;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * Handles the global configuration resource.
 */
@Path("plugins/fisheye/global-config")
public class FisheyeGlobalResource
{
    private final FisheyeContext context;

    /**
     * Constructor.
     *
     * @param context
     *            the fisheye context
     */
    @Inject
    public FisheyeGlobalResource(final FisheyeContext context)
    {
        Subject subject = SecurityUtils.getSubject();

        subject.checkRole(Role.ADMIN);
        this.context = context;
    }

    /**
     * Gets the global configuration.
     *
     * @return the global configuration
     */
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public FisheyeGlobalConfiguration getConfiguration()
    {
        return context.getGlobalConfiguration();
    }

    /**
     * Sets the global configuration.
     *
     * @param configuration
     *            the new configuration
     */
    @POST
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public void setConfiguration(final FisheyeGlobalConfiguration configuration)
    {
        context.setGlobalConfiguration(configuration);
    }
}
