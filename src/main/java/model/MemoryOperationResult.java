package model;

public class MemoryOperationResult {
    private final CacheResultStatus status;
    private final String dataByte;
    private final String cacheLineData;
    private final String cacheLineTag;
    private final boolean cacheLineValid;
    private final String mainMemoryBlock;
    private final int cacheLineIndex;
    private final long mainMemoryLineIndex;

    public MemoryOperationResult(CacheResultStatus status,
                                 String dataByte,
                                 String cacheLineData,
                                 String cacheLineTag,
                                 boolean cacheLineValid,
                                 String mainMemoryBlock,
                                 int cacheLineIndex,
                                 long mainMemoryLineIndex) {
        this.status = status;
        this.dataByte = dataByte;
        this.cacheLineData = cacheLineData;
        this.cacheLineTag = cacheLineTag;
        this.cacheLineValid = cacheLineValid;
        this.mainMemoryBlock = mainMemoryBlock;
        this.cacheLineIndex = cacheLineIndex;
        this.mainMemoryLineIndex = mainMemoryLineIndex;
    }

    public CacheResultStatus getStatus() {
        return status;
    }

    public String getDataByte() {
        return dataByte;
    }

    public String getCacheLineData() {
        return cacheLineData;
    }

    public String getCacheLineTag() {
        return cacheLineTag;
    }

    public boolean isCacheLineValid() {
        return cacheLineValid;
    }

    public String getMainMemoryBlock() {
        return mainMemoryBlock;
    }

    public int getCacheLineIndex() {
        return cacheLineIndex;
    }

    public long getMainMemoryLineIndex() {
        return mainMemoryLineIndex;
    }
}
