package cn.wildfirechat.app.shiro;

import cn.wildfirechat.app.RestResult;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PasswordRealm extends AuthorizingRealm {

    @Autowired
    AuthDataSource authDataSource;

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
        return info;
    }

    @Override
    public boolean supports(AuthenticationToken token) {
        return token instanceof UsernamePasswordToken;
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        if (token instanceof UsernamePasswordToken) {
            if (((UsernamePasswordToken) token).getPassword() == null) {
                throw new AuthenticationException("密码为空");
            }
            RestResult.RestCode restCode = authDataSource.verifyPassword(((UsernamePasswordToken) token).getUsername(), new String(((UsernamePasswordToken) token).getPassword()));
            if (restCode == RestResult.RestCode.SUCCESS) {
                return new SimpleAuthenticationInfo(token.getPrincipal(), token.getCredentials(), getName());
            }
        }
        throw new AuthenticationException("密码不正确");
    }
}
