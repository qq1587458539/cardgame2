package com.mythostrike.controller.message.game;

import java.util.List;

public record SelectTargetRequest(int lobbyId, List<String> targets) {
}
