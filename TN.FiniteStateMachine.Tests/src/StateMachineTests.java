import org.junit.Test;

/**
 * Created by Tim on 20/02/14.
 */

/*
 ToDo

 Transitions should only be actioned if the state machine is in the correct state
 Initial state can only be set once
 OnExit should be called on leaving any state
 OnEnter should be called on entering any state
*/

public class StateMachineTests
{
    private int triggerVariable;

    @Test
    public void TriggerShouldReturnTrueByDefault()
    {
        final StateMachine machine = new StateMachine();
        final StateMachine.State stateTest = machine.new State("Unit test");
        final StateMachine.State.Transition transition = stateTest.new Transition(stateTest);
        assert transition.rule.transitionIsAllowed();
    }

    @Test
    public void TriggerShouldReturnFalseWhenOverridden()
    {
        final StateMachine machine = new StateMachine();
        final StateMachine.State state = machine.new State("Unit test");
        final StateMachine.State.Transition transition = state.new Transition(state, new TransitionRule()
        {
            @Override
            public boolean transitionIsAllowed()
            {
                return false;
            }
        });
        assert ! transition.rule.transitionIsAllowed();
    }

    @Test
    public void TransitionShouldBeAllowedWhenRuleCriteriaAreMet()
    {
        triggerVariable = 10;
        StateMachine machine = new StateMachine();
        final StateMachine.State initialState = machine.new State("Start");
        final StateMachine.State finalState = machine.new State("Unit test");
        StateMachine.State.Transition transition = initialState.new Transition(finalState, new TransitionRule()
        {
            @Override
            public boolean transitionIsAllowed()
            {
                final int triggerThreshold = 10;
                return (triggerVariable >= triggerThreshold);
            }
        });
        machine.start(initialState);
        transition.trigger();
        assert machine.getCurrentState() == finalState;
    }

    @Test
    public void TransitionShouldNotHappendWhenRuleCriteriaAreNotMet()
    {
        triggerVariable = 9;
        StateMachine machine = new StateMachine();
        final StateMachine.State initialState = machine.new State("Start");
        final StateMachine.State finalState = machine.new State("Unit test");
        StateMachine.State.Transition transition = initialState.new Transition(finalState, new TransitionRule()
        {
            @Override
            public boolean transitionIsAllowed()
            {
                final int triggerThreshold = 10;
                return (triggerVariable >= triggerThreshold);
            }
        });
        machine.start(initialState);
        transition.trigger();
        assert machine.getCurrentState() == initialState;
    }
}
