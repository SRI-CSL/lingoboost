package edu.northwestern.langlearn;

import android.content.Context;

import java.util.Arrays;
import java.util.Collections;

/**
 * Created by ttm on 4/19/17.
 */

public class WordList {
    private Context caller;

    // how many words were originally learned
    public static final int INITIAL_WORDS = 40;

    // how many new words to add with each test session
    public static final int NEW_WORDS = 5;

    public String[] englishWords;
    public String[] germanWords;
    public int length;

    public WordList(Context caller, int stage, boolean shuffled) {
        this.caller = caller;

        int endIndex = INITIAL_WORDS + NEW_WORDS * stage;
        String[] tempTranslations = caller.getResources().getStringArray(R.array.translations);
        if (endIndex >= tempTranslations.length) {
            endIndex = tempTranslations.length - 1;
        }

        String[] translations = Arrays.copyOfRange(tempTranslations, 0, endIndex);
        length = translations.length;

        if (shuffled) {
            Collections.shuffle(Arrays.asList(translations));
        }

        englishWords = new String[length];
        germanWords = new String[length];

        for (int i = 0; i < length; i++) {
            String[] splitup = translations[i].split(" ");
            germanWords[i] = splitup[0].replace("_", " ");
            englishWords[i] = splitup[1].replace("_", " ");
        }
    }
}
