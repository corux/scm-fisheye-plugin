package de.corux.scm.plugins.fisheye.client;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

/**
 * The hg repository details dto.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class HgRepository
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
