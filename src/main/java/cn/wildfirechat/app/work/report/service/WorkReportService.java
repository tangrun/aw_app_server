package cn.wildfirechat.app.work.report.service;

import cn.wildfirechat.app.RestResult;
import cn.wildfirechat.app.admin.AdminService;
import cn.wildfirechat.app.common.CommonService;
import cn.wildfirechat.app.common.entity.UploadFile;
import cn.wildfirechat.app.common.pojo.IdNamePojo;
import cn.wildfirechat.app.common.pojo.Page;
import cn.wildfirechat.app.work.report.entity.WorkReportEntry;
import cn.wildfirechat.app.work.report.entity.WorkReportToEntry;
import cn.wildfirechat.app.work.report.enums.WorkReportType;
import cn.wildfirechat.app.work.report.pojo.InputWorkReportRequest;
import cn.wildfirechat.app.work.report.pojo.ReportListRequest;
import cn.wildfirechat.app.work.report.pojo.VOWorkReport;
import cn.wildfirechat.app.work.report.repository.WorkReportRepository;
import cn.wildfirechat.app.work.report.repository.WorkReportTempRepository;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Nullable;
import javax.annotation.Resource;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class WorkReportService {

    @Resource
    WorkReportRepository workReportRepository;

    @Resource
    WorkReportTempRepository workReportTempRepository;

    @Resource
    AdminService adminService;

    @Resource
    CommonService commonService;

    public RestResult getReportInfoById(Long reportId) {
        Assert.notNull(reportId, "id不能为空");
        WorkReportEntry entry = workReportRepository.findById(reportId).orElse(null);
        if (entry == null) return RestResult.error(RestResult.RestCode.ERROR_NOT_EXIST);
        return RestResult.ok(convert2VO(entry));
    }

    @Transactional
    public RestResult deleteReport(Long reportId, String userId) {
        Assert.notNull(reportId, "id不能为空");
        WorkReportEntry workReportEntry = workReportRepository.findById(reportId).orElse(null);

        Assert.notNull(workReportEntry, "对象不存在");
        Assert.isTrue(Objects.equals(workReportEntry.getUserId(), userId), "您无权操作");

        workReportRepository.deleteById(reportId);
        workReportTempRepository.deleteAllByReportId(reportId);
        return RestResult.ok(null);
    }

    /**
     * @param userId
     * @param reportId
     * @param request
     * @return
     */
    @Transactional
    public RestResult createOrUpdateReport(String userId, @Nullable Long reportId, InputWorkReportRequest request) {
        WorkReportEntry workReportEntry = reportId == null ? new WorkReportEntry() : workReportRepository.findById(reportId).orElse(null);

        Assert.notNull(workReportEntry, "对象不存在");
        workReportEntry.setUserId(userId);
        BeanUtils.copyProperties(request, workReportEntry);

        WorkReportType workReportType = WorkReportType.valueOfSafety(request.getType());
        Assert.notNull(workReportType, "类型不能为空");
        workReportEntry.setType(workReportType);
        // 附件处理
        Stream<String> newAttachments = ObjectUtils.defaultIfNull(request.getAttachment(), new ArrayList<MultipartFile>())
                .stream()
                .map((v) -> {
                    if (v != null && v.getSize() > 0) {
                        UploadFile uploadFile = commonService.uploadFile(userId, v);
                        return commonService.getDownloadPath(uploadFile);
                    }
                    return null;
                })
                .filter((v) -> !Objects.isNull(v));
        List<String> finalAttachments = Stream
                .concat(ObjectUtils.defaultIfNull(request.getAttachmentOld(), new ArrayList<String>()).stream(), newAttachments)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toList());
        workReportEntry.setAttachment(StringUtils.join(finalAttachments, ","));

        //
        workReportRepository.saveAndFlush(workReportEntry);

        // 修改时 删除之前的汇报人
        if (reportId != null)
            workReportTempRepository.deleteAllByReportId(reportId);
        List<WorkReportToEntry> list = request.getReportTo().stream()
                .map((idName) -> {
                    return WorkReportToEntry.builder()
                            .reportId(workReportEntry.getId())
                            .targetId(idName.getId())
                            .targetName(idName.getName())
                            .build();
                }).collect(Collectors.toList());
        workReportTempRepository.saveAll(list);

        VOWorkReport voWorkReport = convert2VO(workReportEntry);

        // 发送的群
        for (IdNamePojo idName : request.getSendTo()) {
            adminService.sendWorkReportMessage(userId, idName.getId(), true, voWorkReport);
        }

        // 通知汇报人
        if (request.getNotify() == Boolean.TRUE) {
            for (IdNamePojo idNamePojo : request.getReportTo()) {
                adminService.sendWorkReportMessage(userId, idNamePojo.getId(), false, voWorkReport);
            }
        }

        return RestResult.ok(null);
    }


    /**
     * @param userId    提交人id
     * @param targetId  发送给的对象id
     * @param type
     * @param beginTime
     * @param endTime
     * @return
     */
    public RestResult getReportList(List<String> userIds, List<String> reportUserIds,
                                    WorkReportType type,
                                    Date beginTime, Date endTime, PageRequest pageRequest
    ) {
        if (userIds!=null && userIds.isEmpty())
            userIds = null;
        if (reportUserIds!=null && reportUserIds.isEmpty())
            reportUserIds = null;
        if (beginTime != null) beginTime = DateUtils.truncate(beginTime, Calendar.DAY_OF_MONTH);
        if (endTime != null) endTime = DateUtils.ceiling(endTime, Calendar.DAY_OF_MONTH);
        if (pageRequest==null) {
            pageRequest = PageRequest.of(0, 12);
        }

        List<VOWorkReport> list = workReportRepository.getReportList2(
                        userIds,
                        reportUserIds,
                        type,
                        beginTime,
                        endTime,
                        pageRequest)
                .stream().map(new Function<WorkReportEntry, VOWorkReport>() {
                    @Override
                    public VOWorkReport apply(WorkReportEntry entry) {
                        return convert2VO(entry);
                    }
                }).collect(Collectors.toList());

        return RestResult.ok(list);
    }


    private VOWorkReport convert2VO(WorkReportEntry entry) {
        VOWorkReport voWorkReport = new VOWorkReport();

        BeanUtils.copyProperties(entry, voWorkReport);
        voWorkReport.setType(entry.getType().toString());

        List<WorkReportToEntry> all = workReportTempRepository.findAllByReportId(entry.getId());

        Map<String, String> map = new HashMap<>();
        List<IdNamePojo> list1 = new ArrayList<>();

        for (WorkReportToEntry toEntry : all) {
            map.put(toEntry.getTargetId(), toEntry.getTargetName());
            list1.add(new IdNamePojo(toEntry.getTargetId(), toEntry.getTargetName()));
        }

        voWorkReport.setReportTo(map);
        voWorkReport.setReportToList(list1);

        if (StringUtils.isNotBlank(entry.getAttachment())) {
            voWorkReport.setAttachmentList(Arrays.asList(entry.getAttachment().split(",")));
        }

        return voWorkReport;
    }

}
