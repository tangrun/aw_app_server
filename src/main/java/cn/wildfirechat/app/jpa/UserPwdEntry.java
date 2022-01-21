package cn.wildfirechat.app.jpa;

import javax.persistence.*;

/**
 * 自己新增
 * 用户密码登录
 */
@Entity
@Table(name = "t_user_pwd")
public class UserPwdEntry {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name = "id")
	private Integer id;



	@Column(name = "user_id")
	public String user_id;

	@Column(name = "passwd")
	public String passwd;

	@Column(name = "salt")
	public String salt;

	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}

	public String getUser_id() {
		return user_id;
	}

	public void setUser_id(String user_id) {
		this.user_id = user_id;
	}

	public String getPasswd() {
		return passwd;
	}

	public void setPasswd(String passwd) {
		this.passwd = passwd;
	}

	public String getSalt() {
		return salt;
	}

	public void setSalt(String salt) {
		this.salt = salt;
	}
}
