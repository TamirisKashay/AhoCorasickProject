
import java.util.*;
import java.io.*;
import java.nio.file.*;

public class Main {

    private static final int RUNS = 5;
    private static final Random random = new Random(42);

    public static void main(String[] args) throws IOException {

        int[] patternCounts = {10, 100, 1000};
        int[] textLengths = {1000, 10000, 100000};

        Path resultsPath = Paths.get("results", "benchmark_results.csv");

        Files.createDirectories(resultsPath.getParent());

        try (PrintWriter writer = new PrintWriter(
                Files.newBufferedWriter(resultsPath))) {

            writer.println(
                    "Patterns,TextLength,Algorithm,Run1_ms,Run2_ms," +
                            "Run3_ms,Run4_ms,Run5_ms,Average_ms,TotalMatches"
            );

            System.out.println("========================================");
            System.out.println("       AHO-CORASICK vs KMP");
            System.out.println("========================================");

            for (int patternCount : patternCounts) {

                for (int textLength : textLengths) {

                    System.out.println("\nPatterns: " + patternCount
                            + " | Text length: " + textLength);

                    List<String> patterns =
                            generatePatterns(patternCount);

                    String text = generateText(textLength, patterns);
                    long[] ahoTimes = new long[RUNS];
                    int ahoMatches = 0;

                    for (int run = 0; run < RUNS; run++) {

                        long start = System.nanoTime();

                        AhoCorasick aho =
                                new AhoCorasick(patterns);

                        Map<String, List<Integer>> results =
                                aho.search(text);

                        long end = System.nanoTime();

                        ahoTimes[run] = end - start;

                        ahoMatches = countMatches(results);
                    }

                    double ahoAverage = averageTime(ahoTimes);

                    printResults("Aho-Corasick", ahoTimes, ahoAverage);

                    writeResults(writer, patternCount, textLength,
                            "Aho-Corasick", ahoTimes,
                            ahoAverage, ahoMatches);

                    long[] kmpTimes = new long[RUNS];
                    int kmpMatches = 0;

                    for (int run = 0; run < RUNS; run++) {

                        long start = System.nanoTime();

                        int totalMatches = 0;

                        for (String pattern : patterns) {

                            List<Integer> positions =
                                    KMP.search(text, pattern);

                            totalMatches += positions.size();
                        }

                        long end = System.nanoTime();

                        kmpTimes[run] = end - start;

                        kmpMatches = totalMatches;
                    }

                    double kmpAverage = averageTime(kmpTimes);

                    printResults("KMP", kmpTimes, kmpAverage);

                    writeResults(writer, patternCount, textLength,
                            "KMP", kmpTimes,
                            kmpAverage, kmpMatches);

                    writer.flush();

                    if (ahoMatches != kmpMatches) {
                        throw new RuntimeException(
                                "Results do not match!"
                        );
                    }

                    System.out.println("Match verification: PASSED");
                }
            }
        }

        System.out.println("\n========================================");
        System.out.println("Benchmark completed successfully!");
        System.out.println("Results saved to: " + resultsPath);
        System.out.println("========================================");
    }

    private static List<String> generatePatterns(int count) {

        Set<String> uniquePatterns = new LinkedHashSet<>();

        while (uniquePatterns.size() < count) {

            int length = 8 + random.nextInt(5);

            uniquePatterns.add(randomString(length));
        }

        return new ArrayList<>(uniquePatterns);
    }


    private static String generateText(
            int length, List<String> patterns) {

        char[] text = randomString(length).toCharArray();

        int insertCount = Math.min(
                patterns.size(),
                Math.max(1, length / 20)
        );

        int step = length / insertCount;

        for (int i = 0; i < insertCount; i++) {

            String pattern = patterns.get(i);

            int position = i * step;

            if (position + pattern.length() <= length) {

                for (int j = 0; j < pattern.length(); j++) {
                    text[position + j] = pattern.charAt(j);
                }
            }
        }

        return new String(text);
    }

    private static String randomString(int length) {

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < length; i++) {

            char ch = (char) ('a' + random.nextInt(26));

            sb.append(ch);
        }

        return sb.toString();
    }

    private static int countMatches(
            Map<String, List<Integer>> results) {

        int total = 0;

        for (List<Integer> positions : results.values()) {
            total += positions.size();
        }

        return total;
    }

    private static double averageTime(long[] times) {

        long total = 0;

        for (long time : times) {
            total += time;
        }

        return total / (double) times.length / 1_000_000.0;
    }

    private static void printResults(
            String algorithm,
            long[] times,
            double average) {

        System.out.println("\n" + algorithm);

        for (int i = 0; i < times.length; i++) {

            double milliseconds = times[i] / 1_000_000.0;

            System.out.printf(
                    Locale.US,
                    "Run %d: %.3f ms%n",
                    i + 1,
                    milliseconds
            );
        }

        System.out.printf(
                Locale.US,
                "Average: %.3f ms%n",
                average
        );
    }

    private static void writeResults(
            PrintWriter writer,
            int patternCount,
            int textLength,
            String algorithm,
            long[] times,
            double average,
            int matches) {

        writer.printf(
                Locale.US,
                "%d,%d,%s",
                patternCount,
                textLength,
                algorithm
        );

        for (long time : times) {

            writer.printf(
                    Locale.US,
                    ",%.3f",
                    time / 1_000_000.0
            );
        }

        writer.printf(
                Locale.US,
                ",%.3f,%d%n",
                average,
                matches
        );
    }
}
