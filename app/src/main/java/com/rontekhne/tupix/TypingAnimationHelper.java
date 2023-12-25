package com.rontekhne.tupix;

import android.os.Handler;
import android.os.Looper;
import android.text.SpannableStringBuilder;
import android.util.Log;
import android.widget.TextView;

/**
 * Animate the text if the target text is a SpannableStringBuilder
 */
public class TypingAnimationHelper {

    private static final int DELAY_MILLIS = 1;

    final private TextView textView;
    final private SpannableStringBuilder targetText;
    private int currentIndex;
    final private Handler handler;

    public TypingAnimationHelper(TextView textView, SpannableStringBuilder targetText) {
        this.textView = textView;
        this.targetText = targetText;
        this.currentIndex = 0;
        this.handler = new Handler(Looper.getMainLooper());
    }

    public void startTypingAnimation() {
        textView.setText(""); // clean the text of the current TextView
        currentIndex = 0;
        handler.postDelayed(typingRunnable, DELAY_MILLIS);
    }

    // letter by letter animation: slower
    final private Runnable typingRunnable = new Runnable() {
        @Override
        public void run() {
            if (currentIndex <= targetText.length()) {
                SpannableStringBuilder partialText = new SpannableStringBuilder(targetText.subSequence(0, currentIndex));
                textView.setText(partialText);
                currentIndex++;
                handler.postDelayed(this, DELAY_MILLIS);
            } else {
                // animation is finished
                Log.d("DEBUG", "Animation is finished");
            }
        }
    };

    // word by word animation: faster
    /* private Runnable typingRunnable = new Runnable() {
        @Override
        public void run() {
            if (currentIndex <= targetText.length()) {
                int nextSpaceIndex = targetText.toString().indexOf(' ', currentIndex);
                if (nextSpaceIndex == -1) {
                    nextSpaceIndex = targetText.length();
                }

                SpannableStringBuilder partialText = new SpannableStringBuilder(targetText.subSequence(0, nextSpaceIndex));
                textView.setText(partialText);
                currentIndex = nextSpaceIndex + 1; // go to the next word
                handler.postDelayed(this, DELAY_MILLIS);
            } else {
                // animation is finished
            }
        }
    };*/

    public void stopTypingAnimation() {
        handler.removeCallbacks(typingRunnable);
        textView.setText(targetText); // define the complete text
    }
}