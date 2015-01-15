package de.corux.scm.plugins.fisheye.client;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.HttpClientBuilder;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.JavaType;

import sonia.scm.ArgumentIsInvalidException;
import de.corux.scm.plugins.fisheye.FisheyeContext;
import de.corux.scm.plugins.fisheye.FisheyeGlobalConfiguration;

/**
 * Simple Fisheye HTTP Client.
 * 
 * @see <a
 *      href="https://docs.atlassian.com/fisheye-crucible/latest/wadl/fecru.html">
 *      https://docs.atlassian.com/fisheye-crucible/latest/wadl/fecru.html</a>
 */
public class FisheyeClient
{
    private final URL baseUrl;
    private final String apiToken;
    private String username;
    private String password;
    private final HttpClient client;

    @Inject
    public FisheyeClient(final FisheyeContext context)
    {
        this(context.getGlobalConfiguration());
    }

    public FisheyeClient(final FisheyeGlobalConfiguration config)
    {
        this(config.getUrlParsed(), config.getApiToken(), null, null);
    }

    /**
     * Instantiates a new fisheye client.
     *
     * @param baseUrl
     *            the base url of the fisheye server.
     * @param apiToken
     *            the api token to authenticate to the fisheye api.
     * @param username
     *            the username to authenticate to the fisheye api.
     * @param password
     *            the password to authenticate to the fisheye api.
     */
    public FisheyeClient(final URL baseUrl, final String apiToken, final String username, final String password)
    {
        this.baseUrl = baseUrl;
        this.apiToken = apiToken;
        this.username = username;
        this.password = password;
        this.client = HttpClientBuilder.create().build();
    }

    /**
     * Sets the REST api credentials.
     *
     * @param username
     *            the username
     * @param password
     *            the password
     */
    public void SetCredentials(final String username, final String password)
    {
        this.username = username;
        this.password = password;
    }

    /**
     * Adds the request headers to the given request.
     *
     * @param request
     *            the request
     * @param useApiToken
     *            if <code>true</code>, the {@link #apiToken} will be used for
     *            authentication; otherwise the {@link #username} and
     *            {@link #password} are used.
     * @throws AuthenticationException
     *             the authentication exception
     */
    private void addHeaders(final HttpRequestBase request, final boolean useApiToken) throws AuthenticationException
    {
        request.addHeader("content-type", "application/json");
        request.addHeader("accept", "application/json");

        if (useApiToken)
        {
            request.addHeader("X-Api-Key", apiToken);
        }
        else
        {
            UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(username, password);
            Header authenticateHeader = new BasicScheme().authenticate(credentials, request, null);
            request.addHeader(authenticateHeader);
        }
    }

    /**
     * Performs an incremental index on the given repository.
     *
     * @param repository
     *            the repository
     * @return true, if successful
     */
    public boolean indexRepository(final String repository)
    {
        try
        {
            URL url = new URL(baseUrl, "/rest-service-fecru/admin/repositories/" + repository + "/incremental-index");

            // request
            HttpPut request = new HttpPut(url.toURI());
            addHeaders(request, true);

            // response
            HttpResponse response = client.execute(request);
            int statusCode = response.getStatusLine().getStatusCode();

            return statusCode == HttpStatus.SC_ACCEPTED || statusCode == HttpStatus.SC_NO_CONTENT;
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }

    }

    /**
     * Lists all available repositories.
     *
     * @return the list of repositories.
     */
    @SuppressWarnings("unchecked")
    public List<Repository> listRepositories()
    {
        if (this.username == null)
        {
            throw new ArgumentIsInvalidException("username");
        }
        if (this.password == null)
        {
            throw new ArgumentIsInvalidException("password");
        }
        try
        {
            int start = 0;
            int limit = 1000;
            PagedList<Repository> list;
            List<Repository> result = new ArrayList<Repository>();
            do
            {
                URL url = new URL(baseUrl, String.format("/rest-service-fecru/admin/repositories?start=%s&limit=%s",
                        start, limit));

                // request
                HttpGet request = new HttpGet(url.toURI());
                addHeaders(request, false);

                // response
                HttpResponse response = client.execute(request);
                InputStream content = response.getEntity().getContent();

                ObjectMapper mapper = new ObjectMapper();
                JavaType type = mapper.getTypeFactory().constructParametricType(PagedList.class, Repository.class);
                list = (PagedList<Repository>) mapper.readValue(content, type);

                // read response
                result.addAll(list.getValues());
                start = list.getStart() + list.getSize();
            } while (!list.isLastPage());

            return result;
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }
}
