package cn.wildfirechat.app.conference;


import cn.wildfirechat.app.RestResult;
import cn.wildfirechat.app.pojo.ConferenceInfo;

public interface ConferenceService {
    RestResult getUserConferenceId(String userId);
    RestResult getMyConferenceId();
    RestResult getConferenceInfo(String conferenceId, String password);
    RestResult putConferenceInfo(ConferenceInfo info);
    RestResult createConference(ConferenceInfo info);
    RestResult destroyConference(String conferenceId);
    RestResult recordingConference(String conferenceId, boolean recording);
    RestResult favConference(String conferenceId);
    RestResult unfavConference(String conferenceId);
    RestResult getFavConferences();
    RestResult isFavConference(String conferenceId);
}
