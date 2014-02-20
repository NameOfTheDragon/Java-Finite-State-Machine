import java.util.EventListener;

public interface TriggerRule extends EventListener
{
    public boolean transitionIsAllowed();

}
