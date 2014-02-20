/**
 * Created by Tim on 19/02/14.
 */
public class TurnstyleStateMachineDemo
{
    private StateMachine turnstile = new StateMachine();
    int moneyInserted = 0;

    private void composeStateMachine()
    {
        StateMachine.State stateLocked = turnstile.new State("Locked");
        StateMachine.State stateUnlocked = turnstile.new State("Unlocked");

        StateMachine.State.Transition transitionUnlockedToLocked = stateUnlocked.new Transition(stateLocked);
        StateMachine.State.Transition transitionLockedToUnlocked = stateLocked.new Transition(stateUnlocked,
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
