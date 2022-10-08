package cn.wildfirechat.app.work.report.pojo;

import cn.wildfirechat.app.common.pojo.IdNamePojo;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
public class InputWorkReportRequest {


    public String jobContent;

    public String jobPlan;

    public String otherMatters;

    /**
     * @see cn.wildfirechat.app.work.report.enums.WorkReportType#name()
     */
    public String type;

    public List<MultipartFile> attachment;

    public List<String> attachmentOld;

    /**
     * json对象字符串 key -value id-name
     */
    public List<IdNamePojo> reportTo;

    /**
     * json数组字符串 id
     */
    public List<IdNamePojo> sendTo;

}
