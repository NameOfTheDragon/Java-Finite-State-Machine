/**
 * Created by Tim on 19/02/14.
 */

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

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
    private final Object transitionLock = new Object();   // protects against simultaneous transitions
    private State currentState;

    public State getCurrentState()
    {
        return currentState;
    }

    /**
     * Sets the initial state and starts the state machine.
     * @param initialState The initial state for the state machine.
     */
    public void start(State initialState)
    {
        this.currentState = initialState;
    }

    /**
     * Represents a state that the state machine can be in.
     */
    public class State
    {

        private final String name;
        private final List<StateTransition> stateTransitions = new ArrayList<StateTransition>();

        /**
         * Gets the descriptive name of the current state.
         */
        public String getName()
        {
            return name;
        }

        /**
         * The OnEnter action for the state, with a default null implementation.
         * Can be overridden to provide a custom OnEnter action.
         */
        protected StateTransitionAction onEnter = new StateTransitionAction()
        {
            @Override public void action() {}
        };
        /**
         * The OnExit action for the state, with default null implementation.
         * Can be overridden to provide a custom OnExit action.
         */
        protected StateTransitionAction onExit = new StateTransitionAction()
        {
            @Override public void action() {}
        };

        /**
         * Constructs a new State instance with the specified name.
         * @param name The descriptive name of the state.
         */
        public State(String name)
        {
            this.name = name;
        }

        /**
         * Represents a transition to another state and a method of triggering
         * the transition, plus a rule for validating whether the transition
         * is currently allowed. For the transition to occur, it must be triggered;
         * The state machine must be in the state that owns the transition;
         * and the transition validation rule must be met; otherwise the trigger
         * is ignored.
         */
        public class StateTransition implements ActionListener
        {
            /**
             * Creates a state transition to the specified state
             * and supplies a validation rule that must be met before the
             * transition can occur.
             * @param destinationState The new state after the transition has completed.
             * @param allowRule The conditions that must be met before the transition can occur.
             */
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

            /**
             * Creates a state transition to the specified state and uses the
             * default validation rule, which always allows the transition.
             * @param destinationState The new state after the transition has completed.
             */
            public StateTransition(State destinationState)
            {
                this(destinationState, null);
            }

            // isTriggerValid should be overridden to provide a validation rule.
            // This default implementation allows all triggers by default.
            public TriggerRule rule = new TriggerRule()
            {
                @Override
                public boolean transitionIsAllowed()
                {
                    return true;
                }
            };

            private State destinationState;

            public void trigger()
            {
                // Triggers are only valid if the state machine is in the correct state, otherwise they are ignored.
                if (StateMachine.this.currentState != State.this)
                {
                    // ToDo - print some diagnostics about the trigger being ignored.
                    return;
                }

                if (rule.transitionIsAllowed())
                    StateMachine.this.transitionToNewState(destinationState);
            }

            @Override
            public void actionPerformed(ActionEvent e)
            {
                this.trigger();
            }
        }

    }

    /**
     * Transitions the state machine to a new state, calling the onExit action
     * on the old state and the onEnter action on the new state. Ensures thread safety
     * by only allowing a single transition to be in progress.
     *
     * @param toState the new state
     */
    private void transitionToNewState(State toState)
    {
        synchronized (transitionLock)
        {
            if (currentState != null)
                currentState.onExit.action();
            currentState = toState;
            toState.onEnter.action();
        }
    }
}
