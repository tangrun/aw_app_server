package cn.wildfirechat.app.work;

import cn.wildfirechat.app.RestResult;
import cn.wildfirechat.app.work.banner.BannerEntity;
import cn.wildfirechat.app.work.banner.BannerResponse;
import cn.wildfirechat.app.work.banner.BannerService;
import cn.wildfirechat.app.work.banner.BannerType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RequestMapping("workbench")
@RestController
public class WorkbenchController {

    @Autowired
    private BannerService bannerService;

    @PostMapping("banner")
    public RestResult<List<BannerResponse>> getWorkbenchBanner() {
        List<BannerEntity> enabledBanner = bannerService.getEnabledBanner(BannerType.workbench);
        List<BannerResponse> list = enabledBanner.stream()
                .map(this::convertTo)
                .collect(Collectors.toList());
        return RestResult.ok(list);
    }

    private BannerResponse convertTo(BannerEntity entity) {
        return new BannerResponse(entity.getId(), entity.getTitle(), entity.getDesc(), entity.getUrl(), entity.getImgUrl());
    }
}
