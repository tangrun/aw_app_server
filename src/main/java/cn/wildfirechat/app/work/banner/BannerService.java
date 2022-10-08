package cn.wildfirechat.app.work.banner;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@CacheConfig(cacheNames = "banner")
@Service
public class BannerService {

    @Autowired
    private BannerRepository bannerRepository;

    @Cacheable(key = "'type:'+#type.toString()")
    public List<BannerEntity> getEnabledBanner(BannerType type){
        return bannerRepository.getAllByEnabledAndType(true, type);
    }

}
