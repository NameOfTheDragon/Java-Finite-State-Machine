package uk.co.tigranetworks;

import java.util.EventListener;

public interface TransitionRule extends EventListener
{
    public boolean transitionIsAllowed();
}
