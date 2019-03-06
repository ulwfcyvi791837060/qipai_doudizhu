package com.anbang.qipai.doudizhu.web.vo;

import com.anbang.qipai.doudizhu.cqrs.q.dbo.PukeGamePlayerDbo;

public class PukeGamePlayerVO {
	private String playerId;
	private String nickname;
	private String gender;// 会员性别:男:male,女:female
	private String headimgurl;
	private String state;
	private String onlineState;
	private int totalScore;

	public PukeGamePlayerVO(PukeGamePlayerDbo dbo) {
		playerId = dbo.getPlayerId();
		nickname = dbo.getNickname();
		gender = dbo.getGender();
		headimgurl = dbo.getHeadimgurl();
		onlineState = dbo.getOnlineState().name();
		totalScore = dbo.getTotalScore();
		String sn = dbo.getState().name();

	}

	public String getPlayerId() {
		return playerId;
	}

	public void setPlayerId(String playerId) {
		this.playerId = playerId;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public String getHeadimgurl() {
		return headimgurl;
	}

	public void setHeadimgurl(String headimgurl) {
		this.headimgurl = headimgurl;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getOnlineState() {
		return onlineState;
	}

	public void setOnlineState(String onlineState) {
		this.onlineState = onlineState;
	}

	public int getTotalScore() {
		return totalScore;
	}

	public void setTotalScore(int totalScore) {
		this.totalScore = totalScore;
	}

}
