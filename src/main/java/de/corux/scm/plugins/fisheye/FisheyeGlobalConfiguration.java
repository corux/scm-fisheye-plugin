package de.corux.scm.plugins.fisheye;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import sonia.scm.Validateable;
import sonia.scm.util.Util;

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
