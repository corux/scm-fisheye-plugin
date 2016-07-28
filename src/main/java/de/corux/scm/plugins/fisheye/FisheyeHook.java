package de.corux.scm.plugins.fisheye;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.corux.scm.plugins.fisheye.client.FisheyeClient;
import sonia.scm.plugin.ext.Extension;
import sonia.scm.repository.PostReceiveRepositoryHook;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryHookEvent;

/**
 * The commit hook to send the fisheye scan request.
 */
@Extension
public class FisheyeHook extends PostReceiveRepositoryHook
{
    /**
     * The logger for {@link FisheyeHook}.
     */
    private static final Logger logger = LoggerFactory.getLogger(FisheyeHook.class);

    private final Provider<FisheyeClient> clientProvider;
    private final FisheyeContext context;

    @Inject
    public FisheyeHook(final Provider<FisheyeClient> clientProvider, final FisheyeContext context)
    {
        this.clientProvider = clientProvider;
        this.context = context;
    }

    @Override
    public void onEvent(final RepositoryHookEvent event)
    {
        Repository repository = event.getRepository();
        FisheyeClient client = clientProvider.get();

        if (repository != null)
        {
            client.updateConfigFromRepository(context.getConfiguration(repository));
            List<String> repositories = context.getFisheyeRepositories(repository);
            for (String fisheyeRepo : repositories)
            {
                String commonMsg = String.format("SCM repository: %s, Fisheye repository: %s", repository.getName(),
                        fisheyeRepo);
                String errorMsg = "Failed to execute fisheye hook. " + commonMsg;
                logger.debug("Executing fisheye hook. {}", commonMsg);
                try
                {
                    boolean result = client.indexRepository(fisheyeRepo);
                    if (result)
                    {
                        logger.info("Successfully executed fisheye hook. {}", commonMsg);
                    }
                    else
                    {
                        logger.error(errorMsg);
                    }

                }
                catch (Exception e)
                {
                    logger.error(errorMsg, e);
                }
            }
        }
        else
        {
            logger.error("received hook without repository");
        }
    }
}
