package model;

import java.util.ArrayList;
import java.util.List;

import static utils.Utils.*;

public class MainMemory {
    //size is expressed in bytes
    private int mainMemorySize;
    private List<String> mainMemoryLines  = new ArrayList<>();
    private int noLines;
    private int tagSize;


    public MainMemory() {}

    public MainMemory(int mainMemorySize) {
        this.mainMemorySize = mainMemorySize;
        this.noLines = mainMemorySize/BLOCK_SIZE;
        for (int i = 0; i < noLines; i++) {
            mainMemoryLines.add(initializeMainMemoryLineData(BLOCK_SIZE));
        }
    }


    public void setMainMemoryLines(List<String> mainMemoryLines) {
        this.mainMemoryLines = mainMemoryLines;
    }

    public void setMainMemorySize(int mainMemorySize) {
        this.mainMemorySize = mainMemorySize;
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

    public int getMainMemorySize() {
        return mainMemorySize;
    }

    public int getNoLines() {
        return noLines;
    }

    public int getTagSize() {
        return tagSize;
    }
}
