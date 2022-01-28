package cn.wildfirechat.app.work.report;

import cn.wildfirechat.app.RestResult;
import cn.wildfirechat.app.common.pojo.Page;
import cn.wildfirechat.app.tools.ShortUUIDGenerator;
import cn.wildfirechat.app.work.report.entity.WorkReportEntry;
import cn.wildfirechat.app.work.report.entity.WorkReportToEntry;
import cn.wildfirechat.app.work.report.enums.WorkReportType;
import cn.wildfirechat.app.work.report.pojo.InputWorkReportRequest;
import cn.wildfirechat.app.work.report.repository.WorkReportRepository;
import cn.wildfirechat.app.work.report.repository.WorkReportTempRepository;
import cn.wildfirechat.app.work.report.service.WorkReportService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Nullable;
import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 汇报相关
 */
@OpenAPIDefinition(
        info = @Info(
                title = "Swagger3",
                version = "1.0",
                description = "Swagger3使用演示",
                contact = @Contact(name = "TOM")
        ),
        security = @SecurityRequirement(name = "JWT")
)
@Slf4j
@RestController
@RequestMapping("/work/report")
public class WorkReportController {

    @Resource
    private WorkReportService workReportService;

    @GetMapping("delete")
    public Object delete(Long reportId) {
        Subject subject = SecurityUtils.getSubject();
        String userId = (String) subject.getSession().getAttribute("userId");

        return workReportService.deleteReport(reportId, userId);
    }

    @PostMapping("createOrUpdateReport")
    public Object createOrUpdateReport(Long reportId, InputWorkReportRequest request) {
        Subject subject = SecurityUtils.getSubject();
        String userId = (String) subject.getSession().getAttribute("userId");

        return workReportService.createOrUpdateReport(userId, reportId, request);
    }

    /**
     * @param type
     * @param beginTime
     * @param endTime
     * @return
     * @see WorkReportType
     */
    @PostMapping("getReceiveReportList")
    public Object getMyReceiverReportList(@RequestParam(defaultValue = "") String type, @DateTimeFormat(pattern = "yyyy-MM-dd") Date beginTime, @DateTimeFormat(pattern = "yyyy-MM-dd") Date endTime, Page page) {
        Subject subject = SecurityUtils.getSubject();
        String userId = (String) subject.getSession().getAttribute("userId");

        return workReportService.getReportList(null, userId, WorkReportType.valueOfSafety(type), beginTime, endTime, page);
    }


    @Operation(
            summary = "获取我的列表",
            description = "dddddd"
    )
    @PostMapping("getMyReportList")
    public Object getReportList(@RequestParam(defaultValue = "") String type, @DateTimeFormat(pattern = "yyyy-MM-dd") Date beginTime, @DateTimeFormat(pattern = "yyyy-MM-dd") Date endTime, Page page) {
        Subject subject = SecurityUtils.getSubject();
        String userId = (String) subject.getSession().getAttribute("userId");

        return workReportService.getReportList(userId, null, WorkReportType.valueOfSafety(type), beginTime, endTime, page);
    }

}
