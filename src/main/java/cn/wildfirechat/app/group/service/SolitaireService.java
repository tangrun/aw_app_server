package cn.wildfirechat.app.group.service;

import cn.wildfirechat.app.RestResult;
import cn.wildfirechat.app.admin.AdminService;
import cn.wildfirechat.app.group.entity.SolitaireEntry;
import cn.wildfirechat.app.group.entity.SolitaireItemEntry;
import cn.wildfirechat.app.group.pojo.SolitaireInfo;
import cn.wildfirechat.app.group.pojo.SolitaireItemInfo;
import cn.wildfirechat.app.group.repository.SolitaireRepository;
import cn.wildfirechat.app.group.repository.SolitaireTempRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class SolitaireService {

    @Autowired
    SolitaireRepository solitaireRepository;

    @Autowired
    SolitaireTempRepository solitaireTempRepository;

    @Autowired
    private AdminService adminService;

    @Transactional
    public RestResult create(String userId, SolitaireInfo request) {
        Assert.isTrue(StringUtils.isNotBlank(request.getTheme()), "主题不能为空");
        Assert.isTrue(StringUtils.isNotBlank(request.getGroupId()), "群id不能为空");

        SolitaireEntry solitaireEntry = new SolitaireEntry();
        solitaireEntry.setUserId(userId)
                .setGroupId(request.getGroupId())
                .setTheme(request.getTheme())
                .setTemplate(request.getTemplate())
                .setSupply(request.getSupply());
        solitaireRepository.saveAndFlush(solitaireEntry);
        request.setId(solitaireEntry.getId());
        return append(userId, request);
    }

    @Transactional
    public RestResult append(String userId, SolitaireInfo request) {
        Assert.notNull(request.getId(), "id不能为空");
        SolitaireEntry solitaireEntry = solitaireRepository.findById(request.getId()).orElse(null);
        Assert.notNull(solitaireEntry, "对象不存在");

        for (SolitaireItemInfo info : request.getItems()) {
            Assert.isTrue(StringUtils.isNotBlank(info.getContent()), "内容不能为空");
        }
        List<SolitaireItemEntry> collect = request.getItems()
                .stream()
                .map(new Function<SolitaireItemInfo, SolitaireItemEntry>() {
                    @Override
                    public SolitaireItemEntry apply(SolitaireItemInfo solitaireItemInfo) {
                        SolitaireItemEntry entry = solitaireItemInfo.getSolitaireItemId() == null ? new SolitaireItemEntry()
                                : solitaireTempRepository.findById(solitaireItemInfo.getSolitaireItemId()).orElse(null);
                        Assert.notNull(entry, "修改的内容不存在");
                        if (entry.getId() != null) {
                            Assert.isTrue(Objects.equals(userId, entry.getUserId()), "只能修改自己的接龙");
                            // 内容没变的 就不需要修改了
                            if (Objects.equals(entry.getContent(), solitaireItemInfo.getContent())) return null;
                        } else {
                            entry.setUserId(userId)
                                    .setSolitaireId(request.getId());
                        }
                        entry.setContent(solitaireItemInfo.getContent());
                        return entry;
                    }
                })
                .filter(solitaireItemEntry -> !Objects.isNull(solitaireItemEntry))
                .collect(Collectors.toList());
        solitaireTempRepository.saveAll(collect);

        adminService.sendSolitaireMessage(userId, request.getGroupId(), getSolitaireInfo_(request.getId()));

        return RestResult.ok(null);
    }

    private SolitaireInfo getSolitaireInfo_(Long solitaireId) {
        Assert.notNull(solitaireId, "ID为空");

        SolitaireEntry solitaireEntry = solitaireRepository.findById(solitaireId).orElse(null);
        Assert.notNull(solitaireEntry, "对象不存在");

        SolitaireInfo solitaireInfo = new SolitaireInfo();
        BeanUtils.copyProperties(solitaireEntry, solitaireInfo);

        List<SolitaireItemEntry> all = solitaireTempRepository.findAllBySolitaireIdOrderById(solitaireId);
        solitaireInfo.items = all.stream()
                .map(new Function<SolitaireItemEntry, SolitaireItemInfo>() {
                    @Override
                    public SolitaireItemInfo apply(SolitaireItemEntry solitaireItemEntry) {
                        SolitaireItemInfo solitaireItemInfo = new SolitaireItemInfo();
                        BeanUtils.copyProperties(solitaireItemEntry, solitaireItemInfo);
                        solitaireItemInfo.setSolitaireItemId(solitaireItemEntry.getId());
                        return solitaireItemInfo;
                    }
                })
                .collect(Collectors.toList());

        return solitaireInfo;
    }

    /**
     * @param solitaireId 接龙ID
     * @return
     */
    public RestResult getSolitaireInfo(Long solitaireId) {
        SolitaireInfo solitaireInfo_ = getSolitaireInfo_(solitaireId);
        return RestResult.ok(solitaireInfo_);
    }


}
