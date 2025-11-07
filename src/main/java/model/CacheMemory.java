package model;

import java.util.ArrayList;
import java.util.List;
import utils.Utils;
import java.math.*;

import static utils.Utils.*;

public class CacheMemory {

    private int cacheSize;
    private List<CacheLine> cacheLines  = new ArrayList<>();
    private int noLines;
    private int indexSize;
    private int tagSize;

    public CacheMemory() {}

    public CacheMemory(int size) {
        this.cacheSize = size/BYTE;
        this.noLines = this.cacheSize/BLOCK_SIZE;
        this.indexSize = (int) (Math.log(noLines) / Math.log(2));
        this.tagSize = BLOCK_SIZE*BYTE - this.indexSize - OFFSET_SIZE;
        for (int i = 0; i < noLines; i++) {
            cacheLines.add(new CacheLine(tagSize));
        }
    }

    public List<CacheLine> getCacheLines() {
        return cacheLines;
    }

    public int getCacheSize() {
        return cacheSize;
    }

    public int getNoLines() {
        return noLines;
    }

    public void setCacheLines(List<CacheLine> cacheLines) {
        this.cacheLines = cacheLines;
    }

    public void setCacheSize(int cacheSize) {
        this.cacheSize = cacheSize;
    }

    public int getTagSize() {
        return tagSize;
    }

    public void setTagSize(int tagSize) {
        this.tagSize = tagSize;
    }

    public void setNoLines(int noLines) {
        this.noLines = noLines;
    }

    public int getIndexSize() {
        return indexSize;
    }

    public void setIndexSize(int indexSize) {
        this.indexSize = indexSize;
    }

    @Override
    public String toString() {
        return "CacheMemory{" +
                "cacheSize=" + cacheSize +
                ", noLines=" + noLines +
                ", indexSize=" + indexSize +
                ", tagSize=" + tagSize +
                '}';
    }
}
