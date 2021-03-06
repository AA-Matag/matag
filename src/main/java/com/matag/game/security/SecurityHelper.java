package com.matag.game.security;

import java.util.Objects;

import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Component;

import com.matag.game.player.PlayerService;
import com.matag.game.status.GameStatus;

import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class SecurityHelper {
  private final PlayerService playerService;

  public SecurityToken extractSecurityToken(SimpMessageHeaderAccessor headerAccessor) {
    var sessionId = headerAccessor.getSessionId();
    var token = Objects.requireNonNull(headerAccessor.getNativeHeader("token")).get(0);
    var gameId = Objects.requireNonNull(headerAccessor.getNativeHeader("gameId")).get(0);
    return new SecurityToken(sessionId, token, gameId);
  }

  public void isPlayerAllowedToExecuteAction(GameStatus gameStatus, String sessionId) {
    var currentPlayer = playerService.getPlayerBySessionId(gameStatus, sessionId);
    if (!gameStatus.getTurn().getCurrentPhaseActivePlayer().equals(currentPlayer.getName())) {
      throw new SecurityException("Player " + currentPlayer.getName() + " is not allowed to execute an action. turn=[" + gameStatus.getTurn() + "]");
    }
  }
}
