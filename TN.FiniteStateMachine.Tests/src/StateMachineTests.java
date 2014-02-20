import org.junit.Test;
import org.testng.annotations.ExpectedExceptions;

/**
 * Created by Tim on 20/02/14.
 */
public class StateMachineTests
{
    private final State stateTest = new State("Unit test");

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
}
