package utils;

public class Utils {

    //each line in both cache and main memory has 4 bytes
    public static final int BLOCK_SIZE = 4;
    public static final int OFFSET_SIZE = 2;
    public static final int BYTE = 8;
    public static final int KB = 1024*1024;

    public static String initializeTag(int tagSize) {
        return "0".repeat(tagSize);
    }

    public static String initializeCacheLineData(int blockSize) {
        return "00000000".repeat(blockSize);
    }
    
    public static String initializeMainMemoryLineData(int blockSize) {
        return "00000000".repeat(blockSize);
    }
}