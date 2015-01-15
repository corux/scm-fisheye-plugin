package de.corux.scm.plugins.fisheye;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.core.UriBuilder;

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

    @Inject
    public RepositoryLinker(final FisheyeClient client, final RepositoryManager repoManager,
            final ScmConfiguration scmConfiguration)
    {
        this.client = client;
        this.repoManager = repoManager;
        this.scmConfiguration = scmConfiguration;
    }

    /**
     * Update all scm repositories with the fisheye repository names.
     */
    public void updateRepositoriesWithFisheyeNames(final String username, final String password)
    {
        client.SetCredentials(username, password);

        Dictionary<sonia.scm.repository.Repository, List<Repository>> list = linkRepositories();
        Enumeration<sonia.scm.repository.Repository> keys = list.keys();
        while (keys.hasMoreElements())
        {
            sonia.scm.repository.Repository repo = keys.nextElement();
            FisheyeConfiguration fisheyeConfiguration = new FisheyeConfiguration(repo);

            List<String> fisheyeRepoNames = new ArrayList<String>();
            for (Repository i : list.get(repo))
            {
                fisheyeRepoNames.add(i.getName());
            }

            fisheyeConfiguration.setRepositories(fisheyeRepoNames);
        }
    }

    private Dictionary<sonia.scm.repository.Repository, List<Repository>> linkRepositories()
    {
        Dictionary<sonia.scm.repository.Repository, List<Repository>> dict = new Hashtable<sonia.scm.repository.Repository, List<Repository>>();

        Collection<sonia.scm.repository.Repository> scmRepositories = repoManager.getAll();
        List<Repository> fisheyeRepositories = client.listRepositories();

        String baseUrl = scmConfiguration.getBaseUrl();
        for (sonia.scm.repository.Repository scmRepo : scmRepositories)
        {
            String url = scmRepo.createUrl(baseUrl);
            List<Repository> repos = getFisheyeRepositoriesForUrl(url, fisheyeRepositories);
            dict.put(scmRepo, repos);
        }

        return dict;
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
