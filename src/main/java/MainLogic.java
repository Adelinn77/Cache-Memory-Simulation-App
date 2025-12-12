import exceptions.MainMemoryAddressSizeNotSet;
import logic.CacheController;
import model.CacheMemory;
import model.MainMemory;
import utils.AddressDecoder;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static utils.Utils.*;

public class MainLogic {

    public static void main(String[] args) {
        MainMemory mm = new MainMemory(64*KB);
        CacheMemory cm = new CacheMemory(32*B);

        cm.setMainMemoryAddressSize(mm.calculateAddressSize());

        try {
            cm.buildCacheMemory();
            mm.setTagSize(cm.getTagSize());
        }
        catch (MainMemoryAddressSizeNotSet e) {
            System.out.println(e.getMessage());
        }

        System.out.println(mm);
        System.out.println(cm);


        // Demo for extracting all the fields from an address requested by the CPU
        String testAddress = generateRandomAddress(mm.calculateAddressSize());
        String addressGrouped = IntStream.range(0, testAddress.length() / 4)
                .mapToObj(i -> testAddress.substring(i * 4, (i+1) * 4))
                .collect(Collectors.joining("_"));
        System.out.println("The requested address from the CPU is:\n" + addressGrouped);

        String offsetAddress = AddressDecoder.extractOffset(testAddress);
        System.out.println("Offset is: " + offsetAddress);

        String indexAddress = AddressDecoder.extractIndex(testAddress, cm.getIndexSize());
        System.out.println("Index is: " + indexAddress);

        String tagAddress = AddressDecoder.extractTag(testAddress, cm.getTagSize());
        System.out.println("Tag is: " + tagAddress);

        CacheController cacheController = new CacheController(cm, mm);
        String byteReturned1 = cacheController.readDataFromAddress(testAddress);
        String byteReturned2 = cacheController.readDataFromAddress(testAddress);

        System.out.println("byteReturned1: " + byteReturned1);
        System.out.println("byteReturned2: " + byteReturned2);
    }
}