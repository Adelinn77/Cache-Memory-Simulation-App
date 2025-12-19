package logic;

import model.CacheLine;
import model.CacheResult;
import model.CacheResultStatus;
import model.MemoryOperationResult;
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

    public MemoryOperationResult readDataFromAddress(String address) {
        String offset = AddressDecoder.extractOffset(address);
        String index = AddressDecoder.extractIndex(address, cacheMemory.getIndexSize());
        String tag = AddressDecoder.extractTag(address, cacheMemory.getTagSize());

        CacheResult cacheResult = cacheMemory.findInCache(index, offset, tag);
        int offsetInt = Integer.parseInt(offset, 2);
        int indexInt = Integer.parseInt(index, 2);
        long tagInt = Long.parseLong(tag, 2);
        long mainMemoryLineIndex = ((tagInt << cacheMemory.getIndexSize()) | indexInt);

        String dataBlock;
        String dataByte;
        if (cacheResult.getStatus().equals(CacheResultStatus.CACHE_HIT)) {
            CacheLine cacheLine = cacheMemory.getCacheLine(indexInt);
            dataBlock = cacheLine.getData();
            dataByte = cacheResult.getData();
        } else {
            dataBlock = mainMemory.loadFromMainMemory(tag, index, cacheMemory.getIndexSize());
            cacheMemory.writeBlockToCache(dataBlock, index, tag);
            dataByte = dataBlock.substring(offsetInt * BYTE_SIZE, offsetInt * BYTE_SIZE + BYTE_SIZE);
        }

        CacheLine cacheLine = cacheMemory.getCacheLine(indexInt);
        String mainMemoryBlock = mainMemory.loadFromMainMemory(tag, index, cacheMemory.getIndexSize());

        return new MemoryOperationResult(
                cacheResult.getStatus(),
                dataByte,
                cacheLine.getData(),
                cacheLine.getTag(),
                cacheLine.isValidBit(),
                mainMemoryBlock,
                indexInt,
                mainMemoryLineIndex
        );
    }

    public MemoryOperationResult writeDataToAddress(String address, String data) {
        String offset = AddressDecoder.extractOffset(address);
        String index = AddressDecoder.extractIndex(address, cacheMemory.getIndexSize());
        String tag = AddressDecoder.extractTag(address, cacheMemory.getTagSize());

        CacheResult cacheResult = cacheMemory.findInCache(index, offset, tag);
        int indexInt = Integer.parseInt(index, 2);
        long tagInt = Long.parseLong(tag, 2);
        long mainMemoryLineIndex = ((tagInt << cacheMemory.getIndexSize()) | indexInt);
        String mainMemoryBlock;
        if (cacheResult.getStatus().equals(CacheResultStatus.CACHE_HIT)) {
            mainMemoryBlock = mainMemory.writeDataByteToMainMemory(data, tag, index, offset, cacheMemory.getIndexSize());
            cacheMemory.writeDataByteToCache(data, index, offset);
        } else {
            mainMemoryBlock = mainMemory.writeDataByteToMainMemory(data, tag, index, offset, cacheMemory.getIndexSize());
            cacheMemory.writeBlockToCache(mainMemoryBlock, index, tag);
        }

        CacheLine cacheLine = cacheMemory.getCacheLine(indexInt);
        return new MemoryOperationResult(
                cacheResult.getStatus(),
                data,
                cacheLine.getData(),
                cacheLine.getTag(),
                cacheLine.isValidBit(),
                mainMemoryBlock,
                indexInt,
                mainMemoryLineIndex
        );
    }
}
