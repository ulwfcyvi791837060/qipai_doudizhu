package com.anbang.qipai.doudizhu.web.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.anbang.qipai.doudizhu.cqrs.c.domain.PukeGameValueObject;
import com.anbang.qipai.doudizhu.cqrs.c.domain.result.ReadyForGameResult;
import com.anbang.qipai.doudizhu.cqrs.c.domain.state.Qiangdizhu;
import com.anbang.qipai.doudizhu.cqrs.c.service.GameCmdService;
import com.anbang.qipai.doudizhu.cqrs.c.service.PlayerAuthService;
import com.anbang.qipai.doudizhu.cqrs.q.dbo.GameFinishVoteDbo;
import com.anbang.qipai.doudizhu.cqrs.q.dbo.JuResultDbo;
import com.anbang.qipai.doudizhu.cqrs.q.dbo.PukeGameDbo;
import com.anbang.qipai.doudizhu.cqrs.q.dbo.PukeGamePlayerDbo;
import com.anbang.qipai.doudizhu.cqrs.q.service.PukeGameQueryService;
import com.anbang.qipai.doudizhu.cqrs.q.service.PukePlayQueryService;
import com.anbang.qipai.doudizhu.msg.msjobj.PukeHistoricalJuResult;
import com.anbang.qipai.doudizhu.msg.service.DoudizhuGameMsgService;
import com.anbang.qipai.doudizhu.msg.service.DoudizhuResultMsgService;
import com.anbang.qipai.doudizhu.msg.service.MemberGoldsMsgService;
import com.anbang.qipai.doudizhu.msg.service.WiseCrackMsgServcie;
import com.anbang.qipai.doudizhu.plan.bean.MemberGoldBalance;
import com.anbang.qipai.doudizhu.plan.service.MemberGoldBalanceService;
import com.anbang.qipai.doudizhu.web.vo.CommonVO;
import com.anbang.qipai.doudizhu.web.vo.GameFinishVoteVO;
import com.anbang.qipai.doudizhu.web.vo.GameVO;
import com.anbang.qipai.doudizhu.websocket.GamePlayWsNotifier;
import com.anbang.qipai.doudizhu.websocket.QueryScope;
import com.dml.mpgame.game.Canceled;
import com.dml.mpgame.game.Finished;
import com.dml.mpgame.game.GameNotFoundException;
import com.dml.mpgame.game.extend.fpmpv.VoteNotPassWhenWaitingNextPan;
import com.dml.mpgame.game.extend.vote.FinishedByVote;
import com.dml.mpgame.game.extend.vote.VoteNotPassWhenPlaying;
import com.dml.mpgame.game.player.GamePlayerOnlineState;

@RestController
@RequestMapping("/game")
public class GameController {

	@Autowired
	private PlayerAuthService playerAuthService;

	@Autowired
	private GameCmdService gameCmdService;

	@Autowired
	private PukeGameQueryService pukeGameQueryService;

	@Autowired
	private PukePlayQueryService pukePlayQueryService;

	@Autowired
	private GamePlayWsNotifier wsNotifier;

	@Autowired
	private DoudizhuGameMsgService gameMsgService;

	@Autowired
	private DoudizhuResultMsgService doudizhuResultMsgService;

	@Autowired
	private WiseCrackMsgServcie wiseCrackMsgServcie;

	@Autowired
	private MemberGoldBalanceService memberGoldBalanceService;

	@Autowired
	private MemberGoldsMsgService memberGoldsMsgService;

	/**
	 * 新一局游戏
	 */
	@RequestMapping(value = "/newgame")
	@ResponseBody
	public CommonVO newgame(String playerId, int panshu, int renshu, boolean qxp, int difen) {
		CommonVO vo = new CommonVO();
		String newGameId = UUID.randomUUID().toString();
		PukeGameValueObject pukeGameValueObject = gameCmdService.newPukeGame(newGameId, playerId, panshu, renshu, qxp,
				difen);
		pukeGameQueryService.newPukeGame(pukeGameValueObject);
		String token = playerAuthService.newSessionForPlayer(playerId);
		Map data = new HashMap();
		data.put("gameId", newGameId);
		data.put("token", token);
		vo.setData(data);
		return vo;
	}

	/**
	 * 新一局游戏,游戏未开始时离开就是退出
	 */
	@RequestMapping(value = "/newgame_leave_quit")
	@ResponseBody
	public CommonVO newgame_leave_quit(String playerId, int panshu, int renshu, boolean qxp, int difen) {
		CommonVO vo = new CommonVO();
		String newGameId = UUID.randomUUID().toString();
		PukeGameValueObject pukeGameValueObject = gameCmdService.newPukeGameLeaveAndQuit(newGameId, playerId, panshu,
				renshu, qxp, difen);
		pukeGameQueryService.newPukeGame(pukeGameValueObject);
		String token = playerAuthService.newSessionForPlayer(playerId);
		Map data = new HashMap();
		data.put("gameId", newGameId);
		data.put("token", token);
		vo.setData(data);
		return vo;
	}

	/**
	 * 加入游戏
	 */
	@RequestMapping(value = "/joingame")
	@ResponseBody
	public CommonVO joingame(String playerId, String gameId) {
		CommonVO vo = new CommonVO();
		PukeGameValueObject pukeGameValueObject;
		try {
			pukeGameValueObject = gameCmdService.joinGame(playerId, gameId);
		} catch (Exception e) {
			vo.setSuccess(false);
			vo.setMsg(e.getClass().toString());
			return vo;
		}
		pukeGameQueryService.joinGame(pukeGameValueObject);
		// 通知其他玩家
		for (String otherPlayerId : pukeGameValueObject.allPlayerIds()) {
			if (!otherPlayerId.equals(playerId)) {
				GamePlayerOnlineState onlineState = pukeGameValueObject.findPlayerOnlineState(otherPlayerId);
				if (onlineState.equals(GamePlayerOnlineState.online)) {
					wsNotifier.notifyToQuery(otherPlayerId, QueryScope.scopesForState(pukeGameValueObject.getState(),
							pukeGameValueObject.findPlayerState(otherPlayerId)));
				}
			}
		}
		String token = playerAuthService.newSessionForPlayer(playerId);
		Map data = new HashMap();
		data.put("token", token);
		vo.setData(data);
		return vo;
	}

	/**
	 * 挂起（手机按黑的时候调用）
	 */
	@RequestMapping(value = "/hangup")
	@ResponseBody
	public CommonVO hangup(String token) {
		CommonVO vo = new CommonVO();
		String playerId = playerAuthService.getPlayerIdByToken(token);
		if (playerId == null) {
			vo.setSuccess(false);
			vo.setMsg("invalid token");
			return vo;
		}
		PukeGameValueObject pukeGameValueObject;
		try {
			pukeGameValueObject = gameCmdService.leaveGameByHangup(playerId);
			if (pukeGameValueObject == null) {
				vo.setSuccess(true);
				return vo;
			}
		} catch (Exception e) {
			vo.setSuccess(false);
			vo.setMsg(e.getClass().getName());
			return vo;
		}
		pukeGameQueryService.leaveGame(pukeGameValueObject);
		// 断开玩家的socket
		wsNotifier.closeSessionForPlayer(playerId);
		String gameId = pukeGameValueObject.getId();
		JuResultDbo juResultDbo = pukePlayQueryService.findJuResultDbo(gameId);
		// 记录战绩
		if (juResultDbo != null) {
			PukeGameDbo pukeGameDbo = pukeGameQueryService.findPukeGameDboById(gameId);
			PukeHistoricalJuResult juResult = new PukeHistoricalJuResult(juResultDbo, pukeGameDbo);
			doudizhuResultMsgService.recordJuResult(juResult);
		}
		if (pukeGameValueObject.getState().name().equals(FinishedByVote.name)
				|| pukeGameValueObject.getState().name().equals(Canceled.name)) {
			gameMsgService.gameFinished(gameId);
		} else {
			gameMsgService.gamePlayerLeave(pukeGameValueObject, playerId);

		}
		// 通知其他玩家
		for (String otherPlayerId : pukeGameValueObject.allPlayerIds()) {
			if (!otherPlayerId.equals(playerId)) {
				GamePlayerOnlineState onlineState = pukeGameValueObject.findPlayerOnlineState(otherPlayerId);
				if (onlineState.equals(GamePlayerOnlineState.online)) {
					List<QueryScope> scopes = QueryScope.scopesForState(pukeGameValueObject.getState(),
							pukeGameValueObject.findPlayerState(otherPlayerId));
					scopes.remove(QueryScope.panResult);
					if (pukeGameValueObject.getState().name().equals(VoteNotPassWhenPlaying.name)
							|| pukeGameValueObject.getState().name().equals(VoteNotPassWhenWaitingNextPan.name)) {
						scopes.remove(QueryScope.gameFinishVote);
					}
					wsNotifier.notifyToQuery(otherPlayerId, scopes);
				}
			}
		}
		return vo;
	}

	/**
	 * 离开游戏(非退出,还会回来的)
	 */
	@RequestMapping(value = "/leavegame")
	@ResponseBody
	public CommonVO leavegame(String token) {
		CommonVO vo = new CommonVO();
		String playerId = playerAuthService.getPlayerIdByToken(token);
		if (playerId == null) {
			vo.setSuccess(false);
			vo.setMsg("invalid token");
			return vo;
		}
		PukeGameValueObject pukeGameValueObject;
		try {
			pukeGameValueObject = gameCmdService.leaveGame(playerId);
			if (pukeGameValueObject == null) {
				vo.setSuccess(true);
				return vo;
			}
		} catch (Exception e) {
			vo.setSuccess(false);
			vo.setMsg(e.getClass().getName());
			return vo;
		}
		pukeGameQueryService.leaveGame(pukeGameValueObject);
		// 断开玩家的socket
		wsNotifier.closeSessionForPlayer(playerId);
		String gameId = pukeGameValueObject.getId();
		JuResultDbo juResultDbo = pukePlayQueryService.findJuResultDbo(gameId);
		// 记录战绩
		if (juResultDbo != null) {
			PukeGameDbo pukeGameDbo = pukeGameQueryService.findPukeGameDboById(gameId);
			PukeHistoricalJuResult juResult = new PukeHistoricalJuResult(juResultDbo, pukeGameDbo);
			doudizhuResultMsgService.recordJuResult(juResult);
		}
		if (pukeGameValueObject.getState().name().equals(FinishedByVote.name)
				|| pukeGameValueObject.getState().name().equals(Canceled.name)) {
			gameMsgService.gameFinished(gameId);
		} else if (pukeGameValueObject.getState().name().equals(Finished.name)) {
			gameMsgService.gameCanceled(gameId, playerId);
		} else {
			gameMsgService.gamePlayerLeave(pukeGameValueObject, playerId);

		}
		// 通知其他玩家
		for (String otherPlayerId : pukeGameValueObject.allPlayerIds()) {
			if (!otherPlayerId.equals(playerId)) {
				GamePlayerOnlineState onlineState = pukeGameValueObject.findPlayerOnlineState(otherPlayerId);
				if (onlineState.equals(GamePlayerOnlineState.online)) {
					List<QueryScope> scopes = QueryScope.scopesForState(pukeGameValueObject.getState(),
							pukeGameValueObject.findPlayerState(otherPlayerId));
					if (!pukeGameValueObject.getState().name().equals(Finished.name)) {
						scopes.remove(QueryScope.panResult);
					}
					if (pukeGameValueObject.getState().name().equals(VoteNotPassWhenPlaying.name)
							|| pukeGameValueObject.getState().name().equals(VoteNotPassWhenWaitingNextPan.name)) {
						scopes.remove(QueryScope.gameFinishVote);
					}
					wsNotifier.notifyToQuery(otherPlayerId, scopes);
				}
			}
		}
		return vo;
	}

	/**
	 * 返回游戏
	 */
	@RequestMapping(value = "/backtogame")
	@ResponseBody
	public CommonVO backtogame(String playerId, String gameId) {
		CommonVO vo = new CommonVO();
		Map data = new HashMap();
		vo.setData(data);
		PukeGameValueObject pukeGameValueObject;
		try {
			pukeGameValueObject = gameCmdService.backToGame(playerId, gameId);
		} catch (Exception e) {
			// 如果找不到game，看下是否是已经结束(正常结束和被投票)的game
			if (e instanceof GameNotFoundException) {
				PukeGameDbo pukeGameDbo = pukeGameQueryService.findPukeGameDboById(gameId);
				if (pukeGameDbo != null && (pukeGameDbo.getState().name().equals(FinishedByVote.name)
						|| pukeGameDbo.getState().name().equals(Finished.name))) {
					data.put("queryScope", QueryScope.juResult);
					return vo;
				}
			}
			vo.setSuccess(false);
			vo.setMsg(e.getClass().toString());
			return vo;
		}

		pukeGameQueryService.backToGame(playerId, pukeGameValueObject);

		// 通知其他人
		for (String otherPlayerId : pukeGameValueObject.allPlayerIds()) {
			if (!otherPlayerId.equals(playerId)) {
				GamePlayerOnlineState onlineState = pukeGameValueObject.findPlayerOnlineState(otherPlayerId);
				if (onlineState.equals(GamePlayerOnlineState.online)) {
					List<QueryScope> scopes = QueryScope.scopesForState(pukeGameValueObject.getState(),
							pukeGameValueObject.findPlayerState(otherPlayerId));
					scopes.remove(QueryScope.panResult);
					if (pukeGameValueObject.getState().name().equals(VoteNotPassWhenPlaying.name)
							|| pukeGameValueObject.getState().name().equals(VoteNotPassWhenWaitingNextPan.name)) {
						scopes.remove(QueryScope.gameFinishVote);
					}
					wsNotifier.notifyToQuery(otherPlayerId, scopes);
				}
			}
		}
		String token = playerAuthService.newSessionForPlayer(playerId);
		data.put("token", token);
		return vo;

	}

	/**
	 * 游戏的所有信息,不包含局
	 * 
	 * @param gameId
	 * @return
	 */
	@RequestMapping(value = "/info")
	@ResponseBody
	public CommonVO info(String gameId) {
		CommonVO vo = new CommonVO();
		PukeGameDbo pukeGameDbo = pukeGameQueryService.findPukeGameDboById(gameId);
		GameVO gameVO = new GameVO(pukeGameDbo);
		Map data = new HashMap();
		data.put("game", gameVO);
		vo.setData(data);
		return vo;
	}

	/**
	 * 最开始的准备,不适用下一盘的准备
	 * 
	 * @param token
	 * @return
	 */
	@RequestMapping(value = "/ready")
	@ResponseBody
	public CommonVO ready(String token) {
		CommonVO vo = new CommonVO();
		Map data = new HashMap();
		vo.setData(data);
		String playerId = playerAuthService.getPlayerIdByToken(token);
		if (playerId == null) {
			vo.setSuccess(false);
			vo.setMsg("invalid token");
			return vo;
		}

		ReadyForGameResult readyForGameResult;
		try {
			readyForGameResult = gameCmdService.readyForGame(playerId, System.currentTimeMillis());
		} catch (Exception e) {
			vo.setSuccess(false);
			vo.setMsg(e.getClass().getName());
			return vo;
		}

		try {
			pukePlayQueryService.readyForGame(readyForGameResult);// TODO 一起点准备的时候可能有同步问题.要靠框架解决
		} catch (Throwable e) {
			vo.setSuccess(false);
			vo.setMsg(e.getMessage());
			return vo;
		}
		// 通知其他人
		for (String otherPlayerId : readyForGameResult.getPukeGame().allPlayerIds()) {
			if (!otherPlayerId.equals(playerId)) {
				GamePlayerOnlineState onlineState = readyForGameResult.getPukeGame()
						.findPlayerOnlineState(otherPlayerId);
				if (onlineState.equals(GamePlayerOnlineState.online)) {
					wsNotifier.notifyToQuery(otherPlayerId,
							QueryScope.scopesForState(readyForGameResult.getPukeGame().getState(),
									readyForGameResult.getPukeGame().findPlayerState(otherPlayerId)));
				}
			}
		}

		List<QueryScope> queryScopes = new ArrayList<>();
		queryScopes.add(QueryScope.gameInfo);
		if (readyForGameResult.getPukeGame().getState().name().equals(Qiangdizhu.name)) {
			queryScopes.add(QueryScope.panForMe);
			queryScopes.add(QueryScope.qiangdizhuInfo);
		}
		data.put("queryScopes", queryScopes);
		return vo;
	}

	/**
	 * 最开始的取消准备,不适用下一盘的准备
	 *
	 * @param token
	 * @return
	 */
	@RequestMapping(value = "/cancelready")
	@ResponseBody
	public CommonVO cancelReady(String token) {
		CommonVO vo = new CommonVO();
		Map data = new HashMap();
		vo.setData(data);
		String playerId = playerAuthService.getPlayerIdByToken(token);
		if (playerId == null) {
			vo.setSuccess(false);
			vo.setMsg("invalid token");
			return vo;
		}
		ReadyForGameResult readyForGameResult;
		try {
			readyForGameResult = gameCmdService.cancelReadyForGame(playerId, System.currentTimeMillis());
		} catch (Exception e) {
			vo.setSuccess(false);
			vo.setMsg(e.getClass().getName());
			return vo;
		}

		try {
			pukePlayQueryService.readyForGame(readyForGameResult);// TODO 一起点准备的时候可能有同步问题.要靠框架解决
		} catch (Throwable e) {
			vo.setSuccess(false);
			vo.setMsg(e.getMessage());
			return vo;
		}
		// 通知其他人
		for (String otherPlayerId : readyForGameResult.getPukeGame().allPlayerIds()) {
			if (!otherPlayerId.equals(playerId)) {
				GamePlayerOnlineState onlineState = readyForGameResult.getPukeGame()
						.findPlayerOnlineState(otherPlayerId);
				if (onlineState.equals(GamePlayerOnlineState.online)) {
					wsNotifier.notifyToQuery(otherPlayerId,
							QueryScope.scopesForState(readyForGameResult.getPukeGame().getState(),
									readyForGameResult.getPukeGame().findPlayerState(otherPlayerId)));
				}
			}
		}

		List<QueryScope> queryScopes = new ArrayList<>();
		queryScopes.add(QueryScope.gameInfo);
		data.put("queryScopes", queryScopes);
		return vo;
	}

	@RequestMapping(value = "/finish")
	@ResponseBody
	public CommonVO finish(String token) {
		CommonVO vo = new CommonVO();
		Map data = new HashMap();
		vo.setData(data);
		String playerId = playerAuthService.getPlayerIdByToken(token);
		if (playerId == null) {
			vo.setSuccess(false);
			vo.setMsg("invalid token");
			return vo;
		}

		PukeGameValueObject pukeGameValueObject;
		try {
			pukeGameValueObject = gameCmdService.finish(playerId, System.currentTimeMillis());
		} catch (Exception e) {
			vo.setSuccess(false);
			vo.setMsg(e.getClass().getName());
			return vo;
		}
		pukeGameQueryService.finish(pukeGameValueObject);
		String gameId = pukeGameValueObject.getId();
		JuResultDbo juResultDbo = pukePlayQueryService.findJuResultDbo(gameId);
		// 记录战绩
		if (juResultDbo != null) {
			PukeGameDbo pukeGameDbo = pukeGameQueryService.findPukeGameDboById(gameId);
			PukeHistoricalJuResult juResult = new PukeHistoricalJuResult(juResultDbo, pukeGameDbo);
			doudizhuResultMsgService.recordJuResult(juResult);
		}
		if (pukeGameValueObject.getState().name().equals(FinishedByVote.name)
				|| pukeGameValueObject.getState().name().equals(Canceled.name)) {
			data.put("queryScope", QueryScope.gameInfo);
		} else {
			// 游戏没结束有两种可能：一种是发起了投票。还有一种是游戏没开始，解散发起人又不是房主，那就自己走人。
			if (pukeGameValueObject.allPlayerIds().contains(playerId)) {
				data.put("queryScope", QueryScope.gameFinishVote);
			} else {
				data.put("queryScope", null);
				gameMsgService.gamePlayerLeave(pukeGameValueObject, playerId);
			}
		}

		// 通知其他人来查询
		for (String otherPlayerId : pukeGameValueObject.allPlayerIds()) {
			if (!otherPlayerId.equals(playerId)) {
				GamePlayerOnlineState onlineState = pukeGameValueObject.findPlayerOnlineState(otherPlayerId);
				if (onlineState.equals(GamePlayerOnlineState.online)) {
					List<QueryScope> scopes = QueryScope.scopesForState(pukeGameValueObject.getState(),
							pukeGameValueObject.findPlayerState(otherPlayerId));
					scopes.remove(QueryScope.panResult);
					wsNotifier.notifyToQuery(otherPlayerId, scopes);
				}
			}
		}
		return vo;
	}

	@RequestMapping(value = "/vote_to_finish")
	@ResponseBody
	public CommonVO votetofinish(String token, boolean yes) {
		CommonVO vo = new CommonVO();
		Map data = new HashMap();
		vo.setData(data);
		String playerId = playerAuthService.getPlayerIdByToken(token);
		if (playerId == null) {
			vo.setSuccess(false);
			vo.setMsg("invalid token");
			return vo;
		}
		PukeGameValueObject pukeGameValueObject;
		try {
			pukeGameValueObject = gameCmdService.voteToFinish(playerId, yes);
		} catch (Exception e) {
			vo.setSuccess(false);
			vo.setMsg(e.getClass().getName());
			return vo;
		}
		pukeGameQueryService.voteToFinish(pukeGameValueObject);
		String gameId = pukeGameValueObject.getId();
		JuResultDbo juResultDbo = pukePlayQueryService.findJuResultDbo(gameId);
		// 记录战绩
		if (juResultDbo != null) {
			PukeGameDbo pukeGameDbo = pukeGameQueryService.findPukeGameDboById(gameId);
			PukeHistoricalJuResult juResult = new PukeHistoricalJuResult(juResultDbo, pukeGameDbo);
			doudizhuResultMsgService.recordJuResult(juResult);
		}
		if (pukeGameValueObject.getState().name().equals(FinishedByVote.name)
				|| pukeGameValueObject.getState().name().equals(Canceled.name)) {
			gameMsgService.gameFinished(gameId);
		}
		data.put("queryScope", QueryScope.gameFinishVote);
		// 通知其他人来查询投票情况
		for (String otherPlayerId : pukeGameValueObject.allPlayerIds()) {
			if (!otherPlayerId.equals(playerId)) {
				GamePlayerOnlineState onlineState = pukeGameValueObject.findPlayerOnlineState(otherPlayerId);
				if (onlineState.equals(GamePlayerOnlineState.online)) {
					List<QueryScope> scopes = QueryScope.scopesForState(pukeGameValueObject.getState(),
							pukeGameValueObject.findPlayerState(otherPlayerId));
					scopes.remove(QueryScope.panResult);
					wsNotifier.notifyToQuery(otherPlayerId, scopes);
				}
			}
		}
		return vo;
	}

	/**
	 * 投票倒计时结束弃权
	 */
	@RequestMapping(value = "/timeover_to_waiver")
	@ResponseBody
	public CommonVO timeoverToWaiver(String token) {
		CommonVO vo = new CommonVO();
		Map data = new HashMap();
		vo.setData(data);
		String playerId = playerAuthService.getPlayerIdByToken(token);
		if (playerId == null) {
			vo.setSuccess(false);
			vo.setMsg("invalid token");
			return vo;
		}

		PukeGameValueObject pukeGameValueObject;
		try {
			pukeGameValueObject = gameCmdService.voteToFinishByTimeOver(playerId, System.currentTimeMillis());
		} catch (Exception e) {
			vo.setSuccess(false);
			vo.setMsg(e.getClass().getName());
			return vo;
		}
		pukeGameQueryService.voteToFinish(pukeGameValueObject);
		String gameId = pukeGameValueObject.getId();
		JuResultDbo juResultDbo = pukePlayQueryService.findJuResultDbo(gameId);
		// 记录战绩
		if (juResultDbo != null) {
			PukeGameDbo pukeGameDbo = pukeGameQueryService.findPukeGameDboById(gameId);
			PukeHistoricalJuResult juResult = new PukeHistoricalJuResult(juResultDbo, pukeGameDbo);
			doudizhuResultMsgService.recordJuResult(juResult);
		}
		if (pukeGameValueObject.getState().name().equals(FinishedByVote.name)
				|| pukeGameValueObject.getState().name().equals(Canceled.name)) {
			gameMsgService.gameFinished(gameId);
		}
		data.put("queryScope", QueryScope.gameFinishVote);
		// 通知其他人来查询投票情况
		for (String otherPlayerId : pukeGameValueObject.allPlayerIds()) {
			if (!otherPlayerId.equals(playerId)) {
				GamePlayerOnlineState onlineState = pukeGameValueObject.findPlayerOnlineState(otherPlayerId);
				if (onlineState.equals(GamePlayerOnlineState.online)) {
					List<QueryScope> scopes = QueryScope.scopesForState(pukeGameValueObject.getState(),
							pukeGameValueObject.findPlayerState(otherPlayerId));
					scopes.remove(QueryScope.panResult);
					wsNotifier.notifyToQuery(otherPlayerId, scopes);
				}
			}
		}
		return vo;
	}

	@RequestMapping(value = "/finish_vote_info")
	@ResponseBody
	public CommonVO finishvoteinfo(String gameId) {
		CommonVO vo = new CommonVO();
		GameFinishVoteDbo gameFinishVoteDbo = pukeGameQueryService.findGameFinishVoteDbo(gameId);
		Map data = new HashMap();
		data.put("vote", new GameFinishVoteVO(gameFinishVoteDbo.getVote()));
		vo.setData(data);
		return vo;

	}

	@RequestMapping(value = "/wisecrack")
	@ResponseBody
	public CommonVO wisecrack(String token, String gameId, String ordinal) {
		CommonVO vo = new CommonVO();
		String playerId = playerAuthService.getPlayerIdByToken(token);
		if (playerId == null) {
			vo.setSuccess(false);
			vo.setMsg("invalid token");
			return vo;
		}
		PukeGameDbo pukeGameDbo = pukeGameQueryService.findPukeGameDboById(gameId);
		if (!ordinal.contains("qiaopihuafy")) {
			// 通知其他人
			for (PukeGamePlayerDbo otherPlayer : pukeGameDbo.getPlayers()) {
				if (!otherPlayer.getPlayerId().equals(playerId)) {
					wsNotifier.notifyToListenWisecrack(otherPlayer.getPlayerId(), ordinal, playerId);
				}
			}
			wiseCrackMsgServcie.wisecrack(playerId);
			vo.setSuccess(true);
			return vo;
		}
		MemberGoldBalance account = memberGoldBalanceService.findByMemberId(playerId);
		if (account.getBalanceAfter() > 10) {
			memberGoldsMsgService.withdraw(playerId, 10, "wisecrack");
			// 通知其他人
			for (PukeGamePlayerDbo otherPlayer : pukeGameDbo.getPlayers()) {
				if (!otherPlayer.getPlayerId().equals(playerId)) {
					wsNotifier.notifyToListenWisecrack(otherPlayer.getPlayerId(), ordinal, playerId);
				}
			}
			wiseCrackMsgServcie.wisecrack(playerId);
			vo.setSuccess(true);
			return vo;
		}
		vo.setSuccess(false);
		vo.setMsg("InsufficientBalanceException");
		return vo;
	}

	@RequestMapping(value = "/playback")
	@ResponseBody
	public CommonVO playback(String gameId, int panNo) {
		CommonVO vo = new CommonVO();
		Map data = new HashMap();
		vo.setData(data);
		return vo;
	}

	@RequestMapping(value = "/speak")
	@ResponseBody
	public CommonVO speak(String token, String gameId, String wordId) {
		CommonVO vo = new CommonVO();
		String playerId = playerAuthService.getPlayerIdByToken(token);
		if (playerId == null) {
			vo.setSuccess(false);
			vo.setMsg("invalid token");
			return vo;
		}
		PukeGameDbo pukeGameDbo = pukeGameQueryService.findPukeGameDboById(gameId);
		List<PukeGamePlayerDbo> playerList = pukeGameDbo.getPlayers();
		for (PukeGamePlayerDbo player : playerList) {
			if (!player.getPlayerId().equals(playerId)) {
				wsNotifier.notifyToListenSpeak(player.getPlayerId(), wordId, playerId, true);
			}
		}
		vo.setSuccess(true);
		return vo;
	}
}
