package com.signbridge.model;

import java.util.List;

public class QuizQuestion {
    private String questionText;
    private SignItem targetSign;
    private List<String> options;
    private int correctIndex;
    private boolean isSignToWord; // true: show sign, guess word; false: show word, guess sign
    private String explanation;

    public QuizQuestion(String questionText, SignItem targetSign, List<String> options, int correctIndex, boolean isSignToWord, String explanation) {
        this.questionText = questionText;
        this.targetSign = targetSign;
        this.options = options;
        this.correctIndex = correctIndex;
        this.isSignToWord = isSignToWord;
        this.explanation = explanation;
    }

    public String getQuestionText() { return questionText; }
    public SignItem getTargetSign() { return targetSign; }
    public List<String> getOptions() { return options; }
    public int getCorrectIndex() { return correctIndex; }
    public boolean isSignToWord() { return isSignToWord; }
    public String getExplanation() { return explanation; }

    public boolean isCorrect(int selectedIndex) {
        return selectedIndex == correctIndex;
    }
}
