package de.corux.scm.plugins.fisheye;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.corux.scm.plugins.fisheye.client.FisheyeClient;
import de.corux.scm.plugins.fisheye.client.GitRepository;
import de.corux.scm.plugins.fisheye.client.HgRepository;
import de.corux.scm.plugins.fisheye.client.SvnRepository;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryManager;

public class RepositoryLinkerTest
{
    private RepositoryLinker linker;
    private FisheyeClient client;
    private RepositoryManager repoManager;
    private Repository repository;

    private final String username = "test-username";
    private final String password = "test-password";

    @Before
    public void initRepositoryLinker() throws IOException
    {
        client = mock(FisheyeClient.class);
        repoManager = mock(RepositoryManager.class);
        repository = mock(Repository.class);
        linker = new RepositoryLinker(client, repoManager, mock(ScmConfiguration.class));
    }

    @Test
    public void testUpdateRepositoriesMultipleFisheyeReposMappedToOneScmRepo() throws IOException
    {
        // arrange
        String repoFisheyeUrl = "https://user:pass@scm.test.com/scm/repo.git";
        String repoScmUrl = "http://scm.test.com/scm/repo.git";
        String repo1FisheyeName = "fisheye-git-repo1";
        String repo2FisheyeName = "fisheye-git-repo2";

        de.corux.scm.plugins.fisheye.client.Repository repo1 = new de.corux.scm.plugins.fisheye.client.Repository();
        repo1.setName(repo1FisheyeName);
        GitRepository git = new GitRepository();
        repo1.setGit(git);

        de.corux.scm.plugins.fisheye.client.Repository repo2 = new de.corux.scm.plugins.fisheye.client.Repository();
        repo2.setName(repo2FisheyeName);
        repo2.setGit(git);

        git.setLocation(repoFisheyeUrl);
        when(repository.createUrl(Matchers.anyString())).thenReturn(repoScmUrl);

        // act
        runTestUpdateRepositories(null, repo1, repo2);
    }

    @Test
    public void testUpdateRepositoriesWhenFisheyeRepoIsNotAnUrl() throws IOException
    {
        // arrange
        String repoFisheyeUrl = "file://C:\\Users\\fecru\\home/amps-repos/checkstyle-svn";
        String repoScmUrl = "http://scm.test.com/scm/repo.git";
        String repoFisheyeName = "fisheye-git-repo";

        de.corux.scm.plugins.fisheye.client.Repository repo = new de.corux.scm.plugins.fisheye.client.Repository();
        repo.setName(repoFisheyeName);
        GitRepository git = new GitRepository();
        repo.setGit(git);

        git.setLocation(repoFisheyeUrl);
        when(repository.createUrl(Matchers.anyString())).thenReturn(repoScmUrl);

        List<de.corux.scm.plugins.fisheye.client.Repository> fisheyeReposList = Arrays.asList(repo);
        when(client.listRepositories()).thenReturn(fisheyeReposList);
        when(repoManager.getAll()).thenReturn(Arrays.asList(repository));

        // act
        linker.updateRepositoriesWithFisheyeNames(username, password, null);

        // assert
        verify(client, times(1)).setCredentials(username, password);
        String prop = FisheyeConfiguration.PROPERTY_FISHEYE_REPOSITORIES;
        verify(repository, times(1)).setProperty(Matchers.eq(prop), Matchers.eq(""));
    }

    @Test
    public void testUpdateRepositoriesWhenRepoToUpdateDoesNotExist() throws IOException
    {
        // arrange
        String repoFisheyeUrl = "https://user:pass@scm.test.com/scm/repo.git";
        String repoScmUrl = "http://scm.test.com/scm/repo.git";
        String repoFisheyeName = "fisheye-git-repo";

        de.corux.scm.plugins.fisheye.client.Repository repo = new de.corux.scm.plugins.fisheye.client.Repository();
        repo.setName(repoFisheyeName);
        GitRepository git = new GitRepository();
        repo.setGit(git);

        git.setLocation(repoFisheyeUrl);
        when(repository.createUrl(Matchers.anyString())).thenReturn(repoScmUrl);

        // act
        runTestUpdateRepositories(Arrays.asList("repo-does-not-exist"), repo);
    }

    @Test
    public void testUpdateRepositoryByName() throws IOException
    {
        // arrange
        String repoFisheyeUrl = "https://user:pass@scm.test.com/scm/repo.git";
        String repoScmUrl = "http://scm.test.com/scm/repo.git";
        String repoFisheyeName = "fisheye-git-repo";

        de.corux.scm.plugins.fisheye.client.Repository repo = new de.corux.scm.plugins.fisheye.client.Repository();
        repo.setName(repoFisheyeName);
        GitRepository git = new GitRepository();
        repo.setGit(git);

        git.setLocation(repoFisheyeUrl);
        when(repository.createUrl(Matchers.anyString())).thenReturn(repoScmUrl);

        // act
        runTestUpdateRepositories(Arrays.asList(repository.getName()), repo);
    }

    @Test
    public void testUpdateRepositoriesForGit() throws IOException
    {
        // arrange
        String repoFisheyeUrl = "https://user:pass@scm.test.com/scm/repo.git";
        String repoScmUrl = "http://scm.test.com/scm/repo.git";
        String repoFisheyeName = "fisheye-git-repo";

        de.corux.scm.plugins.fisheye.client.Repository repo = new de.corux.scm.plugins.fisheye.client.Repository();
        repo.setName(repoFisheyeName);
        GitRepository git = new GitRepository();
        repo.setGit(git);

        git.setLocation(repoFisheyeUrl);
        when(repository.createUrl(Matchers.anyString())).thenReturn(repoScmUrl);

        // act
        runTestUpdateRepositories(null, repo);
    }

    @Test
    public void testUpdateRepositoriesForHg() throws IOException
    {
        // arrange
        String repoFisheyeUrl = "https://user:pass@scm.test.com/scm/repo.hg";
        String repoScmUrl = "http://scm.test.com/scm/repo.hg";
        String repoFisheyeName = "fisheye-hg-repo";

        de.corux.scm.plugins.fisheye.client.Repository repo = new de.corux.scm.plugins.fisheye.client.Repository();
        repo.setName(repoFisheyeName);
        HgRepository hg = new HgRepository();
        repo.setHg(hg);

        hg.setLocation(repoFisheyeUrl);
        when(repository.createUrl(Matchers.anyString())).thenReturn(repoScmUrl);

        // act
        runTestUpdateRepositories(null, repo);
    }

    @Test
    public void testUpdateRepositoriesForSvn() throws IOException
    {
        // arrange
        String repoFisheyeUrl = "https://scm.test.com/scm/repo.svn";
        String repoScmUrl = repoFisheyeUrl;
        String repoFisheyeName = "fisheye-svn-repo";

        de.corux.scm.plugins.fisheye.client.Repository repo = new de.corux.scm.plugins.fisheye.client.Repository();
        repo.setName(repoFisheyeName);
        SvnRepository svn = new SvnRepository();
        repo.setSvn(svn);

        svn.setUrl(repoFisheyeUrl);
        when(repository.createUrl(Matchers.anyString())).thenReturn(repoScmUrl);

        // act
        runTestUpdateRepositories(null, repo);
    }

    private void runTestUpdateRepositories(List<String> reposToUpdate,
            de.corux.scm.plugins.fisheye.client.Repository... fisheyeRepos) throws IOException
    {
        // arrange
        List<Repository> scmRepos = new ArrayList<Repository>();
        scmRepos.add(repository);

        List<String> fisheyeRepoNames = new ArrayList<String>();
        List<de.corux.scm.plugins.fisheye.client.Repository> fisheyeReposList = new ArrayList<de.corux.scm.plugins.fisheye.client.Repository>();
        for (de.corux.scm.plugins.fisheye.client.Repository i : fisheyeRepos)
        {
            fisheyeReposList.add(i);
            fisheyeRepoNames.add(i.getName());
        }

        when(client.listRepositories()).thenReturn(fisheyeReposList);
        when(repoManager.getAll()).thenReturn(scmRepos);

        // act
        linker.updateRepositoriesWithFisheyeNames(username, password, reposToUpdate);

        // assert
        verify(client, times(1)).setCredentials(username, password);
        String prop = FisheyeConfiguration.PROPERTY_FISHEYE_REPOSITORIES;

        if (reposToUpdate == null || reposToUpdate.contains(repository.getName()))
        {
            String repos = StringUtils.join(fisheyeRepoNames, ',');
            verify(repository, times(1)).setProperty(Matchers.eq(prop), Matchers.eq(repos));
        }
        else
        {
            verify(repository, times(0)).setProperty(Matchers.eq(prop), Matchers.anyString());
        }
    }
}
