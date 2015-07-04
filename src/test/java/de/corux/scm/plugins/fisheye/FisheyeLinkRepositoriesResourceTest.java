package de.corux.scm.plugins.fisheye;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;

import static org.junit.Assert.assertEquals;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.corux.scm.plugins.fisheye.client.GitRepository;
import sonia.scm.repository.Repository;

public class FisheyeLinkRepositoriesResourceTest
{
    @Before
    public void mockSubject()
    {
        Subject subject = mock(Subject.class);
        ThreadContext.bind(subject);
    }

    @Test
    public void retrieveMappingsWithExistingFisheyeRepo()
    {
        RepositoryLinker linker = mock(RepositoryLinker.class);
        Map<Repository, List<de.corux.scm.plugins.fisheye.client.Repository>> map = new HashMap<Repository, List<de.corux.scm.plugins.fisheye.client.Repository>>();
        Repository repository = new Repository("test-git", "git", "test-git");
        new FisheyeConfiguration(repository).setRepositories(Arrays.asList("current-fisheye-repo"));
        de.corux.scm.plugins.fisheye.client.Repository fisheyeRepo = new de.corux.scm.plugins.fisheye.client.Repository();
        map.put(repository, Arrays.asList(fisheyeRepo));
        when(linker.retrieveScmRepositoryToFisheyeRepositoriesMapping(Matchers.anyString(), Matchers.anyString()))
                .thenReturn(map);

        FisheyeLinkRepositoriesResource resource = new FisheyeLinkRepositoriesResource(linker);
        List<FisheyeReposDto> mappings = resource.retrieveMappings("test-username", "test-password");

        assertEquals(1, mappings.size());
        FisheyeReposDto mappingRepo = mappings.get(0);
        assertEquals(repository.getName(), mappingRepo.repository);
        assertEquals(1, mappingRepo.currentFisheyeRepositories.size());
        assertEquals("current-fisheye-repo", mappingRepo.currentFisheyeRepositories.get(0));
    }

    @Test
    public void retrieveMappingsWithNewFisheyeRepo()
    {
        RepositoryLinker linker = mock(RepositoryLinker.class);
        Map<Repository, List<de.corux.scm.plugins.fisheye.client.Repository>> map = new HashMap<Repository, List<de.corux.scm.plugins.fisheye.client.Repository>>();
        Repository repository = mock(Repository.class);
        when(repository.getType()).thenReturn("git");
        when(repository.getName()).thenReturn("test-git");
        when(repository.createUrl(Matchers.anyString())).thenReturn("http://test.url/repos/test-git");
        de.corux.scm.plugins.fisheye.client.Repository fisheyeRepo = new de.corux.scm.plugins.fisheye.client.Repository();
        GitRepository git = new GitRepository();
        git.setLocation(repository.createUrl(""));
        fisheyeRepo.setGit(git);
        fisheyeRepo.setName("new-fisheye-repo");
        map.put(repository, Arrays.asList(fisheyeRepo));
        when(linker.retrieveScmRepositoryToFisheyeRepositoriesMapping(Matchers.anyString(), Matchers.anyString()))
                .thenReturn(map);

        FisheyeLinkRepositoriesResource resource = new FisheyeLinkRepositoriesResource(linker);
        List<FisheyeReposDto> mappings = resource.retrieveMappings("test-username", "test-password");

        assertEquals(1, mappings.size());
        FisheyeReposDto mappingRepo = mappings.get(0);
        assertEquals(repository.getName(), mappingRepo.repository);
        assertEquals(1, mappingRepo.newFisheyeRepositories.size());
        assertEquals(fisheyeRepo.getName(), mappingRepo.newFisheyeRepositories.get(0));
    }

    @Test
    public void retrieveMappingsWhenNoNewFisheyeRepoFound()
    {
        RepositoryLinker linker = mock(RepositoryLinker.class);
        Map<Repository, List<de.corux.scm.plugins.fisheye.client.Repository>> map = new HashMap<Repository, List<de.corux.scm.plugins.fisheye.client.Repository>>();
        Repository repository = mock(Repository.class);
        when(repository.getType()).thenReturn("git");
        when(repository.getName()).thenReturn("test-git");
        map.put(repository, new ArrayList<de.corux.scm.plugins.fisheye.client.Repository>());
        when(linker.retrieveScmRepositoryToFisheyeRepositoriesMapping(Matchers.anyString(), Matchers.anyString()))
                .thenReturn(map);

        FisheyeLinkRepositoriesResource resource = new FisheyeLinkRepositoriesResource(linker);
        List<FisheyeReposDto> mappings = resource.retrieveMappings("test-username", "test-password");

        assertEquals(1, mappings.size());
        FisheyeReposDto mappingRepo = mappings.get(0);
        assertEquals(repository.getName(), mappingRepo.repository);
        assertEquals(0, mappingRepo.currentFisheyeRepositories.size());
        assertEquals(0, mappingRepo.newFisheyeRepositories.size());
    }
}
