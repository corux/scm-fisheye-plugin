package de.corux.scm.plugins.fisheye;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.plugin.ext.Extension;
import sonia.scm.repository.PostReceiveRepositoryHook;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryHookEvent;
import de.corux.scm.plugins.fisheye.client.FisheyeClient;

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
            List<String> repositories = context.getFisheyeRepositories(repository);
            for (String fisheyeRepo : repositories)
            {
                String commonMsg = String.format("SCM repository: %s, Fisheye repository: %s", repository.getName(),
                        fisheyeRepo);
                String errorMsg = "Failed to execute fisheye hook. " + commonMsg;
                if (logger.isDebugEnabled())
                {
                    logger.debug("Executing fisheye hook. " + commonMsg);
                }
                try
                {
                    boolean result = client.indexRepository(fisheyeRepo);
                    if (!result && logger.isErrorEnabled())
                    {
                        logger.error(errorMsg);
                    }
                    else if (result && logger.isDebugEnabled())
                    {
                        logger.debug("Successfully executed fisheye hook. " + commonMsg);
                    }

                }
                catch (Exception e)
                {
                    if (logger.isErrorEnabled())
                    {
                        logger.error(errorMsg, e);
                    }
                }
            }
        }
        else if (logger.isErrorEnabled())
        {
            logger.error("received hook without repository");
        }
    }
}
