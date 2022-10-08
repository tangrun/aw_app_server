package cn.wildfirechat.app.work.schedule.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

import javax.annotation.Nullable;
import javax.persistence.Column;
import java.io.Serializable;
import java.util.Date;

@Data
@Builder
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleInfo implements Serializable {

    private Long id;

    private String user_id;

    private String title;

    private String tips_start_datetime;

    private String tips_end_datetime;

    private int is_full_day;

    private int repeat_setting;

    private int repeat_time;

    private int type;;

    private int grade;

    private String tag;

    private int is_open;

    private String address;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private Date create_time;

    private String remark;

    private String pic1;

    private String pic2;

    private String pic3;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private Date end_time;

    private int is_finish;

    private Long forkId;

    private String operatorId;

}
