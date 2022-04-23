package cn.wildfirechat.app.work.report.pojo;

import cn.wildfirechat.app.common.pojo.IdName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Data
public class VOWorkReport {

    public Long id;

    public String userId;

    public String jobContent;

    public String jobPlan;

    public String otherMatters;

    public String type;

    public String attachment;

    public List<String> attachmentList;

    public Map<String,String> reportTo;

    public List<IdName> reportToList;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    public Date createTime;


}
