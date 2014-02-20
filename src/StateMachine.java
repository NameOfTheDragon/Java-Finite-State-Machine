/**
 * Created by Tim on 19/02/14.
 */

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * State: what does it do:
 * 1. Provides an OnEnter action
 * 2. Provides an OnExit action
 * 3. Defines a list of transitions, each of which has:
 * a) a trigger (method?)
 * b) an action
 * c) destination state
 * <p/>
 * What is a trigger?
 * - it is an event listener that provides input stimulus
 * to the state machine.
 * - It provides a method, isTriggerValid(), that determines
 * whether the trigger is valid at that time. Default implementation
 * is to return true.
 */

public class StateMachine
{
    private State currentState;

    public State getCurrentState()
    {
        return currentState;
    }


    public class StateTransition implements ActionListener
    {
        public StateTransition(State destinationState, TriggerRule allowRule)
        {
            if (destinationState == null)
                throw new IllegalArgumentException("Destination state is required");
            this.destinationState = destinationState;
            if (allowRule != null)
            {
                this.rule = allowRule;
            }
        }

        public StateTransition(State destinationState)
        {
            this(destinationState, null);
        }

        // isTriggerValid should be overridden to provide a validation rule.
        // This default implementation allows all triggers by default.
        public TriggerRule rule = new TriggerRule()
        {
            @Override
            public boolean triggerIsAllowed()
            {
                return true;
            }
        };

        private State destinationState;

        public void trigger()
        {
            //ToDo What if the transition is triggered while we are in a different state?
            if (rule.triggerIsAllowed())
                StateMachine.this.transitionToNewState(StateMachine.this.currentState, destinationState);
        }

        @Override
        public void actionPerformed(ActionEvent e)
        {
            this.trigger();
        }
    }

    /**
     * Transitions the state machine to a new state, calling the onExit action
     * on the old state and the onEnter action on the new state.
     *
     * @param fromState the current (old) state
     * @param toState   the new state
     */
    private void transitionToNewState(State fromState, State toState)
    {
        if (fromState != null)
            fromState.onExit.action();
        this.currentState = toState;
        toState.onEnter.action();
    }
}
