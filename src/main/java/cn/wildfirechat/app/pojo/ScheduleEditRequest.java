package cn.wildfirechat.app.pojo;

import lombok.Data;

import java.text.SimpleDateFormat;
import java.util.Date;

@Data
public class ScheduleEditRequest {

    //String targetId;
    //Boolean disable;

    /// <summary>
    /// 主键ID
    /// </summary>
    public long id;

    //标题
    public String title;

    //是自来源于课表（0-自定义 非0，则为user_child_timetable的id）
    public long timetable_id;

    //开始提醒日期
    public String tips_start_datetime;

    //结束提醒日期
    public String tips_end_datetime;

    //是否全天
    public int is_full_day;

    //是否重复提示
    //0-不提醒、1-准点提醒、2-提前10分钟、3-提前30分钟
    //4-提前1小时、5-提前2小时、6-提前1天、7-提前3天
    public int repeat_setting;

    //重复提醒的时间
    //0-不重复、1-每天、2-每周、3-每月、4-每年、5-工作日
    public int repeat_time;

    /// <summary>
    /// 类型 0、日程；1、记事；2、纪念日
    /// </summary>
    public int type;

    /// <summary>
    /// 重要等级
    /// </summary>
    public int grade;

    /// <summary>
    /// 标签
    /// </summary>
    public String tag;

    /// <summary>
    /// 是否公开
    /// </summary>
    public int is_open;

    /// <summary>
    /// 地点
    /// </summary>
    public String address;

    /// <summary>
    /// 备注信息
    /// </summary>
    public String remark;

    /// <summary>
    /// 图片
    /// </summary>
    public String pic1;

    /// <summary>
    /// 图片
    /// </summary>
    public String pic2;

    /// <summary>
    /// 图片
    /// </summary>
    public String pic3;

    /// <summary>
    /// 修改类型 0全部修改、1 修改此次、2 修改此次及以后
    /// </summary>
    public int edit_type;

    public String time;


    /**
     * 获取现在时间
     *
     * @return返回字符串格式 yyyy-MM-dd HH:mm:ss
     */
    public String getStartDate() {
        Date currentTime = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateString = formatter.format(tips_start_datetime);
        return dateString;
    }

}
