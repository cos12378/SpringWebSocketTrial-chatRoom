package com.example.springwebsocketchattingroomtest1.chattingRoom.event;

import com.example.springwebsocketchattingroomtest1.chattingRoom.model.ApplicationStats;
import com.example.springwebsocketchattingroomtest1.chattingRoom.model.Message;
import com.example.springwebsocketchattingroomtest1.chattingRoom.utils.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
public class WebSocketEventListener {

	private static final Logger logger = LoggerFactory.getLogger(WebSocketEventListener.class);

	@Autowired
	SimpMessagingTemplate template;

	@EventListener
	public void handleWebSocketConnectListener(SessionConnectedEvent event) {

		// Increment the new user count.
		ApplicationStats.incrementUserCount();

		logger.info("유저가 연결 되었습니다.");
	}

	@EventListener
	public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {

		StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
		String username = headerAccessor.getSessionAttributes().get("username").toString();

		if (username != null) {

			// Decrement the user count.
			ApplicationStats.decrementUserCount();

			logger.info(String.format("{}가 연결이 끊겼습니다.%s", username));

			Message message = new Message
					.Builder(username, Constants.USER_LEFT)
					.ofType(Message.Type.LEAVE)
					.build();

			// Notify everyone in the chat about user the left user.
			template.convertAndSend("/topic/public", message);
		}
	}
}
