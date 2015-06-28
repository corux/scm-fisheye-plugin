package de.corux.scm.plugins.fisheye.client;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.JavaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.corux.scm.plugins.fisheye.FisheyeContext;
import de.corux.scm.plugins.fisheye.FisheyeGlobalConfiguration;
import sonia.scm.ArgumentIsInvalidException;
import sonia.scm.net.ahc.AdvancedHttpClient;
import sonia.scm.net.ahc.AdvancedHttpRequest;
import sonia.scm.net.ahc.AdvancedHttpRequestWithBody;
import sonia.scm.net.ahc.AdvancedHttpResponse;
import sonia.scm.net.ahc.BaseHttpRequest;
import sonia.scm.util.UrlBuilder;

/**
 * Simple Fisheye HTTP Client.
 * 
 * @see <a href=
 *      "https://docs.atlassian.com/fisheye-crucible/latest/wadl/fecru.html">
 *      https://docs.atlassian.com/fisheye-crucible/latest/wadl/fecru.html</a>
 */
public class FisheyeClient
{
    private final String baseUrl;
    private final String apiToken;
    private String username;
    private String password;
    private final AdvancedHttpClient client;

    /**
     * The logger for {@link FisheyeClient}.
     */
    private static final Logger logger = LoggerFactory.getLogger(FisheyeClient.class);

    @Inject
    public FisheyeClient(final FisheyeContext context, final AdvancedHttpClient client)
    {
        FisheyeGlobalConfiguration configuration = context.getGlobalConfiguration();
        this.client = client;
        this.baseUrl = configuration.getUrl();
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
     * Adds common request headers to the given request.
     *
     * @param request
     *            the request
     * @param useApiToken
     *            if <code>true</code>, the {@link #apiToken} will be used for
     *            authentication; otherwise the {@link #username} and
     *            {@link #password} are used.
     */
    private void addHeaders(final BaseHttpRequest<?> request, final boolean useApiToken)
    {
        request.header("Accept", "application/json");

        if (useApiToken)
        {
            request.header("X-Api-Key", apiToken);
        }
        else
        {
            request.basicAuth(username, password);
        }
    }

    /**
     * {@inheritDoc FisheyeClient#addHeaders(BaseHttpRequest, boolean)}
     * 
     * @param request
     * @param useApiToken
     */
    private void addHeaders(final AdvancedHttpRequestWithBody request, final boolean useApiToken)
    {
        request.contentType("application/json");
        addHeaders((BaseHttpRequest<AdvancedHttpRequestWithBody>) request, useApiToken);
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
        // request
        String url = new UrlBuilder(baseUrl)
                .append(String.format("/rest-service-fecru/admin/repositories/%s/incremental-index", repository))
                .toString();
        AdvancedHttpRequestWithBody request = client.put(url);
        addHeaders(request, true);

        // response
        AdvancedHttpResponse response = request.request();
        int statusCode = response.getStatus();

        if (statusCode == 202 || statusCode == 204)
        {
            return true;
        }
        else
        {
            logger.warn("Fisheye hook failed with statusCode {}", statusCode);
            return false;
        }
    }

    /**
     * Lists all available repositories.
     *
     * @return the list of repositories.
     * @throws IOException
     */
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
        boolean isLastPage = false;
        List<Repository> result = new ArrayList<Repository>();
        do
        {
            String url = new UrlBuilder(baseUrl)
                    .append(String.format("/rest-service-fecru/admin/repositories?start=%s&limit=%s", start, limit))
                    .toString();

            // request
            AdvancedHttpRequest request = client.get(url);
            addHeaders(request, false);

            // response
            AdvancedHttpResponse response = request.request();
            int statusCode = response.getStatus();
            if (statusCode == 200)
            {
                InputStream content = response.contentAsStream();

                ObjectMapper mapper = new ObjectMapper();
                JavaType type = mapper.getTypeFactory().constructParametricType(PagedList.class, Repository.class);
                @SuppressWarnings("unchecked")
                PagedList<Repository> list = (PagedList<Repository>) mapper.readValue(content, type);

                // read response
                isLastPage = list.isLastPage();
                result.addAll(list.getValues());
                start = list.getStart() + list.getSize();
            }
            else
            {
                throw new RuntimeException("Listing fisheye repositories failed with statusCode " + statusCode);
            }
        } while (!isLastPage);

        return result;
    }
}
