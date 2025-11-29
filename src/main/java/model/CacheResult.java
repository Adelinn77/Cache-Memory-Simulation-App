package model;

public class CacheResult {
    private CacheResultStatus status;
    private String data; //byte to be returned if CacheResultStatus is HIT

    public CacheResult (CacheResultStatus status, String data) {
        this.status = status;
        this.data = data;
    }

    public String getData() {
        return data;
    }

    public CacheResultStatus getStatus() {
        return status;
    }

    public void setData(String data) {
        this.data = data;
    }

    public void setStatus(CacheResultStatus status) {
        this.status = status;
    }
}
