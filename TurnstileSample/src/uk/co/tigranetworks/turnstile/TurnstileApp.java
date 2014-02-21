package uk.co.tigranetworks.turnstile;

import com.codeproject.javaConsole.JavaConsole;
import uk.co.tigranetworks.*;

import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Tim on 21/02/14.
 */
public class TurnstileApp implements TraceListener
{
    private final JavaConsole console              = new JavaConsole();
    private final Scanner     scanner              = new Scanner(System.in);
    private final Timer       gateLockTimer        = new Timer("Turnstile Gate Lock Timer");
    private       Integer     moneyInCoinValidator = 0;
    private StateMachine.State.Transition transitionUnlockedToLocked;
    private StateMachine.State.Transition transitionLockedToUnlocked;
    private StateTransitionAction         onEnterUnlocked;
    private TransitionRule                transitionRuleLockedToUnlocked;
    private StateTransitionAction         onEnterLocked;
    private TimerTask                     taskLockGateWhenTimerExpires;
    private StateMachine                  turnstile;

    public void run()
    {
        System.out.println("Finite-State Machine Turnstile simulation\n\n");
        ComposeStateMachine();

        String userInput = "";
        System.out.println("Please simulate coin validation by entering the amount of money inserted (in pence) and pressing enter. The gate will unlock at 20 pence.\n");
        while (userInput != "exit")
        {
            userInput = scanner.next();
            try
            {
                int amount = Integer.parseInt(userInput);
                moneyInserted(amount);
            }
            catch (NumberFormatException ex)
            {
                System.out.println("Invalid input\n");
            }
        }
    }

    void unlockGate()
    {
        System.out.println("Gate unlocked");
    }

    void lockGate()
    {
        System.out.println("Gate locked");
    }

    private void moneyInserted(int amount)
    {
        moneyInCoinValidator += amount;
        printMoneyTotal(amount, moneyInCoinValidator);
        // Attempt to trigger the state transition to unlocked.
        // This will only succeed if enough money has been inserted,
        // because of the transition validation rule.
        transitionLockedToUnlocked.trigger();
    }

    private void printMoneyTotal(int inserted, int total)
    {
        System.out.printf("Amount entered: %d Running total: %d\n", inserted, total);
    }

    private void clearMoneyTotal()
    {
        moneyInCoinValidator = 0;
        System.out.println("Money total reset to 0.");
    }

    /**
     * Schedules the turnstile gate to lock in 5 seconds.
     */
    private void scheduleGateLocking()
    {
        taskLockGateWhenTimerExpires = new TimerTask()
        {
            @Override
            public void run()
            {
                transitionUnlockedToLocked.trigger();
            }
        };
        gateLockTimer.schedule(taskLockGateWhenTimerExpires, 5000l);
    }

    void ComposeStateMachine()
    {
        turnstile = new StateMachine();

        // OnEnter and OnExit action methods
        onEnterLocked = new StateTransitionAction()
        { // OnEnter - lock the gate and clear the money total.
            @Override
            public void action()
            {
                lockGate();
                clearMoneyTotal();
            }
        };
        onEnterUnlocked = new StateTransitionAction()
        { // OnEnter - unlock the gate and schedule it to lock in 5 seconds.
            @Override
            public void action()
            {
                unlockGate();
                scheduleGateLocking();
            }
        };

        // State definitions
        StateMachine.State stateUnlocked = turnstile.new State("Gate Unlocked", onEnterUnlocked, null);
        StateMachine.State stateLocked = turnstile.new State("Gate Locked", onEnterLocked, null);

        // Transition validation rules
        transitionRuleLockedToUnlocked =
                new TransitionRule()
                {
                    @Override
                    public boolean transitionIsAllowed()
                    {
                        return (moneyInCoinValidator >= 20);
                    }
                };

        // State transitions
        transitionUnlockedToLocked = stateUnlocked.new Transition(stateLocked);
        transitionLockedToUnlocked = stateLocked.new Transition(stateUnlocked, transitionRuleLockedToUnlocked);

        // Install diagnostic listeners
        turnstile.setOnStateChangedListener(this);
        turnstile.setOnTriggerListener(this);

        // Start the state machine and set the initial state to Locked.
        try
        {
            turnstile.start(stateLocked);
        }
        catch (FalseStartException ex)
        {
            System.exit(-1);
        }
    }

    /**
     * Listens for trace events from the state maching and prints them to the console.
     *
     * @param traceOutput The diagnostic output.
     */
    @Override
    public void trace(String traceOutput)
    {
        System.out.println(traceOutput);
    }
}

