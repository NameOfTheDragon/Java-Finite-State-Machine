import org.junit.Test;
import uk.co.tigranetworks.FalseStartException;
import uk.co.tigranetworks.StateMachine;
import uk.co.tigranetworks.TransitionRule;

/**
 * Created by Tim on 20/02/14.
 */

/*
ToDo:
 OnExit should be called on leaving any state
 OnEnter should be called on entering any state
 OnExit action should not deadlock if it triggers another transition
 Two or more simultaneous triggers should result in only one transition.
 Once a transition starts, it should always finish in the destination state regardless of any exceptions in OnEnter and OnExit action methods.
 Triggers called from within the OnExit method should always be ignored.

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
        assert !transition.rule.transitionIsAllowed();
    }

    @Test
    public void TransitionShouldBeAllowedWhenRuleCriteriaAreMet() throws FalseStartException
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
    public void TransitionShouldNotHappendWhenRuleCriteriaAreNotMet() throws FalseStartException
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

    @Test
    public void TransitionsShouldOnlyHappenIfTheStateMachineIsInTheCorrectState() throws FalseStartException
    {
        StateMachine machine = new StateMachine();
        final StateMachine.State initialState = machine.new State("Start");
        final StateMachine.State middleState = machine.new State("middle");
        final StateMachine.State finalState = machine.new State("Finish");
        final StateMachine.State.Transition transitionInitialToMiddle = initialState.new Transition(middleState);
        StateMachine.State.Transition transitionMiddleToFinal = middleState.new Transition(finalState);
        StateMachine.State.Transition transitionMiddleToStart = middleState.new Transition(initialState);
        machine.start(initialState);
        transitionMiddleToFinal.trigger();    // invalid
        assert machine.getCurrentState() == initialState;
        transitionInitialToMiddle.trigger();   // ok
        assert machine.getCurrentState() == middleState;
        transitionMiddleToFinal.trigger();  // ok
        assert machine.getCurrentState() == finalState;
    }

    @Test(expected = FalseStartException.class)
    public void InitialStateCanOnlyBeSetOnce() throws FalseStartException
    {
        StateMachine machine = new StateMachine();
        final StateMachine.State initialState = machine.new State("Start");
        assert machine.getCurrentState() != initialState;
        machine.start(initialState);
        assert machine.getCurrentState() == initialState;
        machine.start(initialState);    // should throw
    }
}
