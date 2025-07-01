package org.llmgdfuzz.tools;
import java.util.Random;

public class Normal_Distribution {

    public static int generateBiasedRandom(Random random, int target) {
        double stdDev = 5.0;
        int value;
        do {
            value = (int) Math.round(random.nextGaussian() * stdDev + target);
        } while (value < 0);
        return value;
    }

    public static int generateBiasedRandomr(Random random, int target, int min, int max) {

        int range = max - min + 1;

        if (range <= 3) {
            return min + random.nextInt(range);
        }

        double stdDev = 5.0;
        int value;

        do {
            value = (int) Math.round(random.nextGaussian() * stdDev + target);
        } while (value < min || value > max);
        return value;
    }

    public static int generateBiasedRandomsd(Random random, int target, int min, int max ,double dev) {

        int range = max - min + 1;

        if (range <= 3) {
            return min + random.nextInt(range);
        }

        int value;

        do {
            value = (int) Math.round(random.nextGaussian() * dev + target);
        } while (value < min || value > max);
        return value;
    }
}
