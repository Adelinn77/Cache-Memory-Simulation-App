import exceptions.MainMemoryAddressSizeNotSet;
import model.CacheLine;
import model.CacheMemory;
import model.MainMemory;
import utils.AddressDecoder;

import static utils.Utils.*;
import static utils.AddressDecoder.*;

public class Main {

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


        /// Demo for extracting all the fields from an address requested by the CPU
        String testAddress = generateRandomAddress(mm.calculateAddressSize());
        System.out.println(testAddress);

        String offsetAddress = AddressDecoder.extractOffset(testAddress);
        System.out.println(offsetAddress);

        String indexAddress = AddressDecoder.extractIndex(testAddress, cm.getIndexSize());
        System.out.println(indexAddress);

        String tagAddress = AddressDecoder.extractTag(testAddress, cm.getTagSize());
        System.out.println(tagAddress);


    }
}