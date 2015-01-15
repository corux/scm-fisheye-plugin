package de.corux.scm.plugins.fisheye.client;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

/**
 * The git repository details dto.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class GitRepository
{
    private String location;

    public String getLocation()
    {
        return location;
    }

    public void setLocation(String location)
    {
        this.location = location;
    }
}
