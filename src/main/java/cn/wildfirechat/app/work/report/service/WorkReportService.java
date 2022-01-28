package cn.wildfirechat.app.work.report.service;

import cn.wildfirechat.app.RestResult;
import cn.wildfirechat.app.admin.AdminService;
import cn.wildfirechat.app.common.CommonService;
import cn.wildfirechat.app.common.entity.UploadFile;
import cn.wildfirechat.app.common.pojo.Page;
import cn.wildfirechat.app.work.report.entity.WorkReportEntry;
import cn.wildfirechat.app.work.report.entity.WorkReportToEntry;
import cn.wildfirechat.app.work.report.enums.WorkReportType;
import cn.wildfirechat.app.work.report.pojo.InputWorkReportRequest;
import cn.wildfirechat.app.work.report.pojo.VOWorkReport;
import cn.wildfirechat.app.work.report.repository.WorkReportRepository;
import cn.wildfirechat.app.work.report.repository.WorkReportTempRepository;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

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

    @PersistenceContext
    private EntityManager entityManager;

    private JPAQueryFactory jpaQueryFactory;

    @PostConstruct
    private void init() {
        jpaQueryFactory = new JPAQueryFactory(entityManager);
    }

    @Transactional
    public RestResult deleteReport(Long reportId, String userId) {
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

        if (request.getAttachment() != null) {
            String join = StringUtils.join(request.getAttachment()
                    .stream()
                    .filter(new Predicate<MultipartFile>() {
                        @Override
                        public boolean test(MultipartFile file) {
                            return !file.isEmpty() && file.getSize() > 0;
                        }
                    })
                    .map(new Function<MultipartFile, String>() {
                        @Override
                        public String apply(MultipartFile file) {
                            UploadFile uploadFile = commonService.uploadFile(userId, file);
                            return commonService.localFileDownloadDomain + uploadFile.getId();
                        }
                    }).collect(Collectors.toList()), ",");
            if (StringUtils.isNotBlank(join)) {
                workReportEntry.setAttachment(join);
            }
        }
        workReportRepository.saveAndFlush(workReportEntry);

        if (StringUtils.isNotBlank(request.getReportTo())) {
            if (reportId != null)
                workReportTempRepository.deleteAllByReportId(reportId);
            Map<String, String> map = new Gson().fromJson(request.getReportTo(), new TypeToken<Map<String, String>>() {
            }.getType());
            List<WorkReportToEntry> list = map.entrySet().stream().map(new Function<Map.Entry<String, String>, WorkReportToEntry>() {
                @Override
                public WorkReportToEntry apply(Map.Entry<String, String> entry) {
                    adminService.sendWorkReportMessage(userId, entry.getKey(), workReportEntry);
                    return WorkReportToEntry.builder()
                            .reportId(workReportEntry.getId())
                            .targetId(entry.getKey())
                            .targetName(entry.getValue())
                            .build();
                }
            }).collect(Collectors.toList());
            workReportTempRepository.saveAll(list);
        }

//        if (StringUtils.isNotBlank(request.getSendTo())) {
//            List<String> sendTo = new Gson().fromJson(request.getSendTo(), new TypeToken<List<String>>() {
//            }.getType());
//            for (String s : sendTo) {
//                adminService.sendWorkReportMessage(userId, s, workReportEntry);
//            }
//        }

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
    public RestResult getReportList(String userId, String targetId, WorkReportType type, Date beginTime, Date endTime, Page page) {
        List<Long> ids = null;
        if (StringUtils.isNotBlank(targetId)) {
            ids = workReportTempRepository.findAllReportIdByTargetId(targetId);
            if (ids.isEmpty()) return RestResult.ok(Collections.emptyList());
        }
        List<VOWorkReport> list = workReportRepository.getReportList(userId, ids, type, beginTime, endTime, page.convert2PageRequest())
                .stream().map(new Function<WorkReportEntry, VOWorkReport>() {
                    @Override
                    public VOWorkReport apply(WorkReportEntry entry) {
                        VOWorkReport voWorkReport = new VOWorkReport();

                        BeanUtils.copyProperties(entry, voWorkReport);
                        voWorkReport.setType(entry.getType().toString());

                        List<WorkReportToEntry> all = workReportTempRepository.findAllByReportId(entry.getId());
                        voWorkReport.setReportTo(all
                                .stream()
                                .collect(Collectors.toMap(new Function<WorkReportToEntry, String>() {
                                    @Override
                                    public String apply(WorkReportToEntry workReportToEntry) {
                                        return workReportToEntry.getTargetId();
                                    }
                                }, new Function<WorkReportToEntry, String>() {
                                    @Override
                                    public String apply(WorkReportToEntry workReportToEntry) {
                                        return workReportToEntry.getTargetName();
                                    }
                                })));

                        return voWorkReport;
                    }
                }).collect(Collectors.toList());


        return RestResult.ok(list);
    }


}
