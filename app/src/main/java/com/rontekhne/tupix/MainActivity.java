package com.rontekhne.tupix;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private EditText textInput;
    private TextView textResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textInput = findViewById(R.id.textInput);
        textResult = findViewById(R.id.textResult);
        textResult.setMovementMethod(new android.text.method.ScrollingMovementMethod());
    }

    public void search(View view) {
        // scroll to the top when the search button is clicked
        textResult.scrollTo(0, 0);

        // hide keyboard
        android.view.inputmethod.InputMethodManager inputMethodManager = (android.view.inputmethod.InputMethodManager) getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null) {
            inputMethodManager.hideSoftInputFromWindow(textInput.getWindowToken(), 0);
        }

        // call the search method from TupixDictionary
        TupixDictionary.search(this, textInput.getText(), textResult);
    }

    public void showInfo(View view)
    {
        TupixDictionary.showInfo(this);
    }
}