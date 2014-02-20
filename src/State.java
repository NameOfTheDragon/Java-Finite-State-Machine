import java.util.ArrayList;
import java.util.List;

/**
 * Created by Tim on 20/02/14.
 */
public class State
{

    /**
     * Gets the descriptive name of the current state.
     */
    private final String name;
    private final List<StateMachine.StateTransition> stateTransitions = new ArrayList<StateMachine.StateTransition>();

    public String getName() { return name; }

    protected StateTransitionAction onEnter = new StateTransitionAction()
    {
        @Override
        public void action(){}
    };
    protected StateTransitionAction onExit = new StateTransitionAction()
    {
        @Override
        public void action(){}
    };

    public State(String name)
    {
        this.name = name;
    }

    public void addTransition(StateMachine.StateTransition transition)
    {
        this.stateTransitions.add(transition);
    }
}

