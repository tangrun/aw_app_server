package cn.wildfirechat.app.shiro;

import cn.wildfirechat.app.jpa.ShiroSession;
import cn.wildfirechat.app.jpa.ShiroSessionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Set;

@CacheConfig(cacheNames = "ShiroSession")
@Slf4j
@Service
public class ShiroSessionService {

    @Resource
    private ShiroSessionRepository shiroSessionRepository;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;


    @Cacheable(key = "#sessionId",unless = "#result == null ")
    public ShiroSession getSessionById(String sessionId) {
        log.debug("getSessionById {}",sessionId);
        return shiroSessionRepository.findById(sessionId).orElse(null);
    }

    @CachePut(key = "#sessionId")
    public ShiroSession updateSession(String sessionId, byte[] data) {
        log.debug("updateSession {}",sessionId);
        ShiroSession shiroSession = new ShiroSession(sessionId, data);
        shiroSessionRepository.saveAndFlush(shiroSession);
        return shiroSession;
    }

    @CacheEvict(key = "#sessionId")
    public void deleteSession(String sessionId) {
        log.debug("deleteSession {}",sessionId);
        shiroSessionRepository.deleteById(sessionId);
    }

    public List<ShiroSession> getActiveSession() {
        log.debug("getActiveSession ");
        Set<String> keys = redisTemplate.keys("ShiroSession*");
        return (List<ShiroSession>) ((List) redisTemplate.opsForValue().multiGet(keys));
    }

}
