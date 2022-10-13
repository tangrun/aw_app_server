package cn.wildfirechat.app.work.report.pojo;

import cn.wildfirechat.app.common.pojo.Page;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import java.util.Date;
import java.util.List;

@Data
public class ReportListRequest extends Page {
    List<String> users;
    String type;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    Date beginTime;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    Date endTime;
}
