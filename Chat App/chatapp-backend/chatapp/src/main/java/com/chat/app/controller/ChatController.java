	package com.chat.app.controller;
	
	import org.springframework.messaging.handler.annotation.DestinationVariable;
	import org.springframework.messaging.handler.annotation.MessageMapping;
	import org.springframework.messaging.simp.SimpMessagingTemplate;
	import org.springframework.security.core.Authentication;
	import org.springframework.stereotype.Controller;
	
	import com.chat.app.dto.request.SendMessageDTO;
	import com.chat.app.dto.response.MessageResponseDTO;
	import com.chat.app.service.MessageService;
	import com.chat.app.service.UserService;
	
	import lombok.RequiredArgsConstructor;
	
	@Controller
	@RequiredArgsConstructor
	public class ChatController {
	
	    private final SimpMessagingTemplate messagingTemplate;
	    private final MessageService messageService;
	    private final UserService userService;
	
	    @MessageMapping("/conversations/{conversationId}/sendMessage")
	    public void sendMessage(
	            @DestinationVariable Long conversationId,
	            SendMessageDTO request,
	            Authentication authentication) {
	
	        Long senderId = userService
	                .getUserByEmail(authentication.getName())
	                .getId();
	
	        request.setConversationId(conversationId);
	
	        MessageResponseDTO response =
	                messageService.sendMessage(senderId, request);
	
	        messagingTemplate.convertAndSend(
	                "/topic/conversations/" + conversationId,
	                response
	        );
	    }
	}
