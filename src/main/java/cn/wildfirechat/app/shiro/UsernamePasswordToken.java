package cn.wildfirechat.app.shiro;

public class UsernamePasswordToken extends org.apache.shiro.authc.UsernamePasswordToken {
    public UsernamePasswordToken() {
    }

    public UsernamePasswordToken(String username, String password) {
        super(username, password);
    }
}
