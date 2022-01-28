package cn.wildfirechat.app.shiro;

import cn.wildfirechat.app.RestResult;
import cn.wildfirechat.pojos.InputOutputUserInfo;
import cn.wildfirechat.sdk.UserAdmin;
import cn.wildfirechat.sdk.model.IMResult;
import cn.wildfirechat.sdk.utilities.AdminHttpUtils;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.credential.CredentialsMatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TokenMatcher implements CredentialsMatcher {
    @Autowired
    private AuthDataSource authDataSource;

    @Override
    public boolean doCredentialsMatch(AuthenticationToken token, AuthenticationInfo info) {
        if (token instanceof TokenAuthenticationToken) {
            TokenAuthenticationToken tt = (TokenAuthenticationToken)token;
            RestResult.RestCode restCode = authDataSource.checkPcSession(tt.getToken());
            if (restCode == RestResult.RestCode.SUCCESS) {
                return true;
            }
        }
        return false;
    }

    public static void main(String[] args) {
        AdminHttpUtils.init("http://192.168.0.218:8801", "123456");
        try {
            IMResult<InputOutputUserInfo> userByMobile = UserAdmin.getUserByMobile("18282638510");
            System.out.println(userByMobile.msg);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
