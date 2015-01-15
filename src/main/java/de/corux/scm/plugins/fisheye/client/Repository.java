package de.corux.scm.plugins.fisheye.client;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

/**
 * The repository dto.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Repository
{
    private String type;
    private String name;
    private String description;
    private boolean enabled;
    private GitRepository git;
    private HgRepository hg;
    private SvnRepository svn;

    public String getType()
    {
        return type;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public boolean isEnabled()
    {
        return enabled;
    }

    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }

    public GitRepository getGit()
    {
        return git;
    }

    public void setGit(GitRepository git)
    {
        this.git = git;
    }

    public HgRepository getHg()
    {
        return hg;
    }

    public void setHg(HgRepository hg)
    {
        this.hg = hg;
    }

    public SvnRepository getSvn()
    {
        return svn;
    }

    public void setSvn(SvnRepository svn)
    {
        this.svn = svn;
    }
}
