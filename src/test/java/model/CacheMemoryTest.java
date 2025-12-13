package model;

import exceptions.MainMemoryAddressSizeNotSet;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static utils.Utils.B;
import static utils.Utils.BLOCK_SIZE;
import static utils.Utils.initializeCacheLineData;

class CacheMemoryTest {

    @Test
    void buildCacheMemoryThrowsWhenAddressSizeMissing() {
        CacheMemory cacheMemory = new CacheMemory(32 * B);

        assertThrows(MainMemoryAddressSizeNotSet.class, cacheMemory::buildCacheMemory);
    }

    @Test
    void buildCacheMemoryInitializesLinesWithDefaults() throws MainMemoryAddressSizeNotSet {
        CacheMemory cacheMemory = new CacheMemory(32 * B);
        cacheMemory.setMainMemoryAddressSize(16);

        cacheMemory.buildCacheMemory();

        assertEquals(8, cacheMemory.getCacheLines().size());
        assertEquals(11, cacheMemory.getTagSize());

        CacheLine firstLine = cacheMemory.getCacheLines().get(0);
        assertFalse(firstLine.isValidBit());
        assertEquals("0".repeat(cacheMemory.getTagSize()), firstLine.getTag());
        assertEquals(initializeCacheLineData(BLOCK_SIZE), firstLine.getData());
    }

    @Test
    void findInCacheReturnsHitAfterWritingBlock() throws MainMemoryAddressSizeNotSet {
        CacheMemory cacheMemory = new CacheMemory(32 * B);
        cacheMemory.setMainMemoryAddressSize(16);
        cacheMemory.buildCacheMemory();

        String index = "010";
        String offset = "01";
        String tag = "10101010101";

        CacheResult initialResult = cacheMemory.findInCache(index, offset, tag);
        assertEquals(CacheResultStatus.CACHE_MISS, initialResult.getStatus());

        String dataBlock = "11110000".repeat(BLOCK_SIZE);
        cacheMemory.writeBlockToCache(dataBlock, index, tag);

        CacheResult hitResult = cacheMemory.findInCache(index, offset, tag);
        assertEquals(CacheResultStatus.CACHE_HIT, hitResult.getStatus());
        assertEquals("11110000", hitResult.getData());
    }
}