import junit.framework.Assert;
import org.junit.Test;
import org.testng.annotations.ExpectedExceptions;

/**
 * Created by Tim on 20/02/14.
 */

/*
 ToDo

 Transitions should only be actioned if the state machine is in the correct state
*/

public class StateMachineTests
{
    private final State stateTest = new State("Unit test");
    private int triggerVariable;

    @Test
    public void TriggerShouldReturnTrueByDefault()
    {
        StateMachine machine = new StateMachine();
        StateMachine.StateTransition transition = machine.new StateTransition(stateTest);
        boolean isValid = transition.rule.triggerIsAllowed();
        assert isValid;
    }

    @Test
    public void TriggerShouldReturnFalseWhenOverridden()
    {
        StateMachine machine = new StateMachine();
        StateMachine.StateTransition transition = machine.new StateTransition(stateTest, new TriggerRule()
        {
            @Override
            public boolean triggerIsAllowed()
            {
                return false;
            }
        });
        boolean isAllowed = transition.rule.triggerIsAllowed();
        assert !isAllowed;
    }

    @Test(expected = IllegalArgumentException.class)
    public void TransitionConstructorShouldNotAllowNullStates()
    {
        StateMachine machine = new StateMachine();
        StateMachine.StateTransition transition = machine.new StateTransition(null);
    }

    @Test
    public void TransitionShouldBeAllowedWhenRuleCriteriaAreMet()
    {
        triggerVariable = 10;
        StateMachine machine = new StateMachine();
        StateMachine.StateTransition transition = machine.new StateTransition(stateTest, new TriggerRule()
        {
            @Override
            public boolean triggerIsAllowed()
            {
                final int triggerThreshold = 10;
                return (triggerVariable >= triggerThreshold);
            }
        });
        transition.trigger();
        assert machine.getCurrentState() == stateTest;
    }

    @Test
    public void TransitionShouldNotHappendWhenRuleCriteriaAreNotMet()
    {
        triggerVariable = 9;
        StateMachine machine = new StateMachine();
        StateMachine.StateTransition transition = machine.new StateTransition(stateTest, new TriggerRule()
        {
            @Override
            public boolean triggerIsAllowed()
            {
                final int triggerThreshold = 10;
                return (triggerVariable >= triggerThreshold);
            }
        });
        transition.trigger();
        assert machine.getCurrentState() == null;
    }
}
