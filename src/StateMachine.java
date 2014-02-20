/**
 * Created by Tim on 19/02/14.
 */

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * A flexible, general purpose Finite State Machine.
 * <p/>
 * Wikipedia: A finite-state machine (FSM) or finite-state automaton
 * (plural: automata), or simply a state machine, is a mathematical
 * model of computation used to design both computer programs and
 * sequential logic circuits. It is conceived as an abstract machine
 * that can be in one of a finite number of states. The machine is in
 * only one state at a time; the state it is in at any given time is
 * called the current state. It can change from one state to another
 * when initiated by a triggering event or condition; this is called
 * a transition. A particular FSM is defined by a list of its states,
 * and the triggering condition for each transition.
 * <p/>
 * This implementation, in addition to modelling the state of a system
 * over time, allows actions to be associated with state transitions.
 * This is achieved via the OnEnter and OnExit actions defined by
 * each state. Actions are no-ops by default but can be overridden to
 * provide whatever logic is desired.
 * <p/>
 * Transitions are triggered by calling the trigger() instance method;
 * transitions also implement the @link ActionListener interface so that
 * they can be conveniently used in user interfaces. A transition may be
 * triggered at any time (and triggering is thread-safe), but in order
 * to cause a state transition several conditions conditions must be
 * met: the state that the transition rule is associated with must be
 * the state machine's current state; then the transition's validation
 * rule must return true. The default validation action is to simply
 * return true, but this can be overridden as needed to perform
 * arbitrary validation logic.
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
     *
     * @param initialState The initial state for the state machine.
     */
    public void start(State initialState)
    {
        this.currentState = initialState;
    }

    /**
     * Transitions the state machine to a new state, invoking the OnExit action
     * on the old state and the OnEnter action on the new state on the way.
     * Ensures thread safety by only allowing a single transition to be in progress.
     *
     * @param toState the new state
     */
    private void transitionToNewState(State toState)
    {
        //ToDo: deadlock hazard if one of the action methods causes another trigger.
        //ToDo: What happens if 2 triggers occur simultaneously on different threads?
        synchronized (transitionLock)
        {
            if (currentState != null)
                currentState.onExit.action();
            currentState = toState;
            toState.onEnter.action();
        }
    }

    /**
     * Represents a state that the state machine can be in.
     */
    public class State
    {

        private final String name;
        /**
         * The OnEnter action for the state, with a default null implementation.
         * Can be overridden to provide a custom OnEnter action.
         */
        protected StateTransitionAction onEnter = new StateTransitionAction()
        {
            @Override
            public void action()
            {
            }
        };
        /**
         * The OnExit action for the state, with default null implementation.
         * Can be overridden to provide a custom OnExit action.
         */
        protected StateTransitionAction onExit = new StateTransitionAction()
        {
            @Override
            public void action()
            {
            }
        };

        /**
         * Constructs a new State instance with the specified name.
         *
         * @param name The descriptive name of the state.
         */
        public State(String name)
        {
            this.name = name;
        }

        /**
         * Gets the descriptive name of the current state.
         */
        public String getName()
        {
            return name;
        }

        /**
         * Represents a transition to another state and a method of triggering
         * the transition, plus a rule for validating whether the transition
         * is currently allowed. For the transition to occur, it must be triggered;
         * The state machine must be in the state that owns the transition;
         * and the transition validation rule must be met; otherwise the trigger
         * is ignored.
         */
        public class Transition implements ActionListener
        {
            TransitionRule rule = new TransitionRule()
            {
                @Override
                public boolean transitionIsAllowed()
                {
                    return true;
                }
            };
            private State destinationState;

            /**
             * Represents a transition to another state and the
             * validation rule that must be satisfied before the
             * transition can occur.
             *
             * @param destinationState The destination state of the transition.
             * @param rule             The validation rule that must be satisfied
             *                         for the transition to occur. If this argument is null,
             *                         then the default rule (which always succeeds) is used.
             */
            public Transition(State destinationState, TransitionRule rule)
            {
                if (destinationState == null)
                    throw new IllegalArgumentException("Destination state is required");
                this.destinationState = destinationState;
                if (rule != null)
                {
                    this.rule = rule;
                }
            }

            /**
             * Creates a state transition to the specified state and uses the
             * default validation rule, which always succeeds.
             *
             * @param destinationState The new state after the transition has completed.
             */
            public Transition(State destinationState)
            {
                this(destinationState, null);
            }

            /**
             * Triggers the state transition, provided that the validation
             * rule succeeds and the owning state is the current state;
             * otherwise the trigger is silently ignored.
             */
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

            /**
             * Default implementation for the ActionListener is to trigger
             * the state transition. If this is overridden, then the overriding class
             * must call trigger() manually.
             *
             * @param e ActionEvent arguments.
             */
            @Override
            public void actionPerformed(ActionEvent e)
            {
                this.trigger();
            }
        }
    }
}
