package de.corux.scm.plugins.fisheye.client;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

/**
 * The svn repository details dto.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SvnRepository
{
    /** The url of repository. */
    private String url;

    /** The path in the repository, that contains the sources. */
    private String path;

    public String getUrl()
    {
        return url;
    }

    public void setUrl(String url)
    {
        this.url = url;
    }

    public String getPath()
    {
        return path;
    }

    public void setPath(String path)
    {
        this.path = path;
    }
}
