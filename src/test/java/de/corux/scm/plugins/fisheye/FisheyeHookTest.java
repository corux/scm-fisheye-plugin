package de.corux.scm.plugins.fisheye;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Provider;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;

import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryHookEvent;
import de.corux.scm.plugins.fisheye.client.FisheyeClient;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class FisheyeHookTest
{
    private FisheyeContext context;
    private FisheyeClient fisheyeClient;
    private FisheyeHook fisheyeHook;
    private Repository repository;
    private RepositoryHookEvent hookEvent;

    @Before
    public void initFisheyeHook() throws IOException
    {
        context = mock(FisheyeContext.class);
        FisheyeGlobalConfiguration configuration = mock(FisheyeGlobalConfiguration.class);
        when(context.getGlobalConfiguration()).thenReturn(configuration);

        @SuppressWarnings("unchecked")
        Provider<FisheyeClient> fisheyeClientProvider = mock(Provider.class);
        fisheyeClient = mock(FisheyeClient.class);
        when(fisheyeClientProvider.get()).thenReturn(fisheyeClient);

        fisheyeHook = new FisheyeHook(fisheyeClientProvider, context);

        repository = mock(Repository.class);
        hookEvent = mock(RepositoryHookEvent.class);
        when(hookEvent.getRepository()).thenReturn(repository);
    }

    @Test
    public void testOnEventNoRepositoryInEvent() throws IOException
    {
        // arrange
        RepositoryHookEvent hookEventWithoutRepository = mock(RepositoryHookEvent.class);

        // act
        fisheyeHook.onEvent(hookEventWithoutRepository);

        // assert
        verify(context, times(0)).getFisheyeRepositories(Matchers.any(Repository.class));
        verify(fisheyeClient, times(0)).indexRepository(Matchers.anyString());
    }

    @Test
    public void testOnEventRepositoryNotLinkedToFisheye() throws IOException
    {
        // arrange
        when(context.getFisheyeRepositories(Matchers.any(Repository.class))).thenReturn(new ArrayList<String>());

        // act
        fisheyeHook.onEvent(hookEvent);

        // assert
        verify(context, times(1)).getFisheyeRepositories(Matchers.eq(repository));
        verify(fisheyeClient, times(0)).indexRepository(Matchers.anyString());
    }

    @Test
    public void testOnEventRepositoryLinkedToMultipleFisheyeRepos() throws IOException
    {
        // arrange
        List<String> fisheyeRepos = new ArrayList<String>();
        fisheyeRepos.add("repo 1");
        fisheyeRepos.add("repo 2");
        fisheyeRepos.add("repo 3");
        when(context.getFisheyeRepositories(Matchers.any(Repository.class))).thenReturn(fisheyeRepos);
        
        // when indexRepository for "repo 2" does not succeed, all others should still get executed
        when(fisheyeClient.indexRepository(Matchers.anyString())).thenReturn(true, false, true);

        // act
        fisheyeHook.onEvent(hookEvent);

        // assert
        verify(fisheyeClient, times(fisheyeRepos.size())).indexRepository(Matchers.anyString());
        for (String repo : fisheyeRepos)
        {
            verify(fisheyeClient, times(1)).indexRepository(Matchers.eq(repo));
        }
    }
}
