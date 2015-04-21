package de.corux.scm.plugins.fisheye.client;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.JavaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.ArgumentIsInvalidException;
import sonia.scm.net.HttpClient;
import sonia.scm.net.HttpRequest;
import sonia.scm.net.HttpResponse;
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

    /**
     * The logger for {@link FisheyeClient}.
     */
    private static final Logger logger = LoggerFactory.getLogger(FisheyeClient.class);

    @Inject
    public FisheyeClient(final FisheyeContext context, final HttpClient client)
    {
        FisheyeGlobalConfiguration configuration = context.getGlobalConfiguration();
        this.client = client;
        this.baseUrl = configuration.getUrlParsed();
        this.apiToken = configuration.getApiToken();
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
    private void addHeaders(final HttpRequest request, final boolean useApiToken)
    {
        request.addHeader("Content-Type", "application/json");
        request.addHeader("Accept", "application/json");
        request.addParameters("", "");

        if (useApiToken)
        {
            request.addHeader("X-Api-Key", apiToken);
        }
        else
        {
            request.setBasicAuthentication(username, password);
        }
    }

    /**
     * Performs an incremental index on the given repository.
     *
     * @param repository
     *            the repository
     * @return true, if successful
     * @throws IOException
     */
    public boolean indexRepository(final String repository) throws IOException
    {
        // should be changed to new api call: PUT
        // /rest-service-fecru/admin/repositories/{name}/incremental-index
        URL url = new URL(baseUrl, "/rest-service-fecru/admin/repositories-v1/" + repository + "/scan");

        // request
        HttpRequest request = new HttpRequest(url.toString());
        addHeaders(request, true);

        // response
        HttpResponse response = client.post(request);
        int statusCode = response.getStatusCode();

        if (statusCode == 200)
        {
            return true;
        }
        else
        {
            if (logger.isWarnEnabled())
            {
                logger.warn("Fisheye hook failed with statusCode {}", statusCode);
            }
            return false;
        }
    }

    /**
     * Lists all available repositories.
     *
     * @return the list of repositories.
     * @throws IOException
     */
    @SuppressWarnings("unchecked")
    public List<Repository> listRepositories() throws IOException
    {
        if (this.username == null)
        {
            throw new ArgumentIsInvalidException("username");
        }
        if (this.password == null)
        {
            throw new ArgumentIsInvalidException("password");
        }

        int start = 0;
        int limit = 1000;
        PagedList<Repository> list;
        List<Repository> result = new ArrayList<Repository>();
        do
        {
            URL url = new URL(baseUrl, String.format("/rest-service-fecru/admin/repositories?start=%s&limit=%s", start,
                    limit));

            // request
            HttpRequest request = new HttpRequest(url.toString());
            addHeaders(request, false);

            // response
            HttpResponse response = client.get(request);
            int statusCode = response.getStatusCode();
            if (statusCode == 200)
            {
                InputStream content = response.getContent();

                ObjectMapper mapper = new ObjectMapper();
                JavaType type = mapper.getTypeFactory().constructParametricType(PagedList.class, Repository.class);
                list = (PagedList<Repository>) mapper.readValue(content, type);

                // read response
                result.addAll(list.getValues());
                start = list.getStart() + list.getSize();
            }
            else
            {
                throw new RuntimeException("Listing fisheye repositories failed with statusCode " + statusCode);
            }
        } while (!list.isLastPage());

        return result;
    }
}
