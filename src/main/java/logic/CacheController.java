package logic;

import utils.AddressDecoder;
import model.CacheMemory;
import model.MainMemory;

public class CacheController {
    private CacheMemory cacheMemory;
    private MainMemory mainMemory;
    private AddressDecoder addressDecoder;

    public CacheController(CacheMemory cacheMemory, MainMemory mainMemory) {
        this.cacheMemory = cacheMemory;
        this.mainMemory = mainMemory;
        this.addressDecoder = new AddressDecoder();
    }




}
