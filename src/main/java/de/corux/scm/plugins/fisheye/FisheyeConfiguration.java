package de.corux.scm.plugins.fisheye;

import sonia.scm.Validateable;
import sonia.scm.repository.Repository;
import sonia.scm.util.Util;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;

/**
 * The repository configuration object.
 */
public class FisheyeConfiguration implements Validateable
{
    /** Repository property for the fisheye server url */
    public static final String PROPERTY_FISHEYE_URL = "fisheye.url";

    /** Repository property for the api token. */
    public static final String PROPERTY_FISHEYE_APITOKEN = "fisheye.api-token";

    /**
     * Repository property for a comma seperated list of fisheye repository
     * names.
     */
    public static final String PROPERTY_FISHEYE_REPOSITORIES = "fisheye.repositories";

    /** The fisheye api token. */
    private String apiToken;

    /** The fisheye repositories, linked with this scm repository. */
    private List<String> repositories;

    /** The fisheye url. */
    private String url;

    private Repository repository;

    /**
     * Instantiates a new fisheye configuration.
     *
     * @param url
     *            the url
     * @param apiToken
     *            the api token
     * @param repositories
     *            the repositories
     */
    public FisheyeConfiguration(final String url, final String apiToken, final List<String> repositories)
    {
        this.url = url;
        this.apiToken = apiToken;
        this.repositories = repositories;
    }

    /**
     * Instantiates a new fisheye configuration. This constructor reads the
     * properties from the repository and stores it in the configuration object.
     *
     * @param repository
     *            the repository to read the properties from.
     */
    public FisheyeConfiguration(final Repository repository)
    {
        this.repository = repository;
        this.url = repository.getProperty(PROPERTY_FISHEYE_URL);
        this.apiToken = repository.getProperty(PROPERTY_FISHEYE_APITOKEN);

        List<String> list = new ArrayList<String>();
        String repoString = repository.getProperty(PROPERTY_FISHEYE_REPOSITORIES);
        if (Util.isNotEmpty(repoString))
        {
            for (String repo : repoString.split(","))
            {
                list.add(repo.trim());
            }
        }
        this.repositories = Collections.unmodifiableList(list);
    }

    /**
     * Returns the api token which is used for authentication when requesting a
     * fisheye repository scan.
     *
     * @return fisheye api token
     */
    public String getApiToken()
    {
        return apiToken;
    }

    public void setApiToken(final String apiToken)
    {
        this.apiToken = apiToken;
        repository.setProperty(PROPERTY_FISHEYE_APITOKEN, apiToken);
    }

    /**
     * Gets the fisheye repositories.
     *
     * @return the fisheye repositories
     */
    public List<String> getRepositories()
    {
        return repositories;
    }

    /**
     * Sets the list of fisheye repositories.
     *
     * @param repositories
     *            the new repositories
     */
    public void setRepositories(final List<String> repositories)
    {
        this.repositories = repositories;
        String repoValues = StringUtils.join(repositories, ',');
        repository.setProperty(PROPERTY_FISHEYE_REPOSITORIES, repoValues);
    }

    /**
     * Sets the url.
     *
     * @param url
     *            the new url
     */
    public void setUrl(final String url)
    {
        this.url = url;
        repository.setProperty(PROPERTY_FISHEYE_URL, url);
    }

    /**
     * Return the url of the fisheye server.
     *
     * @return url of the fisheye server
     */
    public String getUrl()
    {
        return url;
    }

    /**
     * Gets the url parsed as {@link URL}.
     *
     * @return the parsed url
     */
    public URL getUrlParsed()
    {
        try
        {
            return new URL(getUrl());
        }
        catch (MalformedURLException e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * Return true, if the configuration is valid.
     *
     * @return true, if the configuration is valid
     */
    @Override
    public boolean isValid()
    {
        return Util.isNotEmpty(url) && Util.isNotEmpty(apiToken);
    }
}
