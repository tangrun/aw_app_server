package cn.wildfirechat.app.work.report.pojo;

import lombok.Data;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Nullable;
import javax.persistence.Column;
import java.util.List;
import java.util.Map;

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
    /**
     * json对象字符串 key -value id-name
     */
    public String reportTo;

    /**
     * json数组字符串 id
     */
    public String sendTo;

}
