import model.CacheLine;
import model.CacheMemory;
import model.MainMemory;
import static utils.Utils.*;

public class Main {

    public static void main(String[] args) {
        MainMemory mm = new MainMemory(64*KB);
        CacheMemory cm = new CacheMemory(32*BYTE);
        System.out.println(cm);
        for(CacheLine cl : cm.getCacheLines()){
            System.out.println(cl.toString());
        }
    }
}