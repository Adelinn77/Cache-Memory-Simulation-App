package exceptions;

public class MainMemoryAddressSizeNotSet extends RuntimeException {
    public MainMemoryAddressSizeNotSet() {
        super("Main memory address size not set.");
    }

    public MainMemoryAddressSizeNotSet(String message) {
        super(message);
    }
}
