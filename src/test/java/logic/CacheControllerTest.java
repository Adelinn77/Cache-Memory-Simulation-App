package logic;

import model.CacheLine;
import model.CacheMemory;
import model.MainMemory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static utils.Utils.*;

class CacheControllerTest {

    private CacheMemory cacheMemory;
    private MainMemory mainMemory;
    private CacheController cacheController;
    private String tag;
    private String index;
    private String offset;
    private String address;

    @BeforeEach
    void setUp() throws Exception {
        mainMemory = new MainMemory(64 * KB);
        cacheMemory = new CacheMemory(32 * B);

        cacheMemory.setMainMemoryAddressSize(mainMemory.calculateAddressSize());
        cacheMemory.buildCacheMemory();
        mainMemory.setTagSize(cacheMemory.getTagSize());

        cacheController = new CacheController(cacheMemory, mainMemory);

        tag = "10101010101"; // 11 bits
        index = "011"; // 3 bits
        offset = "01"; // 2 bits
        address = tag + index + offset; // 16-bit address
    }

    @Test
    void readDataFromAddressLoadsBlockOnMissAndReturnsHitAfterwards() {
        String firstRead = cacheController.readDataFromAddress(address);
        assertEquals("00000000", firstRead);

        CacheLine cacheLine = cacheMemory.getCacheLines().get(Integer.parseInt(index, 2));
        assertTrue(cacheLine.isValidBit());

        String secondRead = cacheController.readDataFromAddress(address);
        assertEquals(firstRead, secondRead);
    }

    @Test
    void writeDataToAddressUpdatesMainMemoryAndCache() {
        String dataByte = "10101010";

        cacheController.writeDataToAddress(address, dataByte);

        CacheLine cacheLine = cacheMemory.getCacheLines().get(Integer.parseInt(index, 2));
        assertEquals(dataByte, cacheLine.getByte(Integer.parseInt(offset, 2)));

        long tagInt = Long.parseLong(tag, 2);
        int indexInt = Integer.parseInt(index, 2);
        int addressInt = (int) ((tagInt << cacheMemory.getIndexSize()) | indexInt);
        String updatedMainMemoryLine = mainMemory.getMainMemoryLines().get(addressInt);

        String expectedLine = "00000000" + dataByte + "00000000" + "00000000";
        assertEquals(expectedLine, updatedMainMemoryLine);

        String readBack = cacheController.readDataFromAddress(address);
        assertEquals(dataByte, readBack);
    }
}