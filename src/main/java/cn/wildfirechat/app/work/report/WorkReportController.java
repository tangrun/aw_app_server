package cn.wildfirechat.app.work.report;

import cn.wildfirechat.app.common.pojo.Page;
import cn.wildfirechat.app.work.report.enums.WorkReportType;
import cn.wildfirechat.app.work.report.pojo.InputWorkReportRequest;
import cn.wildfirechat.app.work.report.pojo.ReportListRequest;
import cn.wildfirechat.app.work.report.service.WorkReportService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;

/**
 * 汇报相关
 */
@Slf4j
@RestController
@RequestMapping("/work/report")
public class WorkReportController {

    @Resource
    private WorkReportService workReportService;

    @PostMapping("getReportInfoById")
    public Object getReportInfoById(Long reportId) {
        return workReportService.getReportInfoById(reportId);
    }

    @PostMapping("delete")
    @GetMapping("delete")
    public Object delete(Long reportId) {
        Subject subject = SecurityUtils.getSubject();
        String userId = (String) subject.getSession().getAttribute("userId");

        return workReportService.deleteReport(reportId, userId);
    }

    @PostMapping("createOrUpdateReport")
    public Object createOrUpdateReport(Long reportId, InputWorkReportRequest request, HttpServletRequest httpServletRequest) {
        Subject subject = SecurityUtils.getSubject();
        String userId = (String) subject.getSession().getAttribute("userId");

        return workReportService.createOrUpdateReport(userId, reportId, request);
    }

    @PostMapping(value = "getReceiveReportList")
    public Object getMyReceiverReportList(ReportListRequest request) {
        Subject subject = SecurityUtils.getSubject();
        String userId = (String) subject.getSession().getAttribute("userId");

        return workReportService.getReportList(request.getUsers(), Collections.singletonList(userId),
                WorkReportType.valueOfSafety(request.getType()),
                request.getBeginTime(), request.getEndTime()
                , request.convert2PageRequest());
    }


    @PostMapping("getMyReportList")
    public Object getReportList(ReportListRequest request) {
        Subject subject = SecurityUtils.getSubject();
        String userId = (String) subject.getSession().getAttribute("userId");
        return workReportService.getReportList(Collections.singletonList(userId), request.getUsers(),
                WorkReportType.valueOfSafety(request.getType()),
                request.getBeginTime(), request.getEndTime()
                , request.convert2PageRequest());
    }

}
