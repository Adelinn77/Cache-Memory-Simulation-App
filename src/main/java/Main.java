import exceptions.MainMemoryAddressSizeNotSet;
import model.CacheLine;
import model.CacheMemory;
import model.MainMemory;
import static utils.Utils.*;

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

    }
}