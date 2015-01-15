package de.corux.scm.plugins.fisheye;

import java.util.ArrayList;
import java.util.List;

import sonia.scm.repository.Repository;
import sonia.scm.store.Store;
import sonia.scm.store.StoreFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Handles the saving and retrieving of the fisheye configuration.
 */
@Singleton
public final class FisheyeContext
{
    /** Store name for the {@link FisheyeGlobalConfiguration}. */
    private static final String STORE_NAME = "fisheye";

    /** Store for the {@link FisheyeGlobalConfiguration}. */
    private final Store<FisheyeGlobalConfiguration> store;

    /** Global configuration. */
    private FisheyeGlobalConfiguration globalConfiguration;

    /**
     * Constructor.
     *
     * @param storeFactory
     *            the store factory
     */
    @Inject
    public FisheyeContext(final StoreFactory storeFactory)
    {
        this.store = storeFactory.getStore(FisheyeGlobalConfiguration.class, STORE_NAME);
        globalConfiguration = store.get();

        if (globalConfiguration == null)
        {
            globalConfiguration = new FisheyeGlobalConfiguration();
        }
    }

    /**
     * Gets the configuration for the given repository.
     *
     * @param repository
     *            the repository
     * @return the configuration
     */
    public FisheyeConfiguration getConfiguration(final Repository repository)
    {
        FisheyeConfiguration configuration = new FisheyeConfiguration(repository);
        return configuration;
    }

    public FisheyeGlobalConfiguration getGlobalConfiguration()
    {
        return globalConfiguration;
    }

    public void setGlobalConfiguration(final FisheyeGlobalConfiguration globalConfiguration)
    {
        this.globalConfiguration = globalConfiguration;
        store.set(globalConfiguration);
    }

    /**
     * Gets the fisheye repositories.
     *
     * @param repository
     *            the repository
     * @return the fisheye repositories
     */
    public List<String> getFisheyeRepositories(final Repository repository)
    {
        FisheyeConfiguration configuration = getConfiguration(repository);
        List<String> repositories = new ArrayList<String>();
        repositories.addAll(configuration.getRepositories());

        if (globalConfiguration.useRepositoryNameAsDefault() && repositories.isEmpty())
        {
            repositories.add(repository.getName());
        }

        return repositories;
    }
}
