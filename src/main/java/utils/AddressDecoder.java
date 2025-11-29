package utils;

import java.util.Random;

import static utils.Utils.OFFSET_SIZE;

public class AddressDecoder {

    public AddressDecoder() {}

    public static String extractOffset(String address) {
        return address.substring(address.length() - OFFSET_SIZE);
    }

    public static String extractIndex(String address, int indexSize) {
        return address.substring(address.length() - OFFSET_SIZE - indexSize, address.length() - OFFSET_SIZE);
    }

    public static String extractTag(String address, int tagSize) {
        return address.substring(0, tagSize);
    }



}
