package de.corux.scm.plugins.fisheye;

import com.google.inject.AbstractModule;

import de.corux.scm.plugins.fisheye.client.FisheyeClient;
import sonia.scm.plugin.ext.Extension;

@Extension
public class FisheyeModule extends AbstractModule
{
    @Override
    protected void configure()
    {
        bind(FisheyeContext.class);
        bind(FisheyeClient.class);
        bind(RepositoryLinker.class);
    }
}
