package cn.wildfirechat.app.work.countdown;

import cn.wildfirechat.app.RestResult;
import cn.wildfirechat.app.work.countdown.vo.AddOrCreateCountDownRequest;
import cn.wildfirechat.app.work.countdown.vo.CountDownInfo;
import cn.wildfirechat.app.work.countdown.vo.QueryListRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CountDownService {
    @Autowired
    CountDownRepository countDownRepository;
    @Autowired
    CountDownService countDownService;

    public RestResult<CountDownInfo> setTop(String userId,Long id,boolean isTop){
        CountDownEntity entity = countDownRepository.findByUserIdAndId(userId, id);
        Assert.notNull(entity, "对象不存在");
        entity.setTop(isTop);
        countDownRepository.saveAndFlush(entity);
        return RestResult.ok(convertTo(entity));
    }

    public RestResult<CountDownInfo> addOrCreateCountDown(String userId, AddOrCreateCountDownRequest request) {
        CountDownEntity entity;
        if (request.getId() == null) {
            entity = new CountDownEntity();
            entity.setUserId(userId);
        } else {
            entity = countDownRepository.findByUserIdAndId(userId, request.getId());
            if (entity == null) return RestResult.error("对象不存在");
        }
        BeanUtils.copyProperties(request, entity, "id");
        countDownRepository.saveAndFlush(entity);

        return RestResult.ok(convertTo(entity));
    }

    public RestResult<Void> deleteCountDown(String userId, Long id) {
        CountDownEntity entity = countDownRepository.findByUserIdAndId(userId, id);
        if (entity == null) return RestResult.error("对象不存在");
        countDownRepository.delete(entity);
        return RestResult.ok(null);
    }

    public RestResult<List<CountDownInfo>> query(QueryListRequest request) {
        List<CountDownEntity> countDownEntityList = countDownRepository.findAll(new Specification<CountDownEntity>() {
            @Override
            public Predicate toPredicate(Root<CountDownEntity> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                Predicate predicate = criteriaBuilder.conjunction();

                if (StringUtils.isNotBlank(request.getType()) && !"全部".equals(request.getType()))
                    predicate.getExpressions().add(criteriaBuilder.equal(root.get("type"), request.getType()));

                if (StringUtils.isNotBlank(request.getUserId()))
                    predicate.getExpressions().add(criteriaBuilder.equal(root.get("userId"), request.getUserId()));

                return predicate;
            }
        }, Sort.by(Sort.Order.desc("top"),Sort.Order.desc("updateTime"),Sort.Order.desc("createTime")));
        if (countDownEntityList.isEmpty())
            return RestResult.ok(Collections.emptyList());
        List<CountDownInfo> countDownInfoList = countDownEntityList.stream().map(this::convertTo)
                .collect(Collectors.toList());
        return RestResult.ok(countDownInfoList);
    }

    private CountDownInfo convertTo(CountDownEntity entity){
        CountDownInfo countDownInfo = new CountDownInfo();
        BeanUtils.copyProperties(entity, countDownInfo);
        if (countDownInfo.getTop() == null) {
            countDownInfo.setTop(false);
        }
        return countDownInfo;
    }
}
