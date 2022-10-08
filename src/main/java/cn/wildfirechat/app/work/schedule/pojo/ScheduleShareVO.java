package cn.wildfirechat.app.work.schedule.pojo;

import cn.wildfirechat.app.work.schedule.ScheduleEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Date;
import java.util.List;

@Data
@Builder
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleShareVO {
    Long id;

    String userId;

    int type;

    List<Long> ids;

    List<ScheduleEntity> scheduleInfoList;

    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    Date createTime;
}
