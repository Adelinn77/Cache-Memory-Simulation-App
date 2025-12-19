package model;

import java.util.ArrayList;
import java.util.List;

import static utils.Utils.*;

public class MainMemory {
    private long mainMemorySizeInBytes;
    private List<String> mainMemoryLines  = new ArrayList<>();
    private long noLines;
    private int tagSize = -1;
    public MainMemory() {}

    public MainMemory(long mainMemorySizeInBytes) {
        this.mainMemorySizeInBytes = mainMemorySizeInBytes;
        this.noLines = mainMemorySizeInBytes/BLOCK_SIZE;
        for (int i = 0; i < noLines; i++) {
            mainMemoryLines.add(initializeMainMemoryLineData(BLOCK_SIZE));
        }
    }

    public void setMainMemoryLines(List<String> mainMemoryLines) {
        this.mainMemoryLines = mainMemoryLines;
    }

    public void setMainMemorySizeInBytes(int mainMemorySizeInBytes) {
        this.mainMemorySizeInBytes = mainMemorySizeInBytes;
    }

    public void setNoLines(int noLines) {
        this.noLines = noLines;
    }

    public void setTagSize(int tagSize) {
        this.tagSize = tagSize;
    }

    public List<String> getMainMemoryLines() {
        return mainMemoryLines;
    }

    public long getMainMemorySizeInBytes() {
        return mainMemorySizeInBytes;
    }

    public long getNoLines() {
        return noLines;
    }

    public int getTagSize() {
        return tagSize;
    }

    public int calculateAddressSize() {
        return (int)  (Math.log(this.mainMemorySizeInBytes) / Math.log(2));
    }

    public String loadFromMainMemory(String tag, String index, int indexSize) {
        long tagInt = Long.parseLong(tag, 2);
        int indexInt = Integer.parseInt(index, 2);
        long addressInt = ((tagInt << indexSize) | indexInt);

        return mainMemoryLines.get((int) addressInt);
    }

    public String writeDataByteToMainMemory(String data, String tag, String index, String offset, int indexSize) {
        long tagInt = Long.parseLong(tag, 2);
        int indexInt = Integer.parseInt(index, 2);
        int offsetInt  = Integer.parseInt(offset, 2);
        long addressInt = ((tagInt << indexSize) | indexInt);

        String newData =  mainMemoryLines.get((int) addressInt);

        int start = offsetInt * BYTE_SIZE;
        int end   = start + BYTE_SIZE;

        newData = newData.substring(0, start)
                + data
                + newData.substring(end);

        mainMemoryLines.set((int) addressInt, newData);

        return newData;
    }

    public int getSizeInKB() {
        return (int) (mainMemorySizeInBytes/KB);
    }

    @Override
    public String toString() {
        return "MainMemory {" +
                "mainMemorySize = " + mainMemorySizeInBytes/KB + "KB" +
                ", noLines = " + noLines +
                ", tagSize = " + tagSize +
                '}';
    }
}
