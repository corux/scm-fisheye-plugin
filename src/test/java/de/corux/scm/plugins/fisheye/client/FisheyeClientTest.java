package de.corux.scm.plugins.fisheye.client;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.ObjectUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.Matchers;

import sonia.scm.ArgumentIsInvalidException;
import sonia.scm.net.HttpClient;
import sonia.scm.net.HttpRequest;
import sonia.scm.net.HttpResponse;
import de.corux.scm.plugins.fisheye.FisheyeContext;
import de.corux.scm.plugins.fisheye.FisheyeGlobalConfiguration;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class FisheyeClientTest
{
    private HttpClient httpClient;
    private FisheyeClient fisheyeClient;
    private final String token = "TestToken";
    private final String url = "http://test.url/";
    private HttpRequest expectedRequest;

    public void initMockFisheyeClient(boolean useToken) throws IOException
    {
        FisheyeContext context = mock(FisheyeContext.class);
        httpClient = mock(HttpClient.class);
        FisheyeGlobalConfiguration configuration = mock(FisheyeGlobalConfiguration.class);
        when(context.getGlobalConfiguration()).thenReturn(configuration);
        when(configuration.getUrlParsed()).thenReturn(new URL(url));
        when(configuration.getApiToken()).thenReturn(token);

        fisheyeClient = new FisheyeClient(context, httpClient);

        expectedRequest = new HttpRequest(url);

        if (useToken)
        {
            expectedRequest.addHeader("X-Api-Key", token);
        }
        else
        {
            expectedRequest.setBasicAuthentication("test-username", "test-password");
            fisheyeClient.SetCredentials("test-username", "test-password");
        }
    }

    @Test
    public void testIndexRepositoryValidStatusCode() throws IOException
    {
        // arrange
        initMockFisheyeClient(true);
        String repository = "TestRepo";
        HttpResponse response = mock(HttpResponse.class);
        when(httpClient.post(Matchers.any(HttpRequest.class))).thenReturn(response);
        when(response.getStatusCode()).thenReturn(200);

        // act
        boolean result = fisheyeClient.indexRepository(repository);

        // assert
        assertTrue(result);
        verify(httpClient, times(1)).post(Matchers.argThat(new CheckRequestHeadersArgumentMatcher(expectedRequest)));
    }

    @Test
    public void testIndexRepositoryInvalidStatusCode() throws IOException
    {
        // arrange
        initMockFisheyeClient(true);
        String repository = "TestRepo";
        HttpResponse response = mock(HttpResponse.class);
        when(httpClient.post(Matchers.any(HttpRequest.class))).thenReturn(response);
        when(response.getStatusCode()).thenReturn(401);

        // act
        boolean result = fisheyeClient.indexRepository(repository);

        // assert
        verify(httpClient, times(1)).post(Matchers.argThat(new CheckRequestHeadersArgumentMatcher(expectedRequest)));
        assertFalse(result);
    }

    @Test(expected = ArgumentIsInvalidException.class)
    public void testListRepositoriesUsernameRequired() throws IOException
    {
        // arrange
        initMockFisheyeClient(false);
        fisheyeClient.SetCredentials(null, "password");

        // act
        fisheyeClient.listRepositories();
    }

    @Test(expected = ArgumentIsInvalidException.class)
    public void testListRepositoriesPasswordRequired() throws IOException
    {
        // arrange
        initMockFisheyeClient(false);
        fisheyeClient.SetCredentials("username", null);

        // act
        fisheyeClient.listRepositories();
    }

    @Test
    public void testListRepositoriesInvalidStatusCode() throws IOException
    {
        // arrange
        initMockFisheyeClient(false);
        HttpResponse response = mock(HttpResponse.class);
        when(httpClient.get(Matchers.any(HttpRequest.class))).thenReturn(response);
        when(response.getStatusCode()).thenReturn(401);

        // act
        List<Repository> result = fisheyeClient.listRepositories();

        // assert
        assertTrue(result.isEmpty());
    }

    @Test
    public void testListRepositoriesEmpty() throws IOException
    {
        runListRepositoriesTest(0, 1);
    }

    @Test
    public void testListRepositoriesOneRepo() throws IOException
    {
        runListRepositoriesTest(1, 1);
    }

    @Test
    public void testListRepositoriesMultiplePages() throws IOException
    {
        runListRepositoriesTest(330, 3);
    }

    public void runListRepositoriesTest(int numRepositories, int numHttpCalls) throws IOException
    {
        List<Repository> list = new ArrayList<Repository>();
        for (int i = 0; i < numRepositories; i++)
        {
            Repository repo = new Repository();
            repo.setDescription("Desc");
            repo.setEnabled(true);
            repo.setName("Name");
            int type = i % 3;
            GitRepository git = new GitRepository();
            HgRepository hg = new HgRepository();
            SvnRepository svn = new SvnRepository();
            switch (type)
            {
            case 0:
                repo.setType("git");
                repo.setGit(git);
                break;
            case 1:
                repo.setType("hg");
                repo.setHg(hg);
                break;
            case 2:
                repo.setType("svn");
                repo.setSvn(svn);
                break;
            }
            list.add(repo);
        }

        runListRepositoriesTest(list, numHttpCalls);
    }

    private void runListRepositoriesTest(List<Repository> repositories, int numHttpCalls) throws IOException
    {
        // arrange
        initMockFisheyeClient(false);
        ByteArrayInputStream[] jsonStreams = new ByteArrayInputStream[numHttpCalls];

        ObjectMapper mapper = new ObjectMapper();
        int itemsPerCall = repositories.size() / numHttpCalls;
        for (int i = 0; i < numHttpCalls; i++)
        {
            PagedList<Repository> list = new PagedList<Repository>();
            list.setLastPage(i == numHttpCalls - 1);
            list.setValues(repositories.subList(i * itemsPerCall, (i + 1) * itemsPerCall));
            jsonStreams[i] = new ByteArrayInputStream(mapper.writeValueAsBytes(list));
        }

        HttpResponse response = mock(HttpResponse.class);
        when(httpClient.get(Matchers.any(HttpRequest.class))).thenReturn(response);
        when(response.getStatusCode()).thenReturn(200);
        when(response.getContent()).thenReturn(jsonStreams[0], Arrays.copyOfRange(jsonStreams, 1, numHttpCalls));

        // act
        List<Repository> result = fisheyeClient.listRepositories();

        // assert
        assertEquals(repositories.size(), result.size());
        for (int i = 0; i < repositories.size(); i++)
        {
            areEqual(repositories.get(i), result.get(i));
        }
        verify(httpClient, times(numHttpCalls)).get(
                Matchers.argThat(new CheckRequestHeadersArgumentMatcher(expectedRequest)));
    }

    private boolean areEqual(Repository o1, Repository o2)
    {
        return o1.isEnabled() == o2.isEnabled() && ObjectUtils.equals(o1.getDescription(), o2.getDescription())
                && ObjectUtils.equals(o1.getName(), o2.getName()) && ObjectUtils.equals(o1.getType(), o2.getType())
                && areEqual(o1.getGit(), o2.getGit()) && areEqual(o1.getHg(), o2.getHg())
                && areEqual(o1.getSvn(), o2.getSvn());
    }

    private boolean areEqual(GitRepository o1, GitRepository o2)
    {
        if (o1 == o2)
        {
            return true;
        }
        if ((o1 == null) || (o2 == null))
        {
            return false;
        }

        return ObjectUtils.equals(o1.getLocation(), o2.getLocation());
    }

    private boolean areEqual(HgRepository o1, HgRepository o2)
    {
        if (o1 == o2)
        {
            return true;
        }
        if ((o1 == null) || (o2 == null))
        {
            return false;
        }

        return ObjectUtils.equals(o1.getLocation(), o2.getLocation());
    }

    private boolean areEqual(SvnRepository o1, SvnRepository o2)
    {
        if (o1 == o2)
        {
            return true;
        }
        if ((o1 == null) || (o2 == null))
        {
            return false;
        }

        return ObjectUtils.equals(o1.getPath(), o2.getPath()) && ObjectUtils.equals(o1.getUrl(), o2.getUrl());
    }

    private class CheckRequestHeadersArgumentMatcher extends ArgumentMatcher<HttpRequest>
    {
        HttpRequest thisObject;

        public CheckRequestHeadersArgumentMatcher(HttpRequest thisObject)
        {
            this.thisObject = thisObject;
        }

        @Override
        public boolean matches(Object argument)
        {
            if (!(argument instanceof HttpRequest))
            {
                return false;
            }

            HttpRequest other = (HttpRequest) argument;

            // check basic auth
            if (!ObjectUtils.equals(thisObject.getUsername(), other.getUsername())
                    || !ObjectUtils.equals(thisObject.getPassword(), other.getPassword()))
            {
                return false;
            }

            // check headers
            if (thisObject.getHeaders() != null)
            {
                for (String header : thisObject.getHeaders().keySet())
                {
                    if (!other.getHeaders().containsKey(header)
                            || !areHeaderEqual(thisObject.getHeaders().get(header), other.getHeaders().get(header)))
                    {
                        return false;
                    }
                }
            }

            return true;
        }

        private boolean areHeaderEqual(List<String> a, List<String> b)
        {
            if (a.size() != b.size())
            {
                return false;
            }

            for (int i = 0; i < a.size(); i++)
            {
                if (a.get(i) != b.get(i))
                {
                    return false;
                }
            }

            return true;
        }
    }
}
