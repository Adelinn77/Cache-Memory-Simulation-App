package model;

import exceptions.MainMemoryAddressSizeNotSet;

import java.util.ArrayList;
import java.util.List;

import static utils.Utils.*;

public class CacheMemory {

    private int cacheSizeInBytes;
    private List<CacheLine> cacheLines  = new ArrayList<>();
    private int noLines;
    private int indexSize;
    private int tagSize;
    private int mainMemoryAddressSize = -1;

    public CacheMemory() {}

    public CacheMemory(int size) {
        this.cacheSizeInBytes = size;
        this.noLines = this.cacheSizeInBytes / BLOCK_SIZE;
        this.indexSize = (int) (Math.log(noLines) / Math.log(2));
    }

    public void buildCacheMemory() throws  MainMemoryAddressSizeNotSet{
        if(this.mainMemoryAddressSize == -1)
            throw  new MainMemoryAddressSizeNotSet("Main memory address size must be set before building cache.");
        this.tagSize = this.mainMemoryAddressSize - this.indexSize - OFFSET_SIZE;
        for (int i = 0; i < noLines; i++) {
            cacheLines.add(new CacheLine(tagSize));
        }
    }

    public CacheResult findInCache(String index, String offset, String tag) {
        int indexInt = Integer.parseInt(index, 2);
        int offsetInt = Integer.parseInt(offset, 2);

        CacheLine cacheLine = cacheLines.get(indexInt);
        if (!cacheLine.isValidBit()) {
            return new CacheResult(CacheResultStatus.CACHE_MISS, "Valid bit is 0!");
        }

        if (!tag.equals(cacheLine.getTag())){
            return new CacheResult(CacheResultStatus.CACHE_MISS, "Tag fields do not match!");
        }

        String byteReturned = cacheLine.getByte(offsetInt);
        return new CacheResult(CacheResultStatus.CACHE_HIT, byteReturned);
    }

    public void writeBlockToCache(String dataBlock, String index, String tag) {
        int indexInt = Integer.parseInt(index, 2);
        CacheLine cl = cacheLines.get(indexInt);
        cl.setData(dataBlock);
        cl.setTag(tag);
        cl.setValidBit(true);
    }

    public void writeDataToCacheLine(String data, String index, String offset) {
        int indexInt = Integer.parseInt(index, 2);
        int offsetInt = Integer.parseInt(offset, 2);

        CacheLine cl = cacheLines.get(indexInt);
        String newData = cl.getData();

        int start = offsetInt * BYTE_SIZE;
        int end   = start + BYTE_SIZE;

        newData = newData.substring(0, start)
                + data
                + newData.substring(end);

        cl.setData(newData);
    }


    public List<CacheLine> getCacheLines() {
        return cacheLines;
    }

    public int getCacheSizeInBytes() {
        return cacheSizeInBytes;
    }

    public int getNoLines() {
        return noLines;
    }

    public void setCacheLines(List<CacheLine> cacheLines) {
        this.cacheLines = cacheLines;
    }

    public void setCacheSizeInBytes(int cacheSizeInBytes) {
        this.cacheSizeInBytes = cacheSizeInBytes;
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

    public void setMainMemoryAddressSize(int mainMemoryAddressSize) {
        this.mainMemoryAddressSize = mainMemoryAddressSize;
    }

    @Override
    public String toString() {
        return "CacheMemory {" +
                "cacheSize = " + cacheSizeInBytes + "B" +
                ", noLines = " + noLines +
                ", indexSize = " + indexSize +
                ", tagSize = " + tagSize +
                "}" +
                "\nCache Lines:\n" + cacheLines.toString();
    }
}
