package cn.wildfirechat.app.shiro;

import org.apache.shiro.authc.UsernamePasswordToken;

public class UsernameCodeToken extends UsernamePasswordToken {
    public UsernameCodeToken() {
    }

    public UsernameCodeToken(String username, String password) {
        super(username, password);
    }
}
