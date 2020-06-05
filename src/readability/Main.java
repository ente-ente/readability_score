package readability;

import jdk.jshell.spi.ExecutionControl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Scanner;

abstract class Score {
    private final int words;
    private final int sentences;
    private final int characters;
    private final int syllables;
    private final int polysyllables;
    private final double score;
    private final int approximateAge;

    public Score(int words, int sentences, int characters, int syllables, int polysyllables) {
        this.words = words;
        this.sentences = sentences;
        this.characters = characters;
        this.syllables = syllables;
        this.polysyllables = polysyllables;
        this.score = calculateScore();
        this.approximateAge = calculateApproximateAge();
    }

    private int calculateApproximateAge() {
        int[] approximateAges = {6, 7, 9, 10, 11,
                12, 13, 14, 15, 16, 17, 18, 24, 25};
        return approximateAges[(int) Math.round(this.getScore()) - 1];
    }

    protected abstract String getName();

    protected int getWords() {
        return this.words;
    }

    protected int getSentences() {
        return this.sentences;
    }

    protected int getCharacters() {
        return this.characters;
    }

    protected int getSyllables() {
        return this.syllables;
    }

    protected int getPolysyllables() {
        return this.polysyllables;
    }

    protected double getScore() {
        return this.score;
    }

    protected abstract double calculateScore();

    public int getApproximateAge() {
        return this.approximateAge;
    }

    private double roundToTwoDecimals(double number) {
        return Math.round(number * 100.0) / 100.0;
    }

    public String toString() {
        return this.getName() +
                ": " + roundToTwoDecimals(this.getScore()) + " (about " + this.getApproximateAge() + " year olds).";
    }
}

class AutomatedReadabilityIndex extends Score {

    public AutomatedReadabilityIndex(int words, int sentences, int characters) {
        super(words, sentences, characters, 0, 0);
    }

    protected String getName() {
        return "Automated Readability Index";
    }

    protected double calculateScore() {
        return 4.71 * getCharacters() / getWords() + 0.5 * getWords() / getSentences() - 21.43;
    }
}

class FleschKincaidReadabilityTests extends Score {

    public FleschKincaidReadabilityTests(int words, int sentences, int characters, int syllables, int polysyllables) {
        super(words, sentences, characters, syllables, polysyllables);
    }

    protected String getName() {
        return "Flesch–Kincaid readability tests";
    }

    protected double calculateScore() {
        return 0.39 * getWords() / getSentences() + 11.8 * getSyllables() / getWords() - 15.59;
    }
}

class SimpleMeasureOfGobbledygook extends Score {

    public SimpleMeasureOfGobbledygook(int words, int sentences, int characters, int syllables, int polysyllables) {
        super(words, sentences, characters, syllables, polysyllables);
    }

    protected String getName() {
        return "Simple Measure of Gobbledygook";
    }

    protected double calculateScore() {
        return 1.043 * Math.sqrt(getPolysyllables() * 30.0 / getSentences()) + 3.1291;
    }
}

class ColemanLiauIndex extends Score {

    public ColemanLiauIndex(int words, int sentences, int characters, int syllables, int polysyllables) {
        super(words, sentences, characters, syllables, polysyllables);
    }

    protected String getName() {
        return "Coleman–Liau index";
    }

    protected double calculateScore() {
        return 0.0588 * getCharacters() / getWords() * 100 - 0.296 * getSentences() / getWords() * 100 - 15.8;
    }
}

public class Main {
    private static String getInputString(String path) throws IOException {
        return new String(Files.readAllBytes(Paths.get(path)));
    }

    private static int countSyllables(String word) {
        final String vowels = "aeiouyAEIOUY";
        int vowelsCount = 0;
        for (int i = 0; i < word.length(); i++) {
            if (vowels.indexOf(word.charAt(i)) == -1) continue;
            if (i == 0 || vowels.indexOf(word.charAt(i - 1)) == -1) {
                if (i < word.length() - 1 || word.charAt(i) != 'e') vowelsCount++;
            }
        }
        return vowelsCount == 0 ? 1 : vowelsCount;
    }

    public static void main(String[] args) {
        String text;
        try {
            text = getInputString(args[0]);
        } catch (IOException e) {
            System.out.println("Couldn't read file: " + e.getMessage());
            return;
        }
        String[] wordList = text.split(" ");
        int syllables = 0;
        int polysyllables = 0;
        for (String word : wordList) {
            word = word.replaceAll("[^a-zA-Z0-9]", "");
            if (word.isEmpty()) {
                continue;
            }
            int syllablesCount = countSyllables(word);
            syllables += syllablesCount;
            if (syllablesCount > 2) {
                polysyllables++;
            }
        }
        int words = wordList.length;
        int sentences = text.split("[^\\s][.!?]\\s").length;
        int characters = (text.replaceAll("\\s+", "")).length();
        System.out.println("Words: " + words);
        System.out.println("Sentences: " + sentences);
        System.out.println("Characters: " + characters);
        System.out.println("Syllables: " + syllables);
        System.out.println("Polysyllables: " + polysyllables);
        System.out.println("Enter the score you want to calculate (ARI, FK, SMOG, CL, all): ");

        Scanner scanner = new Scanner(System.in);
        String formula = scanner.next();
        try {
            switch (formula) {
                case "ARI":
                    System.out.println(new AutomatedReadabilityIndex(words, sentences, characters));
                    break;
                case "FK":
                    System.out.println(
                            new FleschKincaidReadabilityTests(words, sentences, characters, syllables, polysyllables));
                    break;
                case "SMOG":
                    System.out.println(
                            new SimpleMeasureOfGobbledygook(words, sentences, characters, syllables, polysyllables));
                    break;
                case "CL":
                    System.out.println(new ColemanLiauIndex(words, sentences, characters, syllables, polysyllables));
                    break;
                case "all":
                    Score[] scores = {
                            new AutomatedReadabilityIndex(words, sentences, characters),
                            new FleschKincaidReadabilityTests(words, sentences, characters, syllables, polysyllables),
                            new SimpleMeasureOfGobbledygook(words, sentences, characters, syllables, polysyllables),
                            new ColemanLiauIndex(words, sentences, characters, syllables, polysyllables)
                    };
                    double sumOfAges = 0;
                    for (Score score : scores) {
                        System.out.println(score);
                        sumOfAges += score.getApproximateAge();
                    }
                    System.out.println("\nThis text should be understood in average by " +
                            Math.round(sumOfAges / 4 * 100.0) / 100.0 + " year olds.");
                    break;
                default:
                    throw new UnsupportedOperationException("Not implemented, yet.");
            }

        } catch (UnsupportedOperationException e){
            System.out.println(formula + " : " + e.getMessage());
            return;
        }


    }
}
