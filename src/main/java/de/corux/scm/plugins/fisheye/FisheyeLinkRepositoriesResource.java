package de.corux.scm.plugins.fisheye;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;

import com.google.inject.Inject;

import sonia.scm.repository.Repository;
import sonia.scm.security.Role;

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
     * Retrieves all possible mappings for fisheye to scm repositories.
     */
    @POST
    @Path("retrieve-mapping")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public List<FisheyeReposDto> retrieveMappings(@FormParam("username") final String username,
            @FormParam("password") final String password)
    {
        List<FisheyeReposDto> list = new ArrayList<FisheyeReposDto>();
        Map<Repository, List<de.corux.scm.plugins.fisheye.client.Repository>> mapping = linker
                .retrieveScmRepositoryToFisheyeRepositoriesMapping(username, password);
        for (Repository repo : mapping.keySet())
        {
            FisheyeReposDto dto = new FisheyeReposDto();
            dto.repository = repo.getName();
            dto.currentFisheyeRepositories.addAll(new FisheyeConfiguration(repo).getRepositories());
            for (de.corux.scm.plugins.fisheye.client.Repository fisheyeRepo : mapping.get(repo))
            {
                dto.newFisheyeRepositories.add(fisheyeRepo.getName());
            }

            list.add(dto);
        }

        return list;
    }

    /**
     * Links all of the selected repositories with the fisheye repositories.
     */
    @POST
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public void linkRepositories(@FormParam("username") final String username,
            @FormParam("password") final String password,
            @FormParam("repositories") final List<String> selectedRepositories)
    {
        linker.updateRepositoriesWithFisheyeNames(username, password, selectedRepositories);
    }
}
