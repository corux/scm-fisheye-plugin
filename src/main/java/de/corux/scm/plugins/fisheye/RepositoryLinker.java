package de.corux.scm.plugins.fisheye;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.ws.rs.core.UriBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.corux.scm.plugins.fisheye.client.FisheyeClient;
import de.corux.scm.plugins.fisheye.client.Repository;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.repository.RepositoryManager;

/**
 * Provides methods for linking scm repositories to fisheye repositories.
 */
public class RepositoryLinker
{
    private final FisheyeClient client;
    private final RepositoryManager repoManager;
    private final ScmConfiguration scmConfiguration;

    /**
     * The logger for {@link RepositoryLinker}.
     */
    private static final Logger logger = LoggerFactory.getLogger(RepositoryLinker.class);

    @Inject
    public RepositoryLinker(final FisheyeClient client, final RepositoryManager repoManager,
            final ScmConfiguration scmConfiguration)
    {
        this.client = client;
        this.repoManager = repoManager;
        this.scmConfiguration = scmConfiguration;
    }

    /**
     * Update scm repositories with the fisheye repository names.
     *
     * @param username
     *            the username for accessing the fisheye API
     * @param password
     *            the password for accessing the fisheye API
     * @param repositories
     *            the scm repository names to link with fisheye repositories.
     *            set this to <code>null</code> to link all repositories.
     */
    public void updateRepositoriesWithFisheyeNames(final String username, final String password,
            final List<String> repositories)
    {
        Map<sonia.scm.repository.Repository, List<Repository>> map = retrieveScmRepositoryToFisheyeRepositoriesMapping(
                username, password);
        Set<sonia.scm.repository.Repository> keys = map.keySet();
        for (sonia.scm.repository.Repository repo : keys)
        {
            if (repositories != null && !repositories.contains(repo.getName()))
            {
                // skip not-selected repositories
                continue;
            }

            FisheyeConfiguration fisheyeConfiguration = new FisheyeConfiguration(repo);

            List<String> fisheyeRepoNames = new ArrayList<String>();
            for (Repository i : map.get(repo))
            {
                fisheyeRepoNames.add(i.getName());
            }

            fisheyeConfiguration.setRepositories(fisheyeRepoNames);
        }
    }

    /**
     * Retrieve the scm repository to fisheye repositories mapping. For each scm
     * repository a list of fisheye repositories, which are configured to use
     * the url of the scm repository, are returned.
     *
     * @param username
     *            the username for accessing the fisheye API
     * @param password
     *            the password for accessing the fisheye API
     * @return the scm to fisheye repository mapping.
     */
    public Map<sonia.scm.repository.Repository, List<Repository>> retrieveScmRepositoryToFisheyeRepositoriesMapping(
            final String username, final String password)
    {
        client.SetCredentials(username, password);
        Map<sonia.scm.repository.Repository, List<Repository>> map = new HashMap<sonia.scm.repository.Repository, List<Repository>>();

        Collection<sonia.scm.repository.Repository> scmRepositories = repoManager.getAll();
        List<Repository> fisheyeRepositories;
        try
        {
            fisheyeRepositories = client.listRepositories();
        }
        catch (IOException e)
        {
            logger.error("Failed to retrieve fisheye repositories", e);
            throw new RuntimeException(e);
        }

        String baseUrl = scmConfiguration.getBaseUrl();
        for (sonia.scm.repository.Repository scmRepo : scmRepositories)
        {
            String url = scmRepo.createUrl(baseUrl);
            List<Repository> repos = getFisheyeRepositoriesForUrl(url, fisheyeRepositories);
            map.put(scmRepo, repos);
        }

        return map;
    }

    private List<Repository> getFisheyeRepositoriesForUrl(final String url, final List<Repository> fisheyeRepositories)
    {
        List<Repository> result = new ArrayList<Repository>();
        for (Repository repo : fisheyeRepositories)
        {
            String repoUrl = null;
            if (repo.getGit() != null)
            {
                repoUrl = repo.getGit().getLocation();
            }
            else if (repo.getHg() != null)
            {
                repoUrl = repo.getHg().getLocation();
            }
            else if (repo.getSvn() != null)
            {
                repoUrl = repo.getSvn().getUrl();
            }

            if (normalizeUrl(url).equals(normalizeUrl(repoUrl)))
            {
                result.add(repo);
            }
        }

        return result;
    }

    /**
     * Removes the protocol and user information from the given URI.
     *
     * @param uri
     *            the URI.
     * @return the string normalized URI.
     */
    private String normalizeUrl(final String uri)
    {
        UriBuilder uriBuilder = UriBuilder.fromUri(uri).userInfo(null).scheme(null);
        return uriBuilder.build().toString();
    }
}
