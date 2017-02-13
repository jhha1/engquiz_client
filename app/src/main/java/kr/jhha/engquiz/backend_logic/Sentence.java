package kr.jhha.engquiz.backend_logic;

/**
 * Created by jhha on 2016-10-14.
 */

public class Sentence {
    public StringBuffer korean = new StringBuffer();
    public StringBuffer english = new StringBuffer();

    public static final String KOREAN = "korean";
    public static final String ENGLIST = "english";

    public Sentence() {
        ;
    }

    public Sentence(String korean, String english) {
        this.korean.append(korean);
        this.english.append(english);
    }

    public String toString() {
        return "ko:" + korean
                + ", en:" + english;
    }
}