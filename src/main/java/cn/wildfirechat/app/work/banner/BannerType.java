package cn.wildfirechat.app.work.banner;

public enum BannerType {

    workbench("workbench",1,"工作台")

    ;

    private final String value;
    private final int type;
    private final String desc;

    BannerType(String value, int type, String desc) {
        this.value = value;
        this.type = type;
        this.desc = desc;
    }

    public String getValue() {
        return value;
    }

    public int getType() {
        return type;
    }

    public String getDesc() {
        return desc;
    }

    @Override
    public String toString() {
        return value;
    }
}
