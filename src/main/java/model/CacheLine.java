package model;
import utils.Utils;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static utils.Utils.*;

public class CacheLine {

    private String data;
    private boolean validBit;
    private String tag;

    public CacheLine() {}

    public CacheLine(int tagSize) {
        this.data =  Utils.initializeCacheLineData(BLOCK_SIZE);
        this.validBit = false;
        this.tag = Utils.initializeTag(tagSize);
    }

    public void setData(String data) {
        this.data = data;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }


    public void setValidBit(boolean validBit) {
        this.validBit = validBit;
    }

    public String getData() {
        return data;
    }

    public String getTag() {
        return tag;
    }

    public boolean isValidBit() {
        return validBit;
    }

    public String getByte(int offset) {
        return data.substring(offset*BYTE_SIZE, offset*BYTE_SIZE+BYTE_SIZE);
    }

    @Override
    public String toString() {
        String dataGrouped = IntStream.range(0, data.length() / BYTE_SIZE)
                .mapToObj(i -> data.substring(i * BYTE_SIZE, (i+1) * BYTE_SIZE))
                .collect(Collectors.joining("_"));

        return
                "data = " +  dataGrouped +
                ", validBit = " + validBit +
                ", tag = " + tag +
                '\n';
    }
}
