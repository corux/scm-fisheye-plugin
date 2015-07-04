package de.corux.scm.plugins.fisheye.client;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.ObjectUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;
import org.mockito.Matchers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.corux.scm.plugins.fisheye.FisheyeContext;
import de.corux.scm.plugins.fisheye.FisheyeGlobalConfiguration;
import sonia.scm.ArgumentIsInvalidException;
import sonia.scm.net.ahc.AdvancedHttpClient;
import sonia.scm.net.ahc.AdvancedHttpResponse;
import sonia.scm.net.ahc.BaseHttpRequest;

public class FisheyeClientTest
{
    private TestAdvancedHttpClient httpClient;
    private FisheyeClient fisheyeClient;
    private final String token = "TestToken";
    private final String url = "http://test.url/fecru";
    private TestAdvancedHttpRequest expectedRequest;

    private abstract class TestAdvancedHttpClient extends AdvancedHttpClient
    {
        private AdvancedHttpResponse response;
        private BaseHttpRequest<?> request;

        public void setResponse(final AdvancedHttpResponse response)
        {
            this.response = response;
        }

        public BaseHttpRequest<?> getRequest()
        {
            return request;
        }

        @Override
        protected AdvancedHttpResponse request(BaseHttpRequest<?> request) throws IOException
        {
            this.request = request;
            return response;
        }
    }

    private class TestAdvancedHttpRequest extends BaseHttpRequest<TestAdvancedHttpRequest>
    {
        public TestAdvancedHttpRequest(AdvancedHttpClient client, String method, String url)
        {
            super(client, method, url);
        }

        @Override
        protected TestAdvancedHttpRequest self()
        {
            return this;
        }
    }

    public void initMockFisheyeClient(boolean useToken) throws IOException
    {
        FisheyeContext context = mock(FisheyeContext.class);
        httpClient = mock(TestAdvancedHttpClient.class);
        doCallRealMethod().when(httpClient).put(Matchers.anyString());
        doCallRealMethod().when(httpClient).get(Matchers.anyString());
        doCallRealMethod().when(httpClient).request(Matchers.any(BaseHttpRequest.class));
        doCallRealMethod().when(httpClient).setResponse(Matchers.any(AdvancedHttpResponse.class));
        doCallRealMethod().when(httpClient).getRequest();

        FisheyeGlobalConfiguration configuration = mock(FisheyeGlobalConfiguration.class);
        when(context.getGlobalConfiguration()).thenReturn(configuration);
        when(configuration.getUrl()).thenReturn(url);
        when(configuration.getApiToken()).thenReturn(token);

        fisheyeClient = new FisheyeClient(context, httpClient);
        expectedRequest = new TestAdvancedHttpRequest(httpClient, null, url);
        if (useToken)
        {
            expectedRequest.header("X-Api-Key", token);
        }
        else
        {
            expectedRequest.basicAuth("test-username", "test-password");
            fisheyeClient.SetCredentials("test-username", "test-password");
        }
    }

    @Test
    public void testIndexRepositoryValidStatusCode() throws IOException
    {
        // arrange
        initMockFisheyeClient(true);
        AdvancedHttpResponse response = mock(AdvancedHttpResponse.class);
        when(response.getStatus()).thenReturn(202, 204);
        httpClient.setResponse(response);

        // assert status code 202
        boolean result202 = fisheyeClient.indexRepository("TestRepo");
        assertTrue(result202);
        assertTrue(areEqual(expectedRequest, httpClient.getRequest()));

        // assert status code 204
        boolean result204 = fisheyeClient.indexRepository("TestRepo");
        assertTrue(result204);
        assertTrue(areEqual(expectedRequest, httpClient.getRequest()));
    }

    @Test
    public void testIndexRepositoryInvalidStatusCode() throws IOException
    {
        // arrange
        initMockFisheyeClient(true);
        AdvancedHttpResponse response = mock(AdvancedHttpResponse.class);
        when(response.getStatus()).thenReturn(401);
        httpClient.setResponse(response);

        // assert failed status code
        boolean result = fisheyeClient.indexRepository("TestRepo");
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

    @Test(expected = IOException.class)
    public void testListRepositoriesInvalidStatusCode() throws IOException
    {
        // arrange
        initMockFisheyeClient(false);
        AdvancedHttpResponse response = mock(AdvancedHttpResponse.class);
        when(response.getStatus()).thenReturn(401);
        httpClient.setResponse(response);

        // act
        fisheyeClient.listRepositories();
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

        AdvancedHttpResponse response = mock(AdvancedHttpResponse.class);
        when(response.getStatus()).thenReturn(200);
        when(response.contentAsStream()).thenReturn(jsonStreams[0], Arrays.copyOfRange(jsonStreams, 1, numHttpCalls));
        httpClient.setResponse(response);

        // act
        List<Repository> result = fisheyeClient.listRepositories();

        // assert
        assertEquals(repositories.size(), result.size());
        for (int i = 0; i < repositories.size(); i++)
        {
            areEqual(repositories.get(i), result.get(i));
        }
        assertTrue(areEqual(expectedRequest, httpClient.getRequest()));
        verify(httpClient, times(numHttpCalls)).get(Matchers.anyString());
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

    private boolean areEqual(BaseHttpRequest<?> a, BaseHttpRequest<?> b)
    {
        if (!b.getUrl().startsWith(a.getUrl()))
        {
            return false;
        }

        // check headers
        if (a.getHeaders() != null)
        {
            for (String header : a.getHeaders().keySet())
            {
                if (!b.getHeaders().containsKey(header)
                        || !areHeaderEqual(a.getHeaders().get(header), b.getHeaders().get(header)))
                {
                    return false;
                }
            }
        }

        return true;
    }

    private boolean areHeaderEqual(Collection<String> a, Collection<String> b)
    {
        if (a.size() != b.size())
        {
            return false;
        }

        Iterator<String> aIterator = a.iterator();
        Iterator<String> bIterator = b.iterator();
        while (aIterator.hasNext())
        {
            if (!aIterator.next().equals(bIterator.next()))
            {
                return false;
            }
        }

        return true;
    }
}
