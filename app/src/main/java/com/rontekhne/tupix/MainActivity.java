package com.rontekhne.tupix;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.method.ScrollingMovementMethod;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.util.Xml;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import java.io.IOException;
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

    public void search(View view) {
        // scroll to the top when search button is clicked
        textResult.scrollTo(0, 0);

        // hide keyboard
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null) {
            inputMethodManager.hideSoftInputFromWindow(textInput.getWindowToken(), 0);
        }

        searchWord = textInput.getText().toString().toLowerCase();

        SpannableStringBuilder resultText = new SpannableStringBuilder();

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
                                    break;
                                }
                                eventType = parser.next();
                            }

                            // format the lines
                            String fclazz = "\n\n" + clazz;
                            String fmeaning = "\n\n" + meaning;
                            String fsource = "\n\nFontes:\n" + source + "\n\n\n";

                            String resultEntry = word + fclazz + fmeaning + fsource;

                            // apply style to the lines
                            SpannableString formattedResultEntry = new SpannableString(resultEntry);
                            formattedResultEntry.setSpan(new RelativeSizeSpan(1.8f), 0, word.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                            formattedResultEntry.setSpan(new StyleSpan(Typeface.BOLD), 0, word.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                            int classStart = word.length();
                            int classEnd = classStart + fclazz.length();
                            formattedResultEntry.setSpan(new RelativeSizeSpan(1.2f), classStart, classEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                            formattedResultEntry.setSpan(new StyleSpan(Typeface.ITALIC), classStart, classEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                            int meaningStart = classEnd;
                            int meaningEnd = meaningStart + fmeaning.length();
                            formattedResultEntry.setSpan(new RelativeSizeSpan(1.2f), meaningStart, meaningEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                            int sourceStart = meaningEnd;
                            int sourceEnd = sourceStart + fsource.length();
                            formattedResultEntry.setSpan(new RelativeSizeSpan(0.8f), sourceStart, sourceEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                            resultText.append(formattedResultEntry);
                        }
                    }
                }
                eventType = parser.next();
            }

            if (resultText.length() > 0) {
                // show results in the TextView
                textResult.setText(resultText);
            } else { // word doesn't exit in the tupix.xml
                textResult.setText(R.string.palavra_n_o_encontrada_no_dicion_rio);
            }
        } catch (XmlPullParserException | IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Erro ao processar o arquivo XML.", Toast.LENGTH_SHORT).show();
        }

        // clean the input
        textInput.setText("");
    }
}
