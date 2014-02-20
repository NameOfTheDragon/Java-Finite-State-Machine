/**
 * Created by Tim on 19/02/14.
 */
public class TurnstyleStateMachineDemo
{
    private StateMachine machine = new StateMachine();
    int moneyInserted = 0;

    private void composeStateMachine()
    {
        StateMachine.State stateLocked = machine.new State("Locked");
        StateMachine.State stateUnlocked = machine.new State("Unlocked");

        StateMachine.State.StateTransition transitionUnlockedToLocked = stateLocked.new StateTransition(stateLocked);
        StateMachine.State.StateTransition transitionLockedToUnlocked = stateUnlocked.new StateTransition(stateUnlocked,
                new TriggerRule()
                {
                    @Override
                    public boolean transitionIsAllowed()
                    {
                        return (moneyInserted >= 20);
                    }
                });
    }
}
