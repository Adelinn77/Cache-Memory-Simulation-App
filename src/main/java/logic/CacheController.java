package logic;

import model.CacheResult;
import model.CacheResultStatus;
import utils.AddressDecoder;
import model.CacheMemory;
import model.MainMemory;

import static utils.Utils.BYTE_SIZE;

public class CacheController {
    private CacheMemory cacheMemory;
    private MainMemory mainMemory;

    public CacheController(CacheMemory cacheMemory, MainMemory mainMemory) {
        this.cacheMemory = cacheMemory;
        this.mainMemory = mainMemory;
    }

    public String readDataFromAddress(String address) {
        String offset = AddressDecoder.extractOffset(address);
        String index = AddressDecoder.extractIndex(address, cacheMemory.getIndexSize());
        String tag = AddressDecoder.extractTag(address, cacheMemory.getTagSize());

        CacheResult cacheResult = cacheMemory.findInCache(index, offset, tag);
        if (cacheResult.getStatus().equals(CacheResultStatus.CACHE_HIT)) {
            return cacheResult.getData();
        }

        String dataBlock = mainMemory.loadFromMainMemory(tag, index, cacheMemory.getIndexSize());
        cacheMemory.writeBlockToCache(dataBlock, index, tag);

        int offsetInt = Integer.parseInt(offset, 2);
        return dataBlock.substring(offsetInt * BYTE_SIZE, offsetInt * BYTE_SIZE + BYTE_SIZE);
    }

    public void writeDataToAddress(String address, String data) {
        String offset = AddressDecoder.extractOffset(address);
        String index = AddressDecoder.extractIndex(address, cacheMemory.getIndexSize());
        String tag = AddressDecoder.extractTag(address, cacheMemory.getTagSize());

        CacheResult cacheResult = cacheMemory.findInCache(index, offset, tag);
        if (cacheResult.getStatus().equals(CacheResultStatus.CACHE_HIT)) {
            mainMemory.writeDataToMainMemoryLine(data, tag, index, offset, cacheMemory.getIndexSize());
            cacheMemory.writeDataToCacheLine(data, index, offset);
            return;
        }

        String dataBlock = mainMemory.writeDataToMainMemoryLine(data, tag, index, offset, cacheMemory.getIndexSize());
        cacheMemory.writeBlockToCache(dataBlock, index, tag);
    }
}
