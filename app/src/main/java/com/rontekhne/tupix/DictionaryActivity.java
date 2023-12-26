package com.rontekhne.tupix;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

public class DictionaryActivity extends AppCompatActivity {

    private ImageButton backButton;
    private EditText textInput;
    private TextView textResult;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dictionary);

        textInput = findViewById(R.id.textInput);
        textResult = findViewById(R.id.textResult);
        backButton = findViewById(R.id.backButton);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(DictionaryActivity.this, MainActivity.class));
            }
        });
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