package org.hit.android.haim.calc.action;

import org.hit.android.haim.calc.action.impl.*;

import java.util.Arrays;
import java.util.List;

/**
 * All available action types of our calculator, mapped to their implementing actions, to act as a Factory for commands.
 *
 * @author Haim Adrian
 * @since 28-Feb-21
 */
public enum ActionType {
    //@formatter:off
    PLUS("+", Plus.class, "%s + %s = "),
    MINUS("-", Minus.class, "%s - %s = "),
    MULTIPLY("×", Mul.class, "%s × %s = "),
    DIVIDE("÷", Div.class, "%s ÷ %s = "),
    SQUARE("x²", Square.class, "%s² = "),
    SQUARE_ROOT("√", SquareRoot.class, "√%s = "),
    FACTORIAL("n!", Factorial.class, "%s! = "),

    // Functions
    RAND("rand", Rand.class, "rand(1) = "),
    LN("ln", Ln.class, "ln(%s) = "),
    ABS("|x|", Abs.class, "|%s| = "),
    FLOOR("⌊x⌋", Floor.class, "⌊%s⌋ = "),
    CEIL("⌈x⌉", Ceil.class, "⌈%s⌉ = "),

    // Trigonometric functions
    SIN("sin", Sin.class, "sin(%s) = "),
    COS("cos", Cos.class, "cos(%s) = "),
    TAN("tan", Tan.class, "tan(%s) = "),
    COT("cot", Cot.class, "cot(%s) = "),

    CONNECT("hello", null, "hello"),
    DISCONNECT("bye", null, "bye"),
    UNKNOWN("", null, "");
    //@formatter:on

    /**
     * Action text as we display on the calculator
     */
    private final String actionText;

    /**
     * Implementing command, to be able to create commands according to Factory method
     */
    private final Class<? extends ActionIfc<?>> actionClass;

    /**
     * In order to support descriptive calculation, we have a template to display special functions so it is easier for user to see
     * what function we have executed. e.g. "3! = 6"
     */
    private final String descriptiveText;

    ActionType(String actionText, Class<? extends ActionIfc<?>> actionClass, String descriptiveText) {
        this.actionText = actionText;
        this.actionClass = actionClass;
        this.descriptiveText = descriptiveText;
    }

    /**
     * In order to get an enum type based on buttons text, we expose this function such that we can use the text from events
     * and know what action type it is.
     *
     * @param actionText The text to find action type for
     * @return The action type or UNKNOWN if there is no match.
     */
    public static ActionType valueOfActionText(String actionText) {
        ActionType result = UNKNOWN;
        String actionTextTrimmed = String.valueOf(actionText).trim();

        for (ActionType action : values()) {
            if (action.getActionText().equals(actionTextTrimmed)) {
                result = action;
                break;
            }
        }

        return result;
    }

    public static List<ActionType> listFunctions() {
        return Arrays.asList(FLOOR, CEIL, ABS, RAND, LN);
    }

    public static List<ActionType> listTrigoFunctions() {
        return Arrays.asList(SIN, COS, TAN, COT);
    }

    public String getActionText() {
        return actionText;
    }

    /**
     * Use this method to construct a new command that implements the calculation of this action type.
     *
     * @param <R> Expected response type. Mostly Double.
     * @return The implementing command or null if we have failed to construct it.
     */
    @SuppressWarnings("unchecked")
    public <R extends ActionIfc<?>> R newActionInstance() {
        R action = null;

        if (actionClass != null) {
            try {
                action = (R) actionClass.getConstructor().newInstance();
            } catch (Exception e) {
                System.out.println("FML");
                e.printStackTrace();
            }
        }

        return action;
    }

    /**
     * In order to support descriptive calculation, we have a template to display special functions so it is easier for user to see
     * what function we have executed. e.g. "3! = 6"
     *
     * @return The descriptive text
     */
    public String getDescriptiveText() {
        return descriptiveText;
    }
}
