package cn.wildfirechat.app.jpa;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "conference")
public class ConferenceEntity {
	@Id
	@Column(length = 12)
	public String id;
	public String conferenceTitle;
	public String password;
	public String pin;
	public String owner;
	public String manages;
	public long startTime;
	public long endTime;
	public boolean audience;
	public boolean advance;
	public boolean allowSwitchMode;
	public boolean noJoinBeforeStart;
	public boolean recording;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getConferenceTitle() {
		return conferenceTitle;
	}

	public void setConferenceTitle(String conferenceTitle) {
		this.conferenceTitle = conferenceTitle;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getPin() {
		return pin;
	}

	public void setPin(String pin) {
		this.pin = pin;
	}

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public long getEndTime() {
		return endTime;
	}

	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}

	public boolean isAudience() {
		return audience;
	}

	public void setAudience(boolean audience) {
		this.audience = audience;
	}

	public boolean isAdvance() {
		return advance;
	}

	public void setAdvance(boolean advance) {
		this.advance = advance;
	}

	public boolean isAllowSwitchMode() {
		return allowSwitchMode;
	}

	public void setAllowSwitchMode(boolean allowSwitchMode) {
		this.allowSwitchMode = allowSwitchMode;
	}

	public boolean isNoJoinBeforeStart() {
		return noJoinBeforeStart;
	}

	public void setNoJoinBeforeStart(boolean noJoinBeforeStart) {
		this.noJoinBeforeStart = noJoinBeforeStart;
	}

	public boolean isRecording() {
		return recording;
	}

	public void setRecording(boolean recording) {
		this.recording = recording;
	}

	public String getManages() {
		return manages;
	}

	public void setManages(String manages) {
		this.manages = manages;
	}
}
