package uk.co.tigranetworks; /**
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
    private final    State  hiddenStateWithNoTransitions = new State("State Machine Paused");
    private volatile State  currentState                 = hiddenStateWithNoTransitions;
    private final    Object transitionLock               = new Object();   // protects against simultaneous transitions

    // Event sources that produce information about the inner workings of the state machine.
    private TraceListener onStateChanged;
    private TraceListener onTrigger;

    public State getCurrentState()
    {
        return currentState;
    }

    /**
     * Sets the initial state and starts the state machine.
     * Each instance must be started exactly once.
     * <p/>
     * Until the machine is started, all transitions and triggers
     * will be inoperative and the state machine will not function.
     * <p/>
     * Any subsequent calls result in a uk.co.tigranetworks.FalseStartException, and the
     * state machine will remain unaffected. If the state machine
     * needs to be restarted for any reason, then it must be
     * destroyed and re-created.
     *
     * @param initialState The initial state for the state machine.
     * @throws FalseStartException Thrown if the start() method is
     *                             called more than once on an instance.
     */
    public void start(State initialState) throws FalseStartException
    {
        if (currentState != hiddenStateWithNoTransitions)
            throw new FalseStartException();
        transitionToNewState(hiddenStateWithNoTransitions, initialState);
    }

    /**
     * Transitions the state machine to a new state, invoking the OnExit action
     * on the old state and the OnEnter action on the new state on the way.
     * Ensures thread safety by only allowing a single transition to be in progress.
     * <p/>
     * Exceptions within the OnEnter and OnExit action methods are not caught,
     * but we do guarantee that once the transition starts, it will complete
     * with the state machine in toState.
     *
     * @param fromState The state that the transition is associated with
     * @param toState   The destination state (the new current state).
     */
    private void transitionToNewState(State fromState, State toState)
    {
        synchronized (transitionLock)
        {
            // Avoid the race condition where currentState has changed since the transition started.
            if (fromState != currentState)
                return;
            // The state machine temporarily goes into a private state so that
            // all transitions are rendered invalid (except the one in progress).

            currentState = hiddenStateWithNoTransitions;
            try
            {
                if (fromState != null)
                    fromState.onExit.action();
            }
            finally
            {
                currentState = toState;
                raiseOnStateChanged(fromState.getName(), toState.getName());
            }
            toState.onEnter.action();
        }
    }

    /**
     * Fires the specified trance event and provides the descriptive text
     * to the listener.
     *
     * @param listener    The event being raised
     * @param description Trace output text.
     */
    private void raiseTraceEvent(TraceListener listener, String description)
    {
        try
        {
            if (listener != null)
            {
                listener.trace(description);
            }
        }
        catch (Exception ex)
        {
            // Trace is not allowed to throw any exceptions.
        }
    }

    /**
     * Raises the OnStateChanged trace event.
     *
     * @param fromState The name of the original state.
     * @param toState   The name of the destination state.
     */
    protected void raiseOnStateChanged(String fromState, String toState)
    {
        try
        {
            String description = String.format("State transition [%s]->[%s]", fromState, toState);
            raiseTraceEvent(onStateChanged, description);
        }
        catch (Exception ex)
        {
        }
    }

    /**
     * Raises the OnTrigger trace event.
     *
     * @param fromState The name of the original state.
     * @param toState   The name of the destination state.
     */
    protected void raiseOnTrigger(String fromState, String toState, String outcome)
    {
        try
        {
            String description = String.format("Triggered transition from [%s] to [%s] outcome: %s", fromState, toState, outcome);
            raiseTraceEvent(onTrigger, description);
        }
        catch (Exception ex)
        {
        }
    }

    /**
     * Sets a listener for the OnStateChanged event.
     *
     * @param listener The listener. There can be only one.
     */
    public void setOnStateChangedListener(TraceListener listener)
    {
        onStateChanged = listener;
    }

    /**
     * Sets a listener for the OnTrigger event.
     *
     * @param listener The listener. There can be only one.
     */
    public void setOnTriggerListener(TraceListener listener)
    {
        onTrigger = listener;
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
        protected StateTransitionAction onExit  = new StateTransitionAction()
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
         * Creates a new state, with action methods for the ONEnter and OnExit actions.
         * If either action is null, then the default null action is used.
         *
         * @param name    The name of the state (required; not null or empty).
         * @param onEnter The action to be performed on entering this state (or null if none).
         * @param onExit  The action to be performed on leaving this state (or null if none).
         */
        public State(String name, StateTransitionAction onEnter, StateTransitionAction onExit)
        {
            if (name.isEmpty())
                throw new IllegalArgumentException("State name must not be empty or null");
            this.name = name;
            if (onEnter != null)
                this.onEnter = onEnter;
            if (onExit != null)
                this.onExit = onExit;
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
            public TransitionRule rule = new TransitionRule()
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
                    raiseOnTrigger(State.this.getName(), destinationState.getName(), "disarmed");
                    return;
                }

                if (rule.transitionIsAllowed())
                {
                    raiseOnTrigger(State.this.getName(), destinationState.getName(), "armed, executing");
                    StateMachine.this.transitionToNewState(State.this, destinationState);
                }
                else
                    raiseOnTrigger(State.this.getName(), destinationState.getName(), "armed, rejected");
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
