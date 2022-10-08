package cn.wildfirechat.app.jpa;

import lombok.Data;

import javax.persistence.*;

/**
 * 自己新增
 * 用户密码登录
 */
@Data
@Entity
@Table(name = "t_user_pwd",indexes = {
		@Index(name = "idx_user_id",columnList = "user_id"),
		@Index(name = "idx_mobile",columnList = "mobile"),
		@Index(name = "idx_wx_code",columnList = "wechat_unionid"),
		@Index(name = "idx_qq_code",columnList = "qq_openid"),
})
public class UserEntity {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name = "id")
	private Long id;

	@Column(name = "user_id",nullable = false)
	public String userId;

	@Column(name = "passwd")
	public String passwd;

	@Column(name = "salt")
	public String salt;

	@Column(name = "mobile")
	public String mobile;

	/**
	 * 用户状态
	 * null 以前的数据
	 * 0 正常状态
	 * 1 用户注销
	 * 2 系统封号
	 */
	@Column(name = "state")
	private Integer state;

	/**
	 * 登陆时 非可用状态时提示内容
	 */
	@Column(name = "login_remark")
	private String loginRemark;

	@Column(name = "wechat_unionid")
	private String wechatUnionid;

	@Column(name = "qq_openid")
	private String qqOpenid;

}
