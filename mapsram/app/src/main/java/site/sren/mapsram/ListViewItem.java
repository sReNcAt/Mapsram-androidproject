package site.sren.mapsram;

public class ListViewItem {
    // 아이템 타입을 구분하기 위한 type 변수.
    private int type ;

    private String work;
    private String memo;
    private int alramType;
    private int hour;
    private int minutes;
    private Double latitude;
    private Double logitude;

    public Double getLogitude() {
        return logitude;
    }

    public void setLogitude(Double logitude) {
        this.logitude = logitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public int getMinutes() {
        return minutes;
    }

    public void setMinutes(int minutes) {
        this.minutes = minutes;
    }

    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }

    public String getWork() {
        return work;
    }

    public void setWork(String work) {
        this.work = work;
    }

    public int getAlramType() {
        return alramType;
    }

    public void setAlramType(int alramType) {
        this.alramType = alramType;
    }
}