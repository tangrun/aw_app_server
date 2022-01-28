package cn.wildfirechat.app.work.report.enums;


import java.util.Objects;

public enum  WorkReportType  {

    // 0、汇报；1、日报；2、周报；3、月报；4、年报
    Report(0,"Report","汇报"),
    Daily(1,"Daily","日报"),
    Week(2,"Week","周报"),
    Month(3,"Month","月报"),
    Year(4,"Year","年报"),

    ;
    int type;
    String name,desc;

    WorkReportType(int type, String name, String desc) {
        this.type = type;
        this.name = name;
        this.desc = desc;
    }

    public int getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public String getDesc() {
        return desc;
    }

    @Override
    public String toString() {
        return name;
    }


    public static WorkReportType valueOfSafety(String name) {
        for (WorkReportType value : values()) {
            if (Objects.equals(value.name, name))return value;
        }
        return null;
    }
}
