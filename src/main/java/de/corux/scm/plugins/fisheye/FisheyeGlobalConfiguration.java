package de.corux.scm.plugins.fisheye;

import java.net.MalformedURLException;
import java.net.URL;

import sonia.scm.Validateable;
import sonia.scm.util.Util;

//~--- JDK imports ------------------------------------------------------------

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang.StringUtils;

/**
 * The global configuration object.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "fisheye-config")
public class FisheyeGlobalConfiguration implements Validateable
{
    @XmlElement(name = "use-repository-name-as-default")
    private boolean useRepositoryNameAsDefault = false;

    private String url;

    @XmlElement(name = "api-token")
    private String apiToken;

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
        if (StringUtils.isEmpty(getUrl()))
        {
            return null;
        }

        try
        {
            return new URL(getUrl());
        }
        catch (MalformedURLException e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setUrl(final String url)
    {
        this.url = url;
    }

    public String getApiToken()
    {
        return apiToken;
    }

    public void setApiToken(final String apiToken)
    {
        this.apiToken = apiToken;
    }

    @Override
    public boolean isValid()
    {
        return Util.isNotEmpty(url);
    }

    public boolean useRepositoryNameAsDefault()
    {
        return useRepositoryNameAsDefault;
    }

    public void setUseRepositoryNameAsDefault(final boolean useRepositoryNameAsDefault)
    {
        this.useRepositoryNameAsDefault = useRepositoryNameAsDefault;
    }
}
