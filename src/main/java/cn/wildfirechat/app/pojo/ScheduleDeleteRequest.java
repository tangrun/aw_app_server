package cn.wildfirechat.app.pojo;

import lombok.Data;

/**
 * 删除日程
 */
@Data
public class ScheduleDeleteRequest {

    public long id;
    public int type;
    public int repeat_time;
    public String time;

}
