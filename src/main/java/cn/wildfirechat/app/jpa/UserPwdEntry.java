package cn.wildfirechat.app.jpa;

import lombok.Data;

import javax.persistence.*;

/**
 * 自己新增
 * 用户密码登录
 */
@Data
@Entity
@Table(name = "t_user_pwd")
public class UserPwdEntry {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name = "id")
	private Integer id;

	@Column(name = "user_id")
	public String userId;

	@Column(name = "passwd")
	public String passwd;

	@Column(name = "salt")
	public String salt;

	@Column(name = "mobile")
	public String mobile;

}
