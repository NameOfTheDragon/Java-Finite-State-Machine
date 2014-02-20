/**
 * Created by Tim on 19/02/14.
 */
public class TurnstyleStateMachineDemo
{
    private StateMachine machine = new StateMachine();
    int moneyInserted = 0;

    private void composeStateMachine()
    {
         State stateLocked = new State("Locked");
         State stateUnlocked = new State("Unlocked");

         StateMachine.StateTransition transitionUnlockedToLocked = machine.new StateTransition(stateLocked);
         StateMachine.StateTransition transitionLockedToUnlocked = machine.new StateTransition(stateUnlocked,
         new TriggerRule()
                {
                    @Override
                    public boolean triggerIsAllowed()
                    {
                        return (moneyInserted >= 20);
                    }
                });
        stateLocked.addTransition(transitionLockedToUnlocked);
        stateUnlocked.addTransition(transitionUnlockedToLocked);
    }
}
