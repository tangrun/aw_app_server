package cn.wildfirechat.app.shiro;

import cn.wildfirechat.app.jpa.ShiroSession;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.UnknownSessionException;
import org.apache.shiro.session.mgt.SimpleSession;
import org.apache.shiro.session.mgt.eis.AbstractSessionDAO;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.*;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class DBSessionDao extends AbstractSessionDAO {

    @Resource
    ShiroSessionService shiroSessionService;

    @Override
    protected Serializable doCreate(Session session) {
        String sessionId = UUID.randomUUID().toString().replaceAll("-", "");
        ((SimpleSession) session).setId(sessionId);
        return sessionId;
    }

    @Override
    protected Session doReadSession(Serializable sessionId) {
        ShiroSession shiroSession = shiroSessionService.getSessionById((String) sessionId);
        return shiroSession == null ? null : byteToSession(shiroSession.getSessionData());
    }

    @Override
    public void update(Session session) throws UnknownSessionException {
        shiroSessionService.updateSession((String) session.getId(), sessionToByte(session));
    }

    @Override
    public void delete(Session session) {
        shiroSessionService.deleteSession((String) session.getId());
    }

    @Override
    public Collection<Session> getActiveSessions() {
        List<ShiroSession> activeSession = shiroSessionService.getActiveSession();
        return activeSession.stream()
                .map(new Function<ShiroSession, Session>() {
                    @Override
                    public Session apply(ShiroSession shiroSession) {
                        return byteToSession(shiroSession.getSessionData());
                    }
                })
                .collect(Collectors.toList());
    }


    // convert session object to byte, then store it to redis
    private byte[] sessionToByte(Session session) {
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        byte[] bytes = null;
        try {
            ObjectOutputStream oo = new ObjectOutputStream(bo);
            oo.writeObject(session);
            bytes = bo.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bytes;
    }

    // restore session
    private Session byteToSession(byte[] bytes) {
        ByteArrayInputStream bi = new ByteArrayInputStream(bytes);
        ObjectInputStream in;
        SimpleSession session = null;
        try {
            in = new ObjectInputStream(bi);
            session = (SimpleSession) in.readObject();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return session;
    }

}
