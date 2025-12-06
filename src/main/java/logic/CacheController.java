package logic;

import model.CacheResult;
import model.CacheResultStatus;
import utils.AddressDecoder;
import model.CacheMemory;
import model.MainMemory;
import utils.Utils.*;

import static utils.Utils.BYTE_SIZE;
import static utils.Utils.OFFSET_SIZE;

public class CacheController {
    private CacheMemory cacheMemory;
    private MainMemory mainMemory;

    public CacheController(CacheMemory cacheMemory, MainMemory mainMemory) {
        this.cacheMemory = cacheMemory;
        this.mainMemory = mainMemory;
    }

    public String accessAddress(String address) {
        String offset = AddressDecoder.extractOffset(address);
        String index = AddressDecoder.extractIndex(address, cacheMemory.getIndexSize());
        String tag = AddressDecoder.extractTag(address, cacheMemory.getTagSize());

        CacheResult cacheResult = cacheMemory.findInCache(index, offset, tag);
        if (cacheResult.getStatus().equals(CacheResultStatus.CACHE_HIT)) {
            return cacheResult.getData();
        }

        String data = mainMemory.loadFromMainMemory(tag, index, cacheMemory.getIndexSize());
        cacheMemory.writeBlockToCache(data, index, tag);

        int offsetInt = Integer.parseInt(offset, 2);
        return data.substring(offsetInt * BYTE_SIZE, offsetInt * BYTE_SIZE + BYTE_SIZE);
    }
}
