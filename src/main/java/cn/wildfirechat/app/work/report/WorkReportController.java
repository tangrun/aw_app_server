package cn.wildfirechat.app.work.report;

import cn.wildfirechat.app.common.pojo.Page;
import cn.wildfirechat.app.work.report.enums.WorkReportType;
import cn.wildfirechat.app.work.report.pojo.InputWorkReportRequest;
import cn.wildfirechat.app.work.report.service.WorkReportService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Calendar;
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

    /**
     * @param type
     * @param beginTime
     * @param endTime
     * @return
     * @see WorkReportType
     */
    @PostMapping(value = "getReceiveReportList")
    public Object getMyReceiverReportList(@RequestParam(defaultValue = "") String type, @DateTimeFormat(pattern = "yyyy-MM-dd") Date beginTime, @DateTimeFormat(pattern = "yyyy-MM-dd") Date endTime, Page page) {
        Subject subject = SecurityUtils.getSubject();
        String userId = (String) subject.getSession().getAttribute("userId");

        if (beginTime != null) beginTime = DateUtils.ceiling(beginTime, Calendar.DAY_OF_MONTH);
        if (endTime != null) endTime = DateUtils.ceiling(endTime, Calendar.DAY_OF_MONTH);

        return workReportService.getReportList(null, userId, WorkReportType.valueOfSafety(type), beginTime, endTime, page);
    }


    @PostMapping("getMyReportList")
    public Object getReportList(@RequestParam(defaultValue = "") String type, @DateTimeFormat(pattern = "yyyy-MM-dd") Date beginTime, @DateTimeFormat(pattern = "yyyy-MM-dd") Date endTime, Page page) {
        Subject subject = SecurityUtils.getSubject();
        String userId = (String) subject.getSession().getAttribute("userId");

        if (beginTime != null) beginTime = DateUtils.ceiling(beginTime, Calendar.DAY_OF_MONTH);
        if (endTime != null) endTime = DateUtils.ceiling(endTime, Calendar.DAY_OF_MONTH);

        return workReportService.getReportList(userId, null, WorkReportType.valueOfSafety(type), beginTime, endTime, page);
    }

}
