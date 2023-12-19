package com.rontekhne.tupix;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Xml;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import org.xmlpull.v1.XmlPullParser;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    private EditText textInput;
    private TextView textResult;

    private String searchWord;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textInput = findViewById(R.id.textInput);

        textResult = findViewById(R.id.textResult);
        textResult.setMovementMethod(new ScrollingMovementMethod());
    }

    // adicionar estilo à saída
    public void search(View view) {
        // hide keyboard
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null) {
            inputMethodManager.hideSoftInputFromWindow(textInput.getWindowToken(), 0);
        }

        searchWord = textInput.getText().toString().toLowerCase();

        StringBuilder resultText = new StringBuilder();

        try {
            // read the XML file
            InputStream is = getResources().openRawResource(R.raw.tupix);
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(is, null);

            // Parse the XML
            int eventType = parser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    String tagName = parser.getName();
                    if ("word".equals(tagName)) {
                        String word = parser.nextText().trim().toLowerCase();
                        if (word.equalsIgnoreCase(searchWord)) {
                            // word found, get info
                            String clazz = "";
                            String meaning = "";
                            String source = "";

                            while (eventType != XmlPullParser.END_DOCUMENT) {
                                if (eventType == XmlPullParser.START_TAG) {
                                    String innerTagName = parser.getName();
                                    switch (innerTagName) {
                                        case "class":
                                            clazz = parser.nextText().trim();
                                            break;
                                        case "meaning":
                                            meaning = parser.nextText().trim();
                                            break;
                                        case "source":
                                            source = parser.nextText().trim();
                                            break;
                                    }
                                } else if (eventType == XmlPullParser.END_TAG &&
                                        ("a".equals(parser.getName()) ||
                                                "b".equals(parser.getName()))) {
                                    // end of tags , get out of inner loop
                                    // ** add all letters here manually or in a loop
                                    break;
                                }
                                eventType = parser.next();
                            }

                            // add to the string of results
                            String resultEntry = "Palavra: " + word +
                                    "\nClasse: " + clazz +
                                    "\nSignificado: " + meaning +
                                    "\nFontes: " + source + "\n\n";
                            resultText.append(resultEntry);
                        }
                    }
                }
                eventType = parser.next();
            }

            if (resultText.length() > 0) {
                // show results in the TextView
                // resultText.insert(0, "Resultados para " + searchWord + ":\n");
                textResult.setText(resultText.toString());
            } else {
                textResult.setText("Palavra não encontrada no dicionário.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // clean the input
        textInput.setText("");
    }
}
