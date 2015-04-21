package de.corux.scm.plugins.fisheye;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;

import sonia.scm.repository.Repository;
import sonia.scm.store.Store;
import sonia.scm.store.StoreFactory;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class FisheyeContextTest
{
    private FisheyeContext context;

    @SuppressWarnings("unchecked")
    @Before
    public void initFisheyeContext() throws IOException
    {
        Store<FisheyeGlobalConfiguration> store = mock(Store.class);
        StoreFactory storeFactory = mock(StoreFactory.class);
        when(storeFactory.getStore(Matchers.any(Class.class), Matchers.anyString())).thenReturn(store);

        context = new FisheyeContext(storeFactory);
    }

    @Test
    public void testGetFisheyeRepositoriesEmpty() throws IOException
    {
        // arrange
        Repository repository = mock(Repository.class);

        // act
        List<String> result = context.getFisheyeRepositories(repository);

        // assert
        assertTrue(result.isEmpty());
    }

    @Test
    public void testGetFisheyeRepositoriesGetConfiguredRepo() throws IOException
    {
        // arrange
        String fisheyeRepo = "fisheyeRepo1";
        Repository repository = mock(Repository.class);
        when(repository.getProperty(Matchers.eq(FisheyeConfiguration.PROPERTY_FISHEYE_REPOSITORIES))).thenReturn(
                fisheyeRepo);

        // act
        List<String> result = context.getFisheyeRepositories(repository);

        // assert
        assertEquals(1, result.size());
        assertEquals(fisheyeRepo, result.get(0));
    }

    @Test
    public void testGetFisheyeRepositoriesGetMultipleConfiguredRepo() throws IOException
    {
        // arrange
        List<String> fisheyeRepos = new ArrayList<String>();
        fisheyeRepos.add("fisheyeRepo1");
        fisheyeRepos.add("fisheyeRepo2");
        fisheyeRepos.add("fisheyeRepo3");
        Repository repository = mock(Repository.class);
        when(repository.getProperty(Matchers.eq(FisheyeConfiguration.PROPERTY_FISHEYE_REPOSITORIES))).thenReturn(
                StringUtils.join(fisheyeRepos, ','));

        // act
        List<String> result = context.getFisheyeRepositories(repository);

        // assert
        assertEquals(fisheyeRepos.size(), result.size());
        for (int i = 0; i < fisheyeRepos.size(); i++)
        {
            assertEquals(fisheyeRepos.get(i), result.get(i));
        }
    }

    @Test
    public void testGetFisheyeRepositoriesGetDefaultFisheyeRepo() throws IOException
    {
        // arrange
        String repoName = "ScmRepoName";
        Repository repository = mock(Repository.class);
        FisheyeGlobalConfiguration globalConfiguration = mock(FisheyeGlobalConfiguration.class);
        when(globalConfiguration.useRepositoryNameAsDefault()).thenReturn(true);
        when(repository.getName()).thenReturn(repoName);

        // act
        context.setGlobalConfiguration(globalConfiguration);
        List<String> result = context.getFisheyeRepositories(repository);

        // assert
        assertEquals(1, result.size());
        assertEquals(repoName, result.get(0));
    }

    @Test
    public void testGetFisheyeRepositoriesDoNotGetDefaultIfFisheyeRepoIsConfigured() throws IOException
    {
        // arrange
        String repoName = "ScmRepoName";
        String fisheyeRepo = "fisheyeRepo1";
        Repository repository = mock(Repository.class);
        FisheyeGlobalConfiguration globalConfiguration = mock(FisheyeGlobalConfiguration.class);
        when(globalConfiguration.useRepositoryNameAsDefault()).thenReturn(true);
        when(repository.getName()).thenReturn(repoName);
        when(repository.getProperty(Matchers.eq(FisheyeConfiguration.PROPERTY_FISHEYE_REPOSITORIES))).thenReturn(
                fisheyeRepo);

        // act
        context.setGlobalConfiguration(globalConfiguration);
        List<String> result = context.getFisheyeRepositories(repository);

        // assert
        assertEquals(1, result.size());
        assertEquals(fisheyeRepo, result.get(0));
    }

}
