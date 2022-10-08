package cn.wildfirechat.app.work.schedule;

import cn.wildfirechat.app.RestResult;
import cn.wildfirechat.app.UserService;
import cn.wildfirechat.app.jpa.UserEntity;
import cn.wildfirechat.app.jpa.UserEntityRepository;
import cn.wildfirechat.app.work.schedule.pojo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;

@Service
public class SchedulePermissionService {

    @Autowired
    SchedulePermissionRepository scheduleRepository;
    @Autowired
    UserService userService;
    @Autowired
    UserEntityRepository userEntityRepository;

    /**
     * 某人对我的权限设置
     * @return
     */
    public RestResult<SchedulePermissionEntity> getUserPermission(String userId,ScheduleUserPermissionRequest request) {
        SchedulePermissionEntity entity = scheduleRepository.findUserSet(request.getUserId(),userId);
        if(entity == null){
            entity = new SchedulePermissionEntity();
            entity.uid = request.getUserId();
            entity.fid = userId;
        }
        return RestResult.ok(entity);
    }

    /**
     * 我对某个好友的权限设置
     * @return
     */
    public RestResult<SchedulePermissionEntity> getPermissionByFid(String userId,ScheduleUserPermissionRequest request) {
        SchedulePermissionEntity entity = scheduleRepository.findUserSet(userId,request.getUserId());
        if(entity == null){
            entity = new SchedulePermissionEntity();
            entity.id = Long.valueOf(0);
            entity.uid = userId;
            entity.fid = request.getUserId();
        }
        return RestResult.ok(entity);
    }

    /**
     * 好友对我的权限设置  我有权读、写的好友列表
     * @param userId
     * @return
     */
    public RestResult<List<SchedulePermissionEntity>> friendsPermission(String userId) {
        List<SchedulePermissionEntity> list = scheduleRepository.findSetByFid(userId);
        return RestResult.ok(list);
    }

    /**
     * 我的权限设置
     * @param userId
     * @return
     */
    public RestResult<List<SchedulePermissionEntity>> getPermission(String userId) {
        List<SchedulePermissionEntity> list = scheduleRepository.findUserSet(userId);
        //Assert.notNull(permissionSetting,"用户不存在");
        return RestResult.ok(list);
    }

    /**
     * 我的权限设置
     * @param userId
     * @return
     */
    public RestResult<SchedulePermissionResponse> getAllPermission(String userId) {
        SchedulePermissionResponse response = new SchedulePermissionResponse();
        List<SchedulePermissionEntity> mylist = scheduleRepository.findUserSet(userId);
        List<SchedulePermissionEntity> friendlist = scheduleRepository.findSetByFid(userId);
        response.setMySet(mylist==null?new ArrayList<>():mylist);
        response.setUserSet(friendlist==null?new ArrayList<>():friendlist);

        //System.out.println("应返还数据：" + new Gson().toJson(response));
        return RestResult.ok(response);
    }

    /**
     * 设置权限
     * @param userId
     * @param request
     * @return
     */
    public RestResult<Void> setPermission(String userId, SchedulePermissionEntity request) {
        UserEntity userEntity = userService.findByUserId(userId);
        Assert.notNull(userEntity, "用户不存在");
        //Assert.isTrue(SchedulePermission.checkValue(request.getRead()), "权限不正确");
        //Assert.isTrue(SchedulePermission.checkValue(request.getWrite()), "权限不正确");
        request.uid = userId;
        if(request.id == null){
            request.id = Long.valueOf(0);
        }
//        SchedulePermissionEntity permissionEntity = scheduleRepository.findUserSet(userId,request.fid);
//        if(permissionEntity == null){
//            scheduleRepository.save(request);
//        }else {
//            if(request.look != 0 || request.edit != 0){
//                permissionEntity.setEdit(request.edit);
//                permissionEntity.setLook(request.look);
//                scheduleRepository.save(permissionEntity);
//            }else {
//                scheduleRepository.removeInfoTips(request);
//            }
//        }

        scheduleRepository.modifyPermission(request.id,request.uid,request.fid,request.look,request.edit);
        return RestResult.ok();
    }

}
