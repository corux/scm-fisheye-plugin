package de.corux.scm.plugins.fisheye.client;

import java.util.Iterator;
import java.util.List;

/**
 * The paged list dto.
 *
 * @param <T>
 *            the type of the paged values.
 */
public class PagedList<T> implements Iterable<T>
{
    private int start;
    private int size;
    private int limit;
    private boolean lastPage;
    private List<T> values;

    @Override
    public Iterator<T> iterator()
    {
        return getValues().iterator();
    }

    public int getStart()
    {
        return start;
    }

    public void setStart(int start)
    {
        this.start = start;
    }

    public int getSize()
    {
        return size;
    }

    public void setSize(int size)
    {
        this.size = size;
    }

    public int getLimit()
    {
        return limit;
    }

    public void setLimit(int limit)
    {
        this.limit = limit;
    }

    public boolean isLastPage()
    {
        return lastPage;
    }

    public void setLastPage(boolean lastPage)
    {
        this.lastPage = lastPage;
    }

    public List<T> getValues()
    {
        return values;
    }

    public void setValues(List<T> values)
    {
        this.values = values;
    }
}
