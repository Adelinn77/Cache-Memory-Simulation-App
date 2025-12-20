package utils;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class Utils {

    //each line in both cache and main memory has 4 bytes
    public static final int BLOCK_SIZE = 4;
    public static final int OFFSET_SIZE = 2;
    public static final int BYTE_SIZE = 8;
    public static final int B = 1;
    public static final int KB = 1024;
    public static final int MB = 1024*1024;
    public static final int PAGE_SIZE_BLOCKS = 256;

    public static String initializeTag(int tagSize) {
        return "0".repeat(tagSize);
    }

    public static String initializeCacheLineData(int blockSize) {
        return "00000000".repeat(blockSize);
    }

    public static String initializeMainMemoryLineData(int blockSize) {
        StringBuilder sb = new StringBuilder(blockSize * BYTE_SIZE);
        ThreadLocalRandom rnd = ThreadLocalRandom.current();

        for (int b = 0; b < blockSize; b++) {
            int value = rnd.nextInt(256);
            sb.append(String.format("%8s", Integer.toBinaryString(value)).replace(' ', '0'));
        }
        return sb.toString();
    }
    public static String generateRandomAddress(int addressSize) {
        Random random = new Random();
        StringBuilder address = new StringBuilder(addressSize);
        for (int i = 0; i < addressSize; i++) {
            address.append(random.nextBoolean() ? '1' : '0');
        }
        return address.toString();
    }
}