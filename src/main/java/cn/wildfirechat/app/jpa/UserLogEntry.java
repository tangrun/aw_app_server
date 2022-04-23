package cn.wildfirechat.app.jpa;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import java.util.Date;

/**
 * 自己新增
 * 用户登录日志
 */
@Data
@Entity
@Table(name = "t_user_log")
public class UserLogEntry {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name = "id")
	private Long id;

	@Column(name = "user_id")
	public String userId;

	@Column(name = "ip")
	public String ip;

	@DateTimeFormat(pattern ="yyyy-MM-dd HH:mm:ss")
	@JsonFormat(pattern="yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
	@Column(name = "login_time")
	public Date loginTime;

	/**
	 * 设备系统版本
	 */
	@Column(name = "device_version")
	public String deviceVersion;

	/**
	 * 手机型号
	 */
	@Column(name = "device_model")
	public String deviceModel;

	/**
	 * app版本
	 */
	@Column(name = "app_version")
	public String appVersion;
}
