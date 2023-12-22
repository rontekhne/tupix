package com.rontekhne.tupix;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.util.Log;
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
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;

public class TupixDictionary {

    private static TypingAnimationHelper typingAnimationHelper;


    // search a word in tupix.xml and return a textview
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
                            SpannableStringBuilder meaning = null;
                            String source = "";

                            while (eventType != XmlPullParser.END_DOCUMENT) {
                                if (eventType == XmlPullParser.START_TAG) {
                                    String innerTagName = parser.getName();
                                    switch (innerTagName) {
                                        case "class":
                                            clazz = parser.nextText().trim();
                                            break;
                                        case "meaning":
                                            meaning = parseMeaningWithItalicTp(parser);
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
                            String fsource = "\n\nFontes:\n" + source + "\n\n\n";

                            // apply style to the lines
                            SpannableStringBuilder formattedResultEntry = new SpannableStringBuilder(word);
                            formattedResultEntry.setSpan(new RelativeSizeSpan(2.2f), 0, word.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                            formattedResultEntry.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, word.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                            int classStart = word.length();
                            int classEnd = classStart + fclazz.length();
                            formattedResultEntry.append(fclazz);
                            formattedResultEntry.setSpan(new RelativeSizeSpan(1.4f), classStart, classEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                            formattedResultEntry.setSpan(new StyleSpan(android.graphics.Typeface.ITALIC), classStart, classEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                            if (meaning != null) {
                                formattedResultEntry.append(meaning);
                            }

                            int sourceStart = formattedResultEntry.length();
                            int sourceEnd = sourceStart + fsource.length();
                            formattedResultEntry.append(fsource);
                            formattedResultEntry.setSpan(new RelativeSizeSpan(0.8f), sourceStart, sourceEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                            resultText.append(formattedResultEntry);
                        }
                    }
                }
                eventType = parser.next();
            }


            // animate text view
            if (resultText.length() > 0) {
                if (typingAnimationHelper != null) {
                    typingAnimationHelper.stopTypingAnimation();
                }

                typingAnimationHelper = new TypingAnimationHelper(resultTextView, resultText);
                typingAnimationHelper.startTypingAnimation();
            } else {
                if (typingAnimationHelper != null) {
                    typingAnimationHelper.stopTypingAnimation();
                }resultTextView.setText(R.string.word_not_found);
            }

        } catch (XmlPullParserException | IOException e) {
            e.printStackTrace();
            Toast.makeText(
                    context,
                    "Erro ao processar o arquivo XML. ",
                    Toast.LENGTH_LONG).show();
        }

        // clean the input
        inputText.clear();
    }

    // parse the meaning tag to appply italics and bold on <tp> tag contents
    private static SpannableStringBuilder parseMeaningWithItalicTp(XmlPullParser parser) throws IOException, XmlPullParserException {
        SpannableStringBuilder meaningBuilder = new SpannableStringBuilder();

        int eventType = parser.next();
        //boolean isFirstText = true;  // Flag to track the start of <meaning>

        meaningBuilder.append("\n\n"); // add newlines in the beginning of the meaning

        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.TEXT) {
                meaningBuilder.append(parser.getText());
            } else if (eventType == XmlPullParser.START_TAG) {
                String innerTagName = parser.getName();
                switch (innerTagName) {
                    case "tp":
                        String tpContent = parser.nextText().trim();
                        // apply italics and bold only inside the content of <tp>
                        SpannableString italicAndBoldSpan = new SpannableString(tpContent);
                        italicAndBoldSpan.setSpan(new StyleSpan(Typeface.ITALIC), 0, tpContent.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        italicAndBoldSpan.setSpan(new StyleSpan(Typeface.BOLD), 0, tpContent.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        meaningBuilder.append(italicAndBoldSpan);
                        break;
                }

            } else if (eventType == XmlPullParser.END_TAG && "meaning".equals(parser.getName())) {
                // end of tag <meaning>, get out of loop
                break;
            }
            eventType = parser.next();
        }

        // apply 1.5f font size to meaning
        meaningBuilder.setSpan(new RelativeSizeSpan(1.4f), 0, meaningBuilder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return meaningBuilder;
    }

    public static void showInfo(Context context)
    {
        // Instantiate AlertDialog
        AlertDialog.Builder dialog = new AlertDialog.Builder(context);

        // set title and message
        dialog.setTitle("Tupix");
        String message = "\nDicionário de Tupi Antigo - Português\n" +
                "\nA língua tupi, ou tupi antigo, foi a língua falada pelos povos tupis e por grande parte dos colonizadores que povoavam o litoral do Brasil nos séculos XVI e XVII. É a língua indígena clássica do Brasil e a que teve mais importância na construção espiritual e cultural do país.\n" +
                "\nAviso:\n" +
                "Todas as fontes são citadas nos verbetes. Todos os direitos intelectuais são reservados ao Professor Eduardo de Almeida Navarro, cujo currículo Lattes se encontra em http://lattes.cnpq.br/4076981549961926\n\n" +
                "Privacidade:\n" +
                "ESTE APP NÃO POSSUI ACESSO À INTERNET, NÃO COMERCIALIZA, NÃO EXIGE LOGIN E NÃO COLETA DADOS DOS USUÁRIOS!\n\n" +
                "\nCriado e mantido por RonTekhne.\n" +
                "https://rontekhne.github.io";
        dialog.setMessage(message);

        // set cancel
        dialog.setCancelable(false);

        // set icon
        dialog.setIcon(R.drawable.tupix_icon_small);

        // set action for close button
        dialog.setNegativeButton("FECHAR", (dialog1, which) -> {
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
        });

        // create AlertDialog to change 'x button' color
        final AlertDialog alertDialog = dialog.create();

        // Set OnShowListener for the AlertDialog
        //noinspection Convert2Lambda
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