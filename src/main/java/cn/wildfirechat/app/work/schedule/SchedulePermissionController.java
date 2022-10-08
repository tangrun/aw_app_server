package cn.wildfirechat.app.work.schedule;

import cn.wildfirechat.app.RestResult;
import cn.wildfirechat.app.admin.AdminService;
import cn.wildfirechat.app.common.consts.SessionAttributes;
import cn.wildfirechat.app.work.schedule.pojo.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 日程权限相关
 */
@Slf4j
@RestController
@RequestMapping("permissionApi")
public class SchedulePermissionController {

    @Autowired
    SchedulePermissionService permissionService;

    @Autowired
    AdminService adminService;

    /**
     * 更新权限设置
     * @param userId
     * @param request
     * @return
     */
    @PostMapping("update")
    public RestResult<Void> permission(
            @SessionAttribute(SessionAttributes.userId) String userId,
            @RequestBody SchedulePermissionEntity request
    ) {
        return permissionService.setPermission(userId, request);
    }


    /**
     * 好友对我的权限设置  我有权读、写的好友列表
     * @param userId
     * @return
     */
    @PostMapping("friendsPermission")
    @Validated
    public RestResult<List<SchedulePermissionEntity>> friendsPermission(
            @SessionAttribute(SessionAttributes.userId) String userId
    ) {
        return permissionService.friendsPermission(userId);
    }


    /**
     * 某人对我的权限设置
     * @param userId
     * @return
     */
    @PostMapping("userPermission")
    @Validated
    public RestResult<SchedulePermissionEntity> userPermission(
            @SessionAttribute(SessionAttributes.userId) String userId,
            @RequestBody ScheduleUserPermissionRequest request
    ) {
        return permissionService.getUserPermission(userId,request);
    }

    /**
     * 我的权限设置
     * @param userId
     * @return
     */
    @PostMapping("permission")
    public RestResult<List<SchedulePermissionEntity>> permission(
            @SessionAttribute(SessionAttributes.userId) String userId
    ) {
        return permissionService.getPermission(userId);
    }

    /**
     * 我的权限设置
     * @param userId
     * @return
     */
    @PostMapping("allPermission")
    public RestResult<SchedulePermissionResponse> allPermission(
            @SessionAttribute(SessionAttributes.userId) String userId
    ) {
        return permissionService.getAllPermission(userId);
    }

    /**
     * 我对某人的权限设置
     * @param userId
     * @return
     */
    @PostMapping("permissionByFid")
    @Validated
    public RestResult<SchedulePermissionEntity> permissionByFid(
            @SessionAttribute(SessionAttributes.userId) String userId,
            @RequestBody ScheduleUserPermissionRequest request
    ) {
        return permissionService.getPermissionByFid(userId,request);
    }
}
