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

        StateMachine.State.Transition transitionUnlockedToLocked = stateLocked.new Transition(stateLocked);
        StateMachine.State.Transition transitionLockedToUnlocked = stateUnlocked.new Transition(stateUnlocked,
                new TransitionRule()
                {
                    @Override
                    public boolean transitionIsAllowed()
                    {
                        return (moneyInserted >= 20);
                    }
                });
    }
}
