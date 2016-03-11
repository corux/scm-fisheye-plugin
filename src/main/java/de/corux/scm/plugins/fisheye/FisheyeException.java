package de.corux.scm.plugins.fisheye;

/**
 * The exception used by the fisheye plugin.
 */
public class FisheyeException extends RuntimeException
{
    private static final long serialVersionUID = -7761481542822712735L;

    /**
     * Instantiates a new fisheye exception.
     *
     * @param cause
     *            the cause
     */
    public FisheyeException(Throwable cause)
    {
        super(cause);
    }
}
