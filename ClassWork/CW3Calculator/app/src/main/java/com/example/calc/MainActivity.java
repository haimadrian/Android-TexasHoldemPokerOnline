package com.example.calc;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    TextView outputText;
    private String action = "";
    private double value;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        outputText = findViewById(R.id.outputText);
        value = 0;
        updateOutputValue();
    }

    private String getLine() {
        String out = outputText.getText().toString().trim();
        int indexOfNewLine = out.lastIndexOf(System.lineSeparator());
        if (indexOfNewLine >= 0) {
            out = out.substring(indexOfNewLine + System.lineSeparator().length());
        }

        return out;
    }

    private void updateOutputValue() {
        if (getLine().length() > 0) {
            outputText.append(System.lineSeparator());
        }

        if (value == (long)value) {
            outputText.append(String.valueOf((long)value));
        } else {
            outputText.append(String.valueOf(value));
        }
    }

    public void funcNumber(View view) {
        Button senderButton = (Button)view;

        if (getLine().matches("0*")) {
            outputText.setText(senderButton.getText());
        } else {
            outputText.append(senderButton.getText());
        }
    }

    public void funcClear(View view) {
        outputText.setText("");
        action = "";
        value = 0;
        updateOutputValue();
    }

    public void funcOperator(View view) {
        Button senderButton = (Button)view;

        String currOut = getLine();
        if (!action.isEmpty()) {
            int lastActionIndex = currOut.lastIndexOf(action);
            double lastVal = Double.parseDouble(currOut.substring(lastActionIndex + 1));
            value = doAction(value, lastVal, action);
        } else {
            value = Double.parseDouble(currOut);
        }

        action = senderButton.getText().toString();

        // If user clicks an operator right after clicking equals, copy the last result
        if (outputText.getText().toString().endsWith(System.lineSeparator())) {
            outputText.append(currOut);
        }
        outputText.append(action);
    }

    public void funcEquals(View view) {
        String currOut = getLine();
        int lastActionIndex = currOut.lastIndexOf(action);
        double lastVal = Double.parseDouble(currOut.substring(lastActionIndex + 1));
        value = doAction(value, lastVal, action);
        updateOutputValue();
        outputText.append(System.lineSeparator());

        action = "";
        value = 0;
    }

    private double doAction(double value, double lastVal, String action) {
        double result = value;

        switch (action) {
            case "+": {
                result += lastVal;
                break;
            }
            case "-": {
                result -= lastVal;
                break;
            }
            case "*": {
                result *= lastVal;
                break;
            }
            case "/": {
                if (lastVal == 0) {
                    outputText.setText(R.string.divideByZero);
                    result = 0;
                } else {
                    result /= lastVal;
                }
                break;
            }
        }

        return result;
    }
}