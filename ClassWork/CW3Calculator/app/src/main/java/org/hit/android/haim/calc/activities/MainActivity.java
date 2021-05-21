package org.hit.android.haim.calc.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.res.ResourcesCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.ui.AppBarConfiguration;

import com.google.android.material.navigation.NavigationView;

import org.hit.android.haim.calc.R;
import org.hit.android.haim.calc.action.ActionContext;
import org.hit.android.haim.calc.action.ActionType;
import org.hit.android.haim.calc.action.ArithmeticAction;
import org.hit.android.haim.calc.fragments.FragmentBasic;
import org.hit.android.haim.calc.fragments.FragmentScientific;
import org.hit.android.haim.calc.server.CalculatorWebService;

import java.util.function.Consumer;

public class MainActivity extends AppCompatActivity {
    private static final String SHARED_PREFERENCES_NAME = "calc-shared";

    private static final String STORED_OUTPUT_KEY = "output";
    private static final String STORED_ACTION_KEY = "action";
    private static final String STORED_VALUE_KEY = "value";
    public static final String STORED_SERVER_KEY = "isServer";

    private AppBarConfiguration mAppBarConfiguration;
    private DrawerLayout navDrawer;

    private FragmentManager fragmentManager;
    private FragmentTransaction fragmentTransaction;

    private TextView outputText;
    private String action = "";
    private double value;

    private FragmentBasic basicFragment;
    private FragmentScientific scientificFragment;
    private boolean isServer = false;

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private Switch serverSwitch;
    private NavigationView navigationView;

    public MainActivity() {
        Log.d("Lifecycle", this.toString() + ".new");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("Lifecycle", this.toString() + ".onCreate");
        setContentView(R.layout.activity_main);

        navDrawer = findViewById(R.id.drawer_layout);
        navDrawer.closeDrawers();

        Toolbar myToolbar = findViewById(R.id.app_toolbar);
        setSupportActionBar(myToolbar);

        navigationView = findViewById(R.id.nav_view);

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration =
                new AppBarConfiguration.Builder(R.id.nav_standard, R.id.nav_science, R.id.nav_server, R.id.nav_login, R.id.nav_exit)
                        .setOpenableLayout(navDrawer)
                        .build();

        // Listen to navigation buttons, to handle exit here instead of navigating to other fragments.
        navigationView.setNavigationItemSelectedListener(this::onMenuItemSelected);

        outputText = findViewById(R.id.outputText);
        outputText.setMovementMethod(new ScrollingMovementMethod());

        serverSwitch = (Switch) navigationView.getMenu().findItem(R.id.nav_server).getActionView();
        serverSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> setIsServerMode(isChecked));

        // Set text to outputText based on saved instance state / shared preferences
        initializeTextBasedOnSavedState(savedInstanceState);

        // Start with basic fragment
        fragmentManager = getSupportFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();
        basicFragment = new FragmentBasic();
        fragmentTransaction.add(R.id.fragmentsLayout, basicFragment).commit();
        fragmentTransaction = fragmentManager.beginTransaction();
        scientificFragment = new FragmentScientific();
        fragmentTransaction.add(R.id.scientificLayout, scientificFragment).commit();

        if (getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            findViewById(R.id.scientificLayout).setVisibility(View.VISIBLE);
            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) myToolbar.getLayoutParams();
            layoutParams.height = 175;
            myToolbar.setLayoutParams(layoutParams);
        } else {
            findViewById(R.id.scientificLayout).setVisibility(View.GONE);
            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) myToolbar.getLayoutParams();
            layoutParams.height = 200;
            myToolbar.setLayoutParams(layoutParams);
        }

        CalculatorWebService.getInstance();
    }

    /**
     * Occurs when user selects a menu item in te navigation view
     * @return Whether to draw selection background or not (We draw selection for selectable items and not buttons)
     */
    private boolean onMenuItemSelected(MenuItem item) {
        boolean shouldDrawSelection = false;
        try {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_exit) {
                Toast.makeText(this, "Good Bye!", Toast.LENGTH_LONG).show();
            } else if (itemId == R.id.nav_server) {
                serverSwitch.setChecked(!serverSwitch.isChecked());
            } else if (itemId == R.id.nav_login) {
                Intent i = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(i);

                // Finish this activity as we went to login activity
                finish();
            } else if (itemId == R.id.nav_standard) {
                View scientificLayout = findViewById(R.id.scientificLayout);
                if (scientificLayout.getVisibility() == View.VISIBLE) {
                    new Handler().post(() -> {
                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                        scientificLayout.setVisibility(View.GONE);
                    });
                }

                shouldDrawSelection = true;
            } else if (itemId == R.id.nav_science) {
                View scientificLayout = findViewById(R.id.scientificLayout);
                if (scientificLayout.getVisibility() != View.VISIBLE) {
                    new Handler().post(() -> {
                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                        scientificLayout.setVisibility(View.VISIBLE);
                    });
                }

                shouldDrawSelection = true;
            }
        } catch (Exception e) {
            Log.d("error", e.getMessage(), e);
        }

        // Do it later so we will exit the event before losing stuff
        new Handler().post(() -> navDrawer.closeDrawers());
        return shouldDrawSelection;
    }

    private void setIsServerMode(boolean isServerMode) {
        this.isServer = isServerMode;
        Log.d("ServerMode", "Is using server mode: " + isServer);

        // Show the login when we work against server only
        navigationView.getMenu().findItem(R.id.nav_login).setVisible(isServerMode);
    }

    /**
     * Occurs when user clicks the burger icon
     */
    public void onMenuClicked(View view) {
        navDrawer.open();
    }

    /**
     * Reads data from saved instance state and set it to data members.<br/>
     * Saved instance state is used when we change screen orientation and the activity is re-created.<br/>
     * When we first start-up, there is no saved instance state. In this case, we get the data from shared preferences.
     * @param savedInstanceState The saved instance state.
     */
    private void initializeTextBasedOnSavedState(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            Log.d("Lifecycle", this.toString() + ".onCreate: Saved instance is null");

            SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE);
            if (!sharedPreferences.contains(STORED_VALUE_KEY)) {
                Log.d("Lifecycle", this.toString() + ".onCreate: No data in shared preferences");
                value = 0;
                action = "";
                updateOutputValue();
            } else {
                Log.d("Lifecycle", this.toString() + ".onCreate: Loading data from shared preferences: " + sharedPreferences.getString(STORED_VALUE_KEY, "null"));
                try {
                    value = Double.parseDouble(sharedPreferences.getString(STORED_VALUE_KEY, "0"));
                } catch (Exception e) {
                    Log.e("Error", "Failed to read stored value from shared preferences. Value: " + sharedPreferences.getString(STORED_VALUE_KEY, "0"), e);
                    value = 0;
                }

                outputText.setText(getValueText(false));
                outputText.bringPointIntoView(outputText.length());
            }

            setIsServerMode(getIntent().getBooleanExtra(STORED_SERVER_KEY, false));
            serverSwitch.setChecked(isServer);

            Toast.makeText(this, "Welcome!", Toast.LENGTH_LONG).show();
        } else {
            Log.d("Lifecycle", this.toString() + ".onCreate: Restoring saved instance state");
            value = savedInstanceState.getDouble(STORED_VALUE_KEY, 0);
            action = savedInstanceState.getString(STORED_ACTION_KEY, "");
            setIsServerMode(savedInstanceState.getBoolean(STORED_SERVER_KEY, false));

            serverSwitch.setChecked(isServer);
            outputText.setText(savedInstanceState.getString(STORED_OUTPUT_KEY, ""));
            outputText.bringPointIntoView(outputText.length());
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d("Event", this.toString() + ".onSaveInstanceState");

        navDrawer.closeDrawers();

        outState.putString(STORED_OUTPUT_KEY, outputText.getText().toString());
        outState.putString(STORED_ACTION_KEY, action);
        outState.putDouble(STORED_VALUE_KEY, value);
        outState.putBoolean(STORED_SERVER_KEY, isServer);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("Lifecycle", this.toString() + ".onDestroy");

        navDrawer.closeDrawers();

        String lastValue = getLine();
        if (!lastValue.isEmpty()) {
            double value;
            try {
                value = parseValue(lastValue);
            } catch (Exception ignore) {
                value = this.value;
            }

            // Save last value to disk so it will be available next time the app is launched
            SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE);
            SharedPreferences.Editor sharedPrefsEditor = sharedPreferences.edit();
            sharedPrefsEditor.putString(STORED_VALUE_KEY, String.valueOf(value));
            sharedPrefsEditor.apply();

            Log.d("Event", "Stored to shared preferences: " + value);
        }

        CalculatorWebService.getInstance().disconnect();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        Log.d("Event", this.toString() + ".onCreateContextMenu");
        try {
            // Context menu
            menu.add(Menu.NONE, 2, Menu.NONE, v.getId() == R.id.button2 ? "2" : "3");
            menu.add(Menu.NONE, 3, Menu.NONE, v.getId() == R.id.button2 ? getString(R.string.e) : getString(R.string.pi));
            v.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.button_num_design, null));
        } catch (Throwable t) {
            setErrorOutput(String.format(getString(R.string.error), t.getMessage()));
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        Log.d("Event", this.toString() + ".onContextItemSelected");
        try {
            String charToAppend = item.getTitle().toString();
            if (Character.isDigit(charToAppend.charAt(0))) {
                onOperandButtonClicked(charToAppend.equals("2") ? basicFragment.getView().findViewById(R.id.button2) : basicFragment.getView().findViewById(R.id.button3));
            }
            // Pi or e
            else {
                appendSpecialChar(charToAppend);
            }

            return true;
        } catch (Throwable t) {
            setErrorOutput(String.format(getString(R.string.error), t.getMessage()));
        }

        return false;
    }

    @Override
    public void onBackPressed() {
        Log.d("Event", this.toString() + ".onBackPressed");
        super.onBackPressed();
    }

    /**
     * Appends a special character (pi or e) that we cannot append at the end of a number.
     * @param specialChar The character to append.
     */
    private void appendSpecialChar(String specialChar) {
        if (getLine().matches("0*")) {
            // In case it is not a new line, discard the last line with zeroes.
            discardLastLineInOutput();

            appendOutputTextAndScrollToBottom(specialChar);
        } else {
            String str = outputText.getText().toString();
            char lastChar = str.charAt(str.length() - 1);
            if (!Character.isDigit(lastChar) && (lastChar != '.') && (lastChar != getString(R.string.e).charAt(0)) && (lastChar != getString(R.string.pi).charAt(0))) {
                appendOutputTextAndScrollToBottom(specialChar);
            }
        }
    }

    /**
     * Find the last line in the output, and return it trimmed.<br/>
     * We use this value in order to perform operations, both single argument and two arguments operation types.
     *
     * @return Last line in the output, to perform operations based on.
     */
    private String getLine() {
        String out = outputText.getText().toString().trim();
        int indexOfNewLine = out.lastIndexOf(System.lineSeparator());
        if (indexOfNewLine >= 0) {
            out = out.substring(indexOfNewLine + System.lineSeparator().length());
        }

        return out;
    }

    /**
     * Parses an input value into double. Also handles Pi and e.
     * @param value The value to parse into double
     * @return A double value representing input value
     */
    private double parseValue(String value) {
        if (value.equals(getString(R.string.e))) {
            return Math.E;
        }

        if (value.equals(getString(R.string.pi))) {
            return Math.PI;
        }

        if (value.equals("-" + getString(R.string.infinity))) {
            return Double.NEGATIVE_INFINITY;
        }

        if (value.equals(getString(R.string.infinity))) {
            return Double.POSITIVE_INFINITY;
        }

        return Double.parseDouble(value);
    }

    public void setErrorOutput(String textToSet) {
        outputText.setText(textToSet);
        if (!textToSet.endsWith(System.lineSeparator())) {
            outputText.append(System.lineSeparator());
        }

        outputText.scrollTo(0, 0);
    }

    private void appendOutputTextAndScrollToBottom(CharSequence textToAppend) {
        outputText.append(textToAppend);
        outputText.bringPointIntoView(outputText.length());
    }

    /**
     * Gets current {@link #value} as text. Also handles Pi and e.
     * @param shouldCastToLong Whether we should treat it as an integer, rather than float. (for Factorial output)
     * @return Current value for output
     */
    private String getValueText(boolean shouldCastToLong) {
        return getValueText(value, shouldCastToLong);
    }

    /**
     * Converts a value as text. Also handles Pi and e.
     * @param shouldCastToLong Whether we should treat it as an integer, rather than float. (for Factorial output)
     * @return Current value for output
     */
    private String getValueText(double value, boolean shouldCastToLong) {
        if (Double.isFinite(value)) {
            return ((value == (long) value) || shouldCastToLong) ? String.valueOf((long) value) : (value == Math.PI ? getString(R.string.pi) : (value == Math.E ? getString(R.string.e) : String.valueOf(value)));
        }

        String infinity = getString(R.string.infinity);
        return value < 0 ? "-" + infinity : infinity;
    }

    /**
     * Call this method when data member {@link #value} is updated with a new value, and you want to print it to the output text view.
     */
    private void updateOutputValue() {
        if (getLine().length() > 0) {
            outputText.append(System.lineSeparator());
        }

        appendOutputTextAndScrollToBottom(getValueText(false));
    }

    /**
     * Raised when user presses the clear (C) button
     *
     * @param view Sender button
     */
    public void onClearButtonClicked(View view) {
        Log.d("Event", this.toString() + ".onClearButtonClicked");
        try {
            outputText.setText("");
            action = "";
            value = 0;
            updateOutputValue();
        } catch (Throwable t) {
            setErrorOutput(String.format(getString(R.string.error), t.getMessage()));
        }
    }

    /**
     * Raised when user presses the equals button, to perform calculation and show the result.
     *
     * @param view Sender button
     */
    public void onEqualsButtonClicked(View view) {
        Log.d("Event", this.toString() + ".onEqualsButtonClicked");
        try {
            String currOut = getLine();

            if (!currOut.isEmpty()) {
                // If there is no action, just print latest row again.
                if (action.isEmpty()) {
                    if (!outputText.getText().toString().endsWith(System.lineSeparator())) {
                        outputText.append(System.lineSeparator());
                    }

                    // In order to let user to see the value behind PI, don't format the value here.
                    double value = parseValue(currOut);
                    String infinity = getString(R.string.infinity);
                    appendOutputTextAndScrollToBottom(value == (long) value ? String.valueOf((long) value) : (Double.isFinite(value) ? String.valueOf(value) : (value < 0 ? "-" + infinity : infinity)));
                    outputText.append(System.lineSeparator());
                } else {
                    int lastActionIndex = currOut.lastIndexOf(action);
                    double lastVal = parseValue(currOut.substring(lastActionIndex + 1));
                    doAction(value, lastVal, action, value -> {
                        this.value = value;
                        updateOutputValue();
                        this.outputText.append(System.lineSeparator());
                        this.action = "";
                        this.value = 0;
                    });
                }
            }
        } catch (Throwable t) {
            setErrorOutput(String.format(getString(R.string.error), t.getMessage()));
        }
    }

    /**
     * Raised when user presses operand (0-9) or dot (.) buttons, to update outputText text view accordingly.
     *
     * @param view Sender button, to get its text and append to output
     */
    public void onOperandButtonClicked(View view) {
        Log.d("Event", this.toString() + ".onOperandButtonClicked");
        try {
            Button senderButton = (Button) view;

            if (getLine().matches("0*") && !".".equals(senderButton.getText().toString())) {
                // In case it is not a new line, discard the last line with zeroes.
                discardLastLineInOutput();
            }

            // In case existing text is empty (when we removed last line with 0) just append the operand.
            String str = outputText.getText().toString();
            if (str.isEmpty()) {
                appendOutputTextAndScrollToBottom(senderButton.getText());
            } else {
                // Otherwise, disallow appending digits to PI and e.
                char lastChar = str.charAt(str.length() - 1);
                if ((lastChar != getString(R.string.e).charAt(0)) && (lastChar != getString(R.string.pi).charAt(0))) {
                    appendOutputTextAndScrollToBottom(senderButton.getText());
                }
            }
        } catch (Throwable t) {
            setErrorOutput(String.format(getString(R.string.error), t.getMessage()));
        }
    }

    /**
     * We use this method in order to remove last output line when it contains zeroes only and user is trying
     * to append a numeric value. Leading zeroes are redundant.
     */
    private void discardLastLineInOutput() {
        String str = outputText.getText().toString();
        if (!str.endsWith(System.lineSeparator())) {
            String[] lines = str.split(System.lineSeparator());
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < lines.length - 1; i++) {
                sb.append(lines[i]).append(System.lineSeparator());
            }
            outputText.setText(sb);
        }
    }

    /**
     * Raised when user presses operator (+,-,*,/) buttons, to update outputText text view accordingly.
     *
     * @param view Sender button
     */
    public void onOperatorButtonClicked(View view) {
        Log.d("Event", this.toString() + ".onOperatorButtonClicked");
        try {
            Button senderButton = (Button) view;

            String currOut = getLine();

            // Disallow appending several operators one by another, or after infinity results.
            char lastChar = currOut.charAt(currOut.length() - 1);
            if (Character.isDigit(lastChar) || (lastChar == getString(R.string.e).charAt(0)) || (lastChar == getString(R.string.pi).charAt(0))) {
                if (!action.isEmpty()) {
                    int lastActionIndex = currOut.lastIndexOf(action);
                    double lastVal = parseValue(currOut.substring(lastActionIndex + 1));
                    doAction(value, lastVal, action, value -> this.value = value);
                } else {
                    value = parseValue(currOut);
                }

                action = senderButton.getText().toString();

                // If user clicks an operator right after clicking equals, copy the last result
                if (outputText.getText().toString().endsWith(System.lineSeparator())) {
                    outputText.append(currOut);
                }

                appendOutputTextAndScrollToBottom(action);
            }
        } catch (Throwable t) {
            setErrorOutput(String.format(getString(R.string.error), t.getMessage()));
        }
    }

    /**
     * Raised when user presses the special function buttons (x^2,x^0.5,n!)
     *
     * @param view Sender button
     */
    public void onFunctionButtonClicked(View view) {
        Log.d("Event", this.toString() + ".onFunctionButtonClicked");
        try {
            String actionText = ((TextView) view).getText().toString();
            doFunction(actionText);
        } catch (Throwable t) {
            setErrorOutput(String.format(getString(R.string.error), t.getMessage()));
        }
    }

    /**
     * Handling a function request. This can be either special function or trigonometric one.<br/>
     * This is a separate method cause we get here from buttons (e.g. x^2) or dropdown list.
     *
     * @param functionText The function to execute.
     */
    public void doFunction(String functionText) {
        try {
            ActionType actionType = ActionType.valueOfActionText(functionText);

            // If there is already an action, calculate it before we can execute a function.
            if (!action.isEmpty()) {
                onEqualsButtonClicked(basicFragment.getView().findViewById(R.id.buttonEquals));
            }

            String currOut = getLine();
            if (!currOut.isEmpty()) {
                value = parseValue(currOut);

                // If user clicks an operator right after clicking equals, copy the last result
                if (!outputText.getText().toString().endsWith(System.lineSeparator())) {
                    outputText.append(System.lineSeparator());
                }

                outputText.append(actionType == ActionType.RAND ? actionType.getDescriptiveText() : String.format(actionType.getDescriptiveText(), getValueText(actionType == ActionType.FACTORIAL)));
            }

            // For functions, act as user pressed equals.
            doAction(value, 0, functionText, value -> {
                this.value = value;
                updateOutputValue();
                outputText.append(System.lineSeparator());
                action = "";
                this.value = 0;
            });
        } catch (Throwable t) {
            setErrorOutput(String.format(getString(R.string.error), t.getMessage()));
        }
    }

    /**
     * Execute an action based on operator or special function.
     *
     * @param value      The value to execute an action on
     * @param lastVal    For operators that take two arguments, this is the right argument
     * @param actionText Action text to execute. See {@link ActionType#valueOfActionText(String)}
     * @see ActionType
     */
    private void doAction(double value, double lastVal, String actionText, Consumer<Double> onResultReadyListener) {
        ActionType actionType = ActionType.valueOfActionText(actionText);
        ArithmeticAction action = actionType.newActionInstance();
        if (action == null) {
            Log.d("doAction", "Unknown action: " + actionText);
        } else {
            if (isServer) {
                Log.d("doAction", "Executing action in front of Server: " + actionType);
                CalculatorWebService.getInstance().executeCalculatorAction(value, lastVal, actionType, response -> {
                    double val = value;
                    if ((response == null) || response.getStatus() != 200) {
                        Log.e("doAction", "Error returned from server: " + (response != null ? response.getErrorMessage() : null));
                        if (response != null) {
                            Toast.makeText(MainActivity.this, response.getStatus() + " - " + response.getErrorMessage(), Toast.LENGTH_LONG).show();
                        }
                    } else {
                        val = parseValue(response.getValue());
                    }

                    onResultReadyListener.accept(handleCalculationResult(value, val, actionType));
                });
            } else {
                Log.d("doAction", "Executing action locally: " + actionType);
                double result = action.executeAsDouble(new ActionContext(lastVal, value));
                onResultReadyListener.accept(handleCalculationResult(value, result, actionType));
            }
        }
    }

    private double handleCalculationResult(double value, double result, ActionType actionType) {
        if (Double.isNaN(result)) {
            if (actionType == ActionType.DIVIDE) {
                setErrorOutput(getString(R.string.divideByZero));
            } else {
                setErrorOutput(String.format(getString(R.string.notEligibleForNegative), actionType.getActionText()));
                appendOutputTextAndScrollToBottom("Was: " + getValueText(value, false));
            }
            result = 0;
        } else if (Double.isFinite(result) && (result < 1)) {
            // Round until the 15th digit right to the floating point, in order to
            // round sin(pi) to 0, and not -1.2246467991473532E-16. (Because Math.PI keeps 16 digits only)
            result = Math.rint(1e15*result) / 1e15;
        }

        return result;
    }
}