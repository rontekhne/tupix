package com.rontekhne.tupix;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Color;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.util.Xml;
import android.view.Gravity;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;

public class TupixDictionary {

    public static void search(Context context, Editable inputText, TextView resultTextView) {
        String searchWord = inputText.toString().toLowerCase();

        SpannableStringBuilder resultText = new SpannableStringBuilder();

        try {
            // read the XML file
            InputStream is = context.getResources().openRawResource(R.raw.tupix);
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
                            formattedResultEntry.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, word.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                            int classStart = word.length();
                            int classEnd = classStart + fclazz.length();
                            formattedResultEntry.setSpan(new RelativeSizeSpan(1.2f), classStart, classEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                            formattedResultEntry.setSpan(new StyleSpan(android.graphics.Typeface.ITALIC), classStart, classEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

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
                resultTextView.setText(resultText);
            } else { // word doesn't exist in the tupix.xml
                resultTextView.setText(":( Palavra não encontrada no dicionário!");
            }
        } catch (XmlPullParserException | IOException e) {
            e.printStackTrace();
            Toast.makeText(context, "Erro ao processar o arquivo XML.", Toast.LENGTH_SHORT).show();
        }

        // clean the input
        inputText.clear();
    }

    public static void showInfo(Context context)
    {
        // Instantiate AlertDialog
        AlertDialog.Builder dialog = new AlertDialog.Builder(context);

        // set title and message
        dialog.setTitle("Tupix");
        String message = "\nDicionário de Tupi Antigo - Português\n" +
                "\nA língua tupi, ou tupi antigo, foi a língua falada pelos povos tupis e por grande parte dos colonizadores que povoavam o litoral do Brasil nos séculos XVI e XVII. \"É a língua indígena clássica do Brasil e a que teve mais importância na construção espiritual e cultural do país.\n" +
                "\nAviso:\n" +
                "Todas as fontes são citadas nos verbetes. Todos os direitos intelectuais são reservados ao Professor Eduardo de Almeida Navarro, cujo currículo Lattes se encontra em http://lattes.cnpq.br/4076981549961926\n\n" +
                "Privacidade:\n" +
                "ESTE APP NÃO POSSUI ACESSO À INTERNET, NÃO COMERCIALIZA, NÃO EXIGE LOGIN E NÃO COLETA DADOS USUÁRIOS!\n\n" +
                "\nCriado e mantido por RonTekhne.\n" +
                "https://rontekhne.github.io";
        dialog.setMessage(message);

        // set cancel
        dialog.setCancelable(false);

        // set icon
        dialog.setIcon(R.drawable.tupix_icon_small);

        // set action for close button
        dialog.setNegativeButton("x", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // create a layout to the Toast that contains a ImageView and a TextView
                LinearLayout toastLayout = new LinearLayout(context);
                toastLayout.setOrientation(LinearLayout.HORIZONTAL);
                toastLayout.setGravity(Gravity.CENTER);

                // Add the ImageView to the layout
                ImageView imagem = new ImageView(context);
                imagem.setImageResource(R.drawable.tupix_icon_small);
                imagem.setAdjustViewBounds(true);
                imagem.setMaxWidth(100);
                toastLayout.addView(imagem);

                // add the TextView to the layout
                TextView text = new TextView(context);
                text.setBackgroundResource(R.color.transparentBackground);
                text.setTextColor(0xFF66894F);
                text.setText("Obrigado por usar o dicionário.\nAvalie-nos na Play Store!");
                text.setPadding(0, 0, 0, 0);

                toastLayout.setBackgroundResource(R.color.transparentBackground);
                toastLayout.addView(text);
                toastLayout.setPadding(0, 5, 20, 5);

                // create the Toast
                Toast toast = new Toast(context);
                toast.setDuration(Toast.LENGTH_LONG);
                toast.setView(toastLayout);
                toast.show();
            }
        });

        // create AlertDialog to change 'x button' color
        final AlertDialog alertDialog = dialog.create();

        // Set OnShowListener for the AlertDialog
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                int colorId = R.color.colorAccent;
                Resources.Theme theme = context.getTheme();

                // change 'x' color
                Button negativeButton = alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
                negativeButton.setTextColor(context.getResources().getColor(colorId, theme));
            }
        });

        alertDialog.show();
    }
}