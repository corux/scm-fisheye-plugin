package de.corux.scm.plugins.fisheye;

import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;

import sonia.scm.security.Role;

import com.google.inject.Inject;

/**
 * Handles the global configuration resource.
 */
@Path("plugins/fisheye/link")
public class FisheyeLinkRepositoriesResource
{
    private final RepositoryLinker linker;

    /**
     * Constructor.
     *
     * @param linker
     *            the repository linker
     */
    @Inject
    public FisheyeLinkRepositoriesResource(final RepositoryLinker linker)
    {
        this.linker = linker;
        Subject subject = SecurityUtils.getSubject();

        subject.checkRole(Role.ADMIN);
    }

    /**
     * Links all scm repositories with the fisheye repositories.
     *
     * @param configuration
     *            the new configuration
     */
    @POST
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public void linkRepositories(@FormParam("username") final String username,
            @FormParam("password") final String password)
    {
        linker.updateRepositoriesWithFisheyeNames(username, password);
    }
}
