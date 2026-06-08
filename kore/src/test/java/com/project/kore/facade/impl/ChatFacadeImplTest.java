package com.project.kore.facade.impl;

import com.project.kore.dto.request.SendMessageRequest;
import com.project.kore.dto.response.ChatMessageResponse;
import com.project.kore.dto.response.ClientBasicInfoResponse;
import com.project.kore.dto.response.ConversationPreviewResponse;
import com.project.kore.enums.ChatStatus;
import com.project.kore.enums.Role;
import com.project.kore.exception.chat.ChatNotAllowedException;
import com.project.kore.exception.common.CustomResourceNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import com.project.kore.mapper.ChatMapper;
import com.project.kore.mapper.UserMapper;
import com.project.kore.model.Chat;
import com.project.kore.model.Message;
import com.project.kore.model.User;
import com.project.kore.service.ChatService;
import com.project.kore.service.MessageService;
import com.project.kore.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ChatFacadeImpl unit tests")
class ChatFacadeImplTest {

    @Mock private ChatService chatService;
    @Mock private MessageService messageService;
    @Mock private ChatMapper chatMapper;
    @Mock private UserService userService;
    @Mock private UserMapper userMapper;

    @InjectMocks
    private ChatFacadeImpl chatFacade;

    private User clientUser;
    private User ptUser;
    private User moderatorUser;
    private User adminUser;
    private User insuranceUser;
    private Chat openChat;
    private Chat closedChat;

    @BeforeEach
    void setUp() {
        clientUser = new User();
        clientUser.setId(1L);
        clientUser.setFirstName("Luca");
        clientUser.setLastName("Rossi");
        clientUser.setRole(Role.CLIENT);

        ptUser = new User();
        ptUser.setId(2L);
        ptUser.setFirstName("Marco");
        ptUser.setLastName("PT");
        ptUser.setRole(Role.PERSONAL_TRAINER);

        moderatorUser = new User();
        moderatorUser.setId(5L);
        moderatorUser.setFirstName("Mod");
        moderatorUser.setLastName("Uno");
        moderatorUser.setRole(Role.MODERATOR);

        adminUser = new User();
        adminUser.setId(6L);
        adminUser.setFirstName("Admin");
        adminUser.setLastName("Zero");
        adminUser.setRole(Role.ADMIN);

        insuranceUser = new User();
        insuranceUser.setId(7L);
        insuranceUser.setFirstName("Ins");
        insuranceUser.setLastName("Man");
        insuranceUser.setRole(Role.INSURANCE_MANAGER);

        openChat = new Chat();
        openChat.setId(10L);
        openChat.setUser1(clientUser);
        openChat.setUser2(ptUser);
        openChat.setStatus(ChatStatus.OPEN);

        closedChat = new Chat();
        closedChat.setId(11L);
        closedChat.setUser1(clientUser);
        closedChat.setUser2(ptUser);
        closedChat.setStatus(ChatStatus.CLOSED);
    }

    // ─── createChat ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("createChat: same sender and receiver → throws IllegalArgumentException")
    void createChat_sameIds_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> chatFacade.createChat(1L, 1L))
                .isInstanceOf(IllegalArgumentException.class);

        verify(userService, never()).getUserById(any());
    }

    @Test
    @DisplayName("createChat: ADMIN talks to anyone → allowed, returns chat id")
    void createChat_adminWithAnyUser_isAllowed() {
        when(userService.getUserById(6L)).thenReturn(adminUser);
        when(userService.getUserById(1L)).thenReturn(clientUser);
        when(chatService.getOrCreateChat(adminUser, clientUser)).thenReturn(10L);

        Long chatId = chatFacade.createChat(6L, 1L);

        assertThat(chatId).isEqualTo(10L);
    }

    @Test
    @DisplayName("createChat: CLIENT↔assigned PT → allowed")
    void createChat_clientWithAssignedPT_isAllowed() {
        clientUser.setAssignedPT(ptUser);

        when(userService.getUserById(1L)).thenReturn(clientUser);
        when(userService.getUserById(2L)).thenReturn(ptUser);
        when(chatService.getOrCreateChat(clientUser, ptUser)).thenReturn(10L);

        Long chatId = chatFacade.createChat(1L, 2L);

        assertThat(chatId).isEqualTo(10L);
    }

    @Test
    @DisplayName("createChat: CLIENT↔unassigned PT → throws ChatNotAllowedException")
    void createChat_clientWithUnassignedPT_throwsChatNotAllowedException() {
        clientUser.setAssignedPT(null);

        when(userService.getUserById(1L)).thenReturn(clientUser);
        when(userService.getUserById(2L)).thenReturn(ptUser);

        assertThatThrownBy(() -> chatFacade.createChat(1L, 2L))
                .isInstanceOf(ChatNotAllowedException.class);
    }

    @Test
    @DisplayName("createChat: CLIENT↔Nutritionist assigned → allowed")
    void createChat_clientWithAssignedNutri_isAllowed() {
        User nutriUser = new User();
        nutriUser.setId(3L);
        nutriUser.setRole(Role.NUTRITIONIST);
        clientUser.setAssignedNutritionist(nutriUser);

        when(userService.getUserById(1L)).thenReturn(clientUser);
        when(userService.getUserById(3L)).thenReturn(nutriUser);
        when(chatService.getOrCreateChat(clientUser, nutriUser)).thenReturn(12L);

        Long chatId = chatFacade.createChat(1L, 3L);

        assertThat(chatId).isEqualTo(12L);
    }

    @Test
    @DisplayName("createChat: CLIENT↔unassigned Nutritionist → throws ChatNotAllowedException")
    void createChat_clientWithUnassignedNutri_throwsChatNotAllowedException() {
        User nutriUser = new User();
        nutriUser.setId(3L);
        nutriUser.setRole(Role.NUTRITIONIST);
        clientUser.setAssignedNutritionist(null);

        when(userService.getUserById(1L)).thenReturn(clientUser);
        when(userService.getUserById(3L)).thenReturn(nutriUser);

        assertThatThrownBy(() -> chatFacade.createChat(1L, 3L))
                .isInstanceOf(ChatNotAllowedException.class);
    }

    @Test
    @DisplayName("createChat: MODERATOR talks to any user → allowed")
    void createChat_moderatorWithClient_isAllowed() {
        when(userService.getUserById(5L)).thenReturn(moderatorUser);
        when(userService.getUserById(1L)).thenReturn(clientUser);
        when(chatService.getOrCreateChat(moderatorUser, clientUser)).thenReturn(13L);

        Long chatId = chatFacade.createChat(5L, 1L);

        assertThat(chatId).isEqualTo(13L);
    }

    @Test
    @DisplayName("createChat: INSURANCE_MANAGER with MODERATOR → allowed")
    void createChat_insuranceWithModerator_isAllowed() {
        when(userService.getUserById(7L)).thenReturn(insuranceUser);
        when(userService.getUserById(5L)).thenReturn(moderatorUser);
        when(chatService.getOrCreateChat(insuranceUser, moderatorUser)).thenReturn(14L);

        Long chatId = chatFacade.createChat(7L, 5L);

        assertThat(chatId).isEqualTo(14L);
    }

    @Test
    @DisplayName("createChat: INSURANCE_MANAGER with CLIENT → throws ChatNotAllowedException")
    void createChat_insuranceWithClient_throwsChatNotAllowedException() {
        when(userService.getUserById(7L)).thenReturn(insuranceUser);
        when(userService.getUserById(1L)).thenReturn(clientUser);

        assertThatThrownBy(() -> chatFacade.createChat(7L, 1L))
                .isInstanceOf(ChatNotAllowedException.class);
    }

    @Test
    @DisplayName("createChat: both users have non-CLIENT roles and no admin/moderator → throws ChatNotAllowedException")
    void createChat_twoPTUsers_throwsChatNotAllowedException() {
        User pt2 = new User();
        pt2.setId(9L);
        pt2.setRole(Role.PERSONAL_TRAINER);

        when(userService.getUserById(2L)).thenReturn(ptUser);
        when(userService.getUserById(9L)).thenReturn(pt2);

        assertThatThrownBy(() -> chatFacade.createChat(2L, 9L))
                .isInstanceOf(ChatNotAllowedException.class);
    }

    // ─── sendMessage ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("sendMessage: chat not found → throws ResourceNotFoundException")
    void sendMessage_chatNotFound_throwsResourceNotFoundException() {
        SendMessageRequest request = new SendMessageRequest(99L, "hello");

        when(chatService.getChatEntity(99L)).thenReturn(null);

        assertThatThrownBy(() -> chatFacade.sendMessage(request, 1L))
                .isInstanceOf(CustomResourceNotFoundException.class);
    }

    @Test
    @DisplayName("sendMessage: sender is not part of chat → throws ChatNotAllowedException")
    void sendMessage_senderNotInChat_throwsChatNotAllowedException() {
        SendMessageRequest request = new SendMessageRequest(10L, "hello");

        User stranger = new User();
        stranger.setId(99L);
        stranger.setRole(Role.CLIENT);

        when(chatService.getChatEntity(10L)).thenReturn(openChat);
        when(userService.getUserById(99L)).thenReturn(stranger);

        assertThatThrownBy(() -> chatFacade.sendMessage(request, 99L))
                .isInstanceOf(ChatNotAllowedException.class);
    }

    @Test
    @DisplayName("sendMessage: open chat, sender is user1 → saves and returns message response")
    void sendMessage_openChat_senderIsUser1_savesMessage() {
        SendMessageRequest request = new SendMessageRequest(10L, "hello");

        Message message = new Message();
        message.setId(50L);
        message.setChat(openChat);
        message.setContent("hello");

        ChatMessageResponse expectedResponse = ChatMessageResponse.builder()
                .id(50L).chatId(10L).senderId(1L).content("hello").build();

        when(chatService.getChatEntity(10L)).thenReturn(openChat);
        when(userService.getUserById(1L)).thenReturn(clientUser);
        when(messageService.saveMessage(openChat, clientUser, "hello")).thenReturn(message);
        when(chatMapper.toMessageResponse(message)).thenReturn(expectedResponse);

        ChatMessageResponse result = chatFacade.sendMessage(request, 1L);

        assertThat(result).isEqualTo(expectedResponse);
        verify(chatService, never()).save(any());
    }

    @Test
    @DisplayName("sendMessage: sender is user2 → saves and returns message response")
    void sendMessage_openChat_senderIsUser2_savesMessage() {
        SendMessageRequest request = new SendMessageRequest(10L, "ciao");

        Message message = new Message();
        message.setId(51L);
        message.setChat(openChat);
        message.setContent("ciao");

        ChatMessageResponse expectedResponse = ChatMessageResponse.builder()
                .id(51L).chatId(10L).senderId(2L).content("ciao").build();

        when(chatService.getChatEntity(10L)).thenReturn(openChat);
        when(userService.getUserById(2L)).thenReturn(ptUser);
        when(messageService.saveMessage(openChat, ptUser, "ciao")).thenReturn(message);
        when(chatMapper.toMessageResponse(message)).thenReturn(expectedResponse);

        ChatMessageResponse result = chatFacade.sendMessage(request, 2L);

        assertThat(result).isEqualTo(expectedResponse);
    }

    @Test
    @DisplayName("sendMessage: CLOSED chat → reopens chat before saving message")
    void sendMessage_closedChat_reopensAndSavesMessage() {
        SendMessageRequest request = new SendMessageRequest(11L, "reopen me");

        Message message = new Message();
        message.setId(52L);
        message.setChat(closedChat);
        message.setContent("reopen me");

        ChatMessageResponse expectedResponse = ChatMessageResponse.builder()
                .id(52L).chatId(11L).content("reopen me").build();

        when(chatService.getChatEntity(11L)).thenReturn(closedChat);
        when(chatService.save(closedChat)).thenReturn(closedChat);
        when(userService.getUserById(1L)).thenReturn(clientUser);
        when(messageService.saveMessage(closedChat, clientUser, "reopen me")).thenReturn(message);
        when(chatMapper.toMessageResponse(message)).thenReturn(expectedResponse);

        chatFacade.sendMessage(request, 1L);

        assertThat(closedChat.getStatus()).isEqualTo(ChatStatus.OPEN);
        verify(chatService).save(closedChat);
    }

    // ─── getConversation ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("getConversation: chat not found → throws ResourceNotFoundException")
    void getConversation_chatNotFound_throwsResourceNotFoundException() {
        when(chatService.getChatEntity(99L)).thenReturn(null);

        assertThatThrownBy(() -> chatFacade.getConversation(99L, 1L, 0, 20))
                .isInstanceOf(CustomResourceNotFoundException.class);
    }

    @Test
    @DisplayName("getConversation: userId not part of chat → throws ChatNotAllowedException")
    void getConversation_userNotInChat_throwsChatNotAllowedException() {
        when(chatService.getChatEntity(10L)).thenReturn(openChat);

        assertThatThrownBy(() -> chatFacade.getConversation(10L, 99L, 0, 20))
                .isInstanceOf(ChatNotAllowedException.class);
    }

    @Test
    @DisplayName("getConversation: valid user → returns paged message response list")
    void getConversation_validUser_returnsMessageList() {
        Message msg = new Message();
        msg.setId(1L);
        List<Message> messages = List.of(msg);

        ChatMessageResponse msgResponse = ChatMessageResponse.builder().id(1L).build();
        List<ChatMessageResponse> responses = List.of(msgResponse);

        when(chatService.getChatEntity(10L)).thenReturn(openChat);
        when(messageService.getMessages(10L, 0, 20)).thenReturn(messages);
        when(chatMapper.toMessageResponseList(messages)).thenReturn(responses);

        List<ChatMessageResponse> result = chatFacade.getConversation(10L, 1L, 0, 20);

        assertThat(result).isEqualTo(responses);
    }

    // ─── getUserConversations ─────────────────────────────────────────────────────

    @Test
    @DisplayName("getUserConversations: CLIENT with chat to ADMIN with no messages → filtered out")
    void getUserConversations_clientAdminEmptyChat_isFilteredOut() {
        Chat adminChat = new Chat();
        adminChat.setId(20L);
        adminChat.setUser1(clientUser);
        adminChat.setUser2(adminUser);
        adminChat.setStatus(ChatStatus.OPEN);

        ConversationPreviewResponse emptyAdminPreview = ConversationPreviewResponse.builder()
                .chatId(20L).otherUserRole("ADMIN").lastMessageTime(null).build();

        when(chatService.getUserConversations(1L)).thenReturn(List.of(adminChat));
        when(userService.getUserById(1L)).thenReturn(clientUser);
        when(messageService.getLastMessage(20L)).thenReturn(null);
        when(messageService.getUnreadCount(20L, 1L)).thenReturn(0);
        when(chatMapper.toConversationPreview(adminChat, 1L, null, 0)).thenReturn(emptyAdminPreview);

        List<ConversationPreviewResponse> result = chatFacade.getUserConversations(1L);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("getUserConversations: CLIENT with chat to MODERATOR with no messages → filtered out")
    void getUserConversations_clientModeratorEmptyChat_isFilteredOut() {
        Chat modChat = new Chat();
        modChat.setId(21L);
        modChat.setUser1(clientUser);
        modChat.setUser2(moderatorUser);
        modChat.setStatus(ChatStatus.OPEN);

        ConversationPreviewResponse emptyModPreview = ConversationPreviewResponse.builder()
                .chatId(21L).otherUserRole("MODERATOR").lastMessageTime(null).build();

        when(chatService.getUserConversations(1L)).thenReturn(List.of(modChat));
        when(userService.getUserById(1L)).thenReturn(clientUser);
        when(messageService.getLastMessage(21L)).thenReturn(null);
        when(messageService.getUnreadCount(21L, 1L)).thenReturn(0);
        when(chatMapper.toConversationPreview(modChat, 1L, null, 0)).thenReturn(emptyModPreview);

        List<ConversationPreviewResponse> result = chatFacade.getUserConversations(1L);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("getUserConversations: CLIENT with chat to ADMIN with messages → NOT filtered")
    void getUserConversations_clientAdminChatWithMessages_isIncluded() {
        Chat adminChat = new Chat();
        adminChat.setId(20L);
        adminChat.setUser1(clientUser);
        adminChat.setUser2(adminUser);
        adminChat.setStatus(ChatStatus.OPEN);

        Message lastMsg = new Message();
        lastMsg.setId(1L);
        lastMsg.setTimeStamp(LocalDateTime.now());

        ConversationPreviewResponse previewWithTime = ConversationPreviewResponse.builder()
                .chatId(20L).otherUserRole("ADMIN").lastMessageTime(LocalDateTime.now()).build();

        when(chatService.getUserConversations(1L)).thenReturn(List.of(adminChat));
        when(userService.getUserById(1L)).thenReturn(clientUser);
        when(messageService.getLastMessage(20L)).thenReturn(lastMsg);
        when(messageService.getUnreadCount(20L, 1L)).thenReturn(1);
        when(chatMapper.toConversationPreview(adminChat, 1L, lastMsg, 1)).thenReturn(previewWithTime);

        List<ConversationPreviewResponse> result = chatFacade.getUserConversations(1L);

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("getUserConversations: ADMIN with empty ADMIN-client chat → NOT filtered (admin is exempt)")
    void getUserConversations_adminEmptyChat_isIncluded() {
        Chat adminClientChat = new Chat();
        adminClientChat.setId(22L);
        adminClientChat.setUser1(adminUser);
        adminClientChat.setUser2(clientUser);
        adminClientChat.setStatus(ChatStatus.OPEN);

        ConversationPreviewResponse emptyPreview = ConversationPreviewResponse.builder()
                .chatId(22L).otherUserRole("CLIENT").lastMessageTime(null).build();

        when(chatService.getUserConversations(6L)).thenReturn(List.of(adminClientChat));
        when(userService.getUserById(6L)).thenReturn(adminUser);
        when(messageService.getLastMessage(22L)).thenReturn(null);
        when(messageService.getUnreadCount(22L, 6L)).thenReturn(0);
        when(chatMapper.toConversationPreview(adminClientChat, 6L, null, 0)).thenReturn(emptyPreview);

        List<ConversationPreviewResponse> result = chatFacade.getUserConversations(6L);

        // ADMIN is not CLIENT/PT/NUTRITIONIST so the filter does not apply
        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("getUserConversations: PT with empty PT chat → NOT filtered (other role is not ADMIN/MODERATOR)")
    void getUserConversations_ptEmptyChatWithClient_isIncluded() {
        Chat ptClientChat = new Chat();
        ptClientChat.setId(23L);
        ptClientChat.setUser1(ptUser);
        ptClientChat.setUser2(clientUser);
        ptClientChat.setStatus(ChatStatus.OPEN);

        ConversationPreviewResponse emptyClientPreview = ConversationPreviewResponse.builder()
                .chatId(23L).otherUserRole("CLIENT").lastMessageTime(null).build();

        when(chatService.getUserConversations(2L)).thenReturn(List.of(ptClientChat));
        when(userService.getUserById(2L)).thenReturn(ptUser);
        when(messageService.getLastMessage(23L)).thenReturn(null);
        when(messageService.getUnreadCount(23L, 2L)).thenReturn(0);
        when(chatMapper.toConversationPreview(ptClientChat, 2L, null, 0)).thenReturn(emptyClientPreview);

        List<ConversationPreviewResponse> result = chatFacade.getUserConversations(2L);

        // Other user is CLIENT, not ADMIN/MODERATOR, so not filtered
        assertThat(result).hasSize(1);
    }

    // ─── markAsRead ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("markAsRead: delegates to messageService")
    void markAsRead_delegatesToMessageService() {
        chatFacade.markAsRead(10L, 1L);

        verify(messageService).markAsRead(10L, 1L);
    }

    // ─── getTotalUnreadCount ──────────────────────────────────────────────────────

    @Test
    @DisplayName("getTotalUnreadCount: delegates to messageService and returns count")
    void getTotalUnreadCount_returnsCount() {
        when(messageService.getTotalUnreadCount(1L)).thenReturn(7);

        Integer result = chatFacade.getTotalUnreadCount(1L);

        assertThat(result).isEqualTo(7);
    }

    // ─── closeChat ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("closeChat: moderator role → delegates to chatService.closeChat")
    void closeChat_moderator_closesChat() {
        when(userService.getUserById(5L)).thenReturn(moderatorUser);

        chatFacade.closeChat(10L, 5L);

        verify(chatService).closeChat(10L, moderatorUser);
    }

    @Test
    @DisplayName("closeChat: admin role → delegates to chatService.closeChat")
    void closeChat_admin_closesChat() {
        when(userService.getUserById(6L)).thenReturn(adminUser);

        chatFacade.closeChat(10L, 6L);

        verify(chatService).closeChat(10L, adminUser);
    }

    @Test
    @DisplayName("closeChat: client role → throws AccessDeniedException")
    void closeChat_client_throwsAccessDeniedException() {
        when(userService.getUserById(1L)).thenReturn(clientUser);

        assertThatThrownBy(() -> chatFacade.closeChat(10L, 1L))
                .isInstanceOf(AccessDeniedException.class);

        verify(chatService, never()).closeChat(any(), any());
    }

    // ─── getModerator ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("getModerator: moderator user → throws AccessDeniedException")
    void getModerator_moderator_throwsAccessDeniedException() {
        assertThatThrownBy(() -> chatFacade.getModerator(moderatorUser))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @DisplayName("getModerator: admin user → throws AccessDeniedException")
    void getModerator_admin_throwsAccessDeniedException() {
        assertThatThrownBy(() -> chatFacade.getModerator(adminUser))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @DisplayName("getModerator: no moderators in system → throws ResourceNotFoundException")
    void getModerator_noModerators_throwsResourceNotFoundException() {
        when(userService.findByRole(Role.MODERATOR)).thenReturn(List.of());

        assertThatThrownBy(() -> chatFacade.getModerator(clientUser))
                .isInstanceOf(CustomResourceNotFoundException.class);
    }

    @Test
    @DisplayName("getModerator: client with no existing moderator conversation → selects least-busy moderator")
    void getModerator_noExistingConversation_selectsLeastBusyModerator() {
        User mod1 = new User(); mod1.setId(30L); mod1.setRole(Role.MODERATOR);
        User mod2 = new User(); mod2.setId(31L); mod2.setRole(Role.MODERATOR);

        ClientBasicInfoResponse mod1Response = ClientBasicInfoResponse.builder()
                .id(30L).email("mod1@test.com").build();

        when(userService.findByRole(Role.MODERATOR)).thenReturn(List.of(mod1, mod2));
        when(chatService.getUserConversations(1L)).thenReturn(List.of());
        when(chatService.countOpenChatsByModerator(30L)).thenReturn(2L);
        when(chatService.countOpenChatsByModerator(31L)).thenReturn(5L);
        when(userMapper.toBasicInfoResponse(mod1)).thenReturn(mod1Response);

        ClientBasicInfoResponse result = chatFacade.getModerator(clientUser);

        assertThat(result).isEqualTo(mod1Response);
    }

    @Test
    @DisplayName("getModerator: client already has conversation with moderator → reuses existing moderator")
    void getModerator_existingConversationWithModerator_reusesExistingModerator() {
        Chat existingModChat = new Chat();
        existingModChat.setId(40L);
        existingModChat.setUser1(clientUser);
        existingModChat.setUser2(moderatorUser);

        ClientBasicInfoResponse moderatorResponse = ClientBasicInfoResponse.builder()
                .id(5L).email("mod@test.com").build();

        when(userService.findByRole(Role.MODERATOR)).thenReturn(List.of(moderatorUser));
        when(chatService.getUserConversations(1L)).thenReturn(List.of(existingModChat));
        when(userMapper.toBasicInfoResponse(moderatorUser)).thenReturn(moderatorResponse);

        ClientBasicInfoResponse result = chatFacade.getModerator(clientUser);

        assertThat(result).isEqualTo(moderatorResponse);
        verify(chatService, never()).countOpenChatsByModerator(any());
    }

    @Test
    @DisplayName("getModerator: client has conversations but none with a moderator → selects least-busy moderator")
    void getModerator_conversationsButNoneWithModerator_selectsLeastBusy() {
        // Use two moderators so stream().min() actually invokes the comparator and countOpenChatsByModerator
        User mod1 = new User(); mod1.setId(50L); mod1.setRole(Role.MODERATOR);
        User mod2 = new User(); mod2.setId(51L); mod2.setRole(Role.MODERATOR);

        Chat chatWithPT = new Chat();
        chatWithPT.setId(41L);
        chatWithPT.setUser1(clientUser);
        chatWithPT.setUser2(ptUser);

        ClientBasicInfoResponse mod1Response = ClientBasicInfoResponse.builder()
                .id(50L).email("mod1@test.com").build();

        when(userService.findByRole(Role.MODERATOR)).thenReturn(List.of(mod1, mod2));
        when(chatService.getUserConversations(1L)).thenReturn(List.of(chatWithPT));
        when(chatService.countOpenChatsByModerator(50L)).thenReturn(1L);
        when(chatService.countOpenChatsByModerator(51L)).thenReturn(4L);
        when(userMapper.toBasicInfoResponse(mod1)).thenReturn(mod1Response);

        ClientBasicInfoResponse result = chatFacade.getModerator(clientUser);

        assertThat(result).isEqualTo(mod1Response);
    }

    @Test
    @DisplayName("getModerator: single moderator with no conversations → returns that moderator (min on 1-element list)")
    void getModerator_singleModerator_returnsIt() {
        // With a single-element list, stream().min() returns the only element without invoking the comparator,
        // so we do NOT stub countOpenChatsByModerator here (it would be flagged as unnecessary by Mockito).
        ClientBasicInfoResponse moderatorResponse = ClientBasicInfoResponse.builder()
                .id(5L).email("mod@test.com").build();

        when(userService.findByRole(Role.MODERATOR)).thenReturn(List.of(moderatorUser));
        when(chatService.getUserConversations(1L)).thenReturn(List.of());
        when(userMapper.toBasicInfoResponse(moderatorUser)).thenReturn(moderatorResponse);

        ClientBasicInfoResponse result = chatFacade.getModerator(clientUser);

        assertThat(result).isEqualTo(moderatorResponse);
    }

    @Test
    @DisplayName("getModerator: getUserConversations returns null → selects least-busy moderator (two moderators)")
    void getModerator_nullConversations_selectsLeastBusy() {
        // Use two moderators so the comparator is actually evaluated
        User mod1 = new User(); mod1.setId(60L); mod1.setRole(Role.MODERATOR);
        User mod2 = new User(); mod2.setId(61L); mod2.setRole(Role.MODERATOR);

        ClientBasicInfoResponse mod1Response = ClientBasicInfoResponse.builder()
                .id(60L).email("mod1@test.com").build();

        when(userService.findByRole(Role.MODERATOR)).thenReturn(List.of(mod1, mod2));
        when(chatService.getUserConversations(1L)).thenReturn(null);
        when(chatService.countOpenChatsByModerator(60L)).thenReturn(0L);
        when(chatService.countOpenChatsByModerator(61L)).thenReturn(3L);
        when(userMapper.toBasicInfoResponse(mod1)).thenReturn(mod1Response);

        ClientBasicInfoResponse result = chatFacade.getModerator(clientUser);

        assertThat(result).isEqualTo(mod1Response);
    }
}
