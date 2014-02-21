package uk.co.tigranetworks;

import java.util.EventListener;

/**
 * Created by Tim on 21/02/14.
 */
public interface TraceListener extends EventListener
{
    public void trace(String traceOutput);
}
