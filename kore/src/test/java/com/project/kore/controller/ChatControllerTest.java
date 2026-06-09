package com.project.kore.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.project.kore.dto.request.SendMessageRequest;
import com.project.kore.dto.response.ChatMessageResponse;
import com.project.kore.dto.response.ClientBasicInfoResponse;
import com.project.kore.dto.response.ConversationPreviewResponse;
import com.project.kore.enums.MessageStatus;
import com.project.kore.enums.Role;
import com.project.kore.exception.GlobalExceptionHandler;
import com.project.kore.exception.chat.ChatNotAllowedException;
import com.project.kore.facade.ChatFacade;
import com.project.kore.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ChatControllerTest {

    @Mock
    ChatFacade chatFacade;

    @InjectMocks
    ChatController controller;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper()
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private RequestPostProcessor withClientUser;
    private User mockUser;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .build();

        mockUser = new User();
        mockUser.setId(10L);
        mockUser.setEmail("luca@test.com");
        mockUser.setRole(Role.CLIENT);
        mockUser.setFirstName("Luca");
        mockUser.setLastName("Rossi");

        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                mockUser, null, List.of(new SimpleGrantedAuthority("ROLE_CLIENT")));

        withClientUser = (MockHttpServletRequest request) -> {
            SecurityContextHolder.getContext().setAuthentication(authToken);
            return request;
        };
    }

    // ------------------------------------------------------------------ POST /api/chat/create/{receiverId}

    @Test
    @DisplayName("POST /api/chat/create/{receiverId} — 200 con ID della chat")
    void createChat_returns200() throws Exception {
        when(chatFacade.createChat(anyLong(), anyLong())).thenReturn(42L);

        mockMvc.perform(post("/api/chat/create/20").with(withClientUser))
                .andExpect(status().isOk());
    }

    // ------------------------------------------------------------------ POST /api/chat/send

    @Test
    @DisplayName("POST /api/chat/send — 200 con messaggio inviato")
    void sendMessage_returns200() throws Exception {
        ChatMessageResponse response = ChatMessageResponse.builder()
                .id(1L).chatId(42L).senderId(10L).senderName("Luca Rossi")
                .content("Ciao!").status(MessageStatus.SENT)
                .createdAt(LocalDateTime.now())
                .build();
        when(chatFacade.sendMessage(any(SendMessageRequest.class), anyLong())).thenReturn(response);

        SendMessageRequest req = new SendMessageRequest(42L, "Ciao!");

        mockMvc.perform(post("/api/chat/send")
                        .with(withClientUser)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.content").value("Ciao!"));
    }

    // La validazione del contenuto non è più sul controller (niente @Valid) ma sul facade
    // (ChatFacade.sendMessage(@Valid SendMessageRequest), classe @Validated) e, in difesa, sull'entity
    // Message. Un contenuto vuoto viola @NotBlank → ConstraintViolationException → 400. Qui copriamo il vincolo.
    @Test
    @DisplayName("SendMessageRequest — contenuto vuoto viola @NotBlank (validato ora sul facade → 400)")
    void sendMessage_blankContent_violatesConstraint() {
        try (jakarta.validation.ValidatorFactory factory = jakarta.validation.Validation.buildDefaultValidatorFactory()) {
            jakarta.validation.Validator validator = factory.getValidator();
            SendMessageRequest req = new SendMessageRequest(42L, "");

            var violations = validator.validate(req);

            assertThat(violations).extracting(v -> v.getPropertyPath().toString())
                    .contains("content");
        }
    }

    // ------------------------------------------------------------------ GET /api/chat/conversation/{chatId}

    @Test
    @DisplayName("GET /api/chat/conversation/{chatId} — 200 con lista messaggi")
    void getConversation_returns200() throws Exception {
        ChatMessageResponse msg = ChatMessageResponse.builder()
                .id(1L).chatId(42L).senderId(10L).content("Ciao!")
                .status(MessageStatus.READ).createdAt(LocalDateTime.now())
                .build();
        when(chatFacade.getConversation(anyLong(), anyLong(), anyInt(), anyInt()))
                .thenReturn(List.of(msg));

        mockMvc.perform(get("/api/chat/conversation/42").with(withClientUser))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    // ------------------------------------------------------------------ GET /api/chat/conversations

    @Test
    @DisplayName("GET /api/chat/conversations — 200 con lista conversazioni")
    void getUserConversations_returns200() throws Exception {
        ConversationPreviewResponse preview = ConversationPreviewResponse.builder()
                .chatId(42L).otherUserId(20L).otherUserName("Mario Bianchi")
                .lastMessage("Ciao!").unreadCount(2)
                .build();
        when(chatFacade.getUserConversations(anyLong())).thenReturn(List.of(preview));

        mockMvc.perform(get("/api/chat/conversations").with(withClientUser))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].chatId").value(42))
                .andExpect(jsonPath("$[0].unreadCount").value(2));
    }

    // ------------------------------------------------------------------ PUT /api/chat/read/{chatId}

    @Test
    @DisplayName("PUT /api/chat/read/{chatId} — 200 quando messaggi marcati come letti")
    void markAsRead_returns200() throws Exception {
        doNothing().when(chatFacade).markAsRead(anyLong(), anyLong());

        mockMvc.perform(put("/api/chat/read/42").with(withClientUser))
                .andExpect(status().isOk());
    }

    // ------------------------------------------------------------------ GET /api/chat/unread

    @Test
    @DisplayName("GET /api/chat/unread — 200 con conteggio messaggi non letti")
    void getTotalUnreadCount_returns200() throws Exception {
        when(chatFacade.getTotalUnreadCount(anyLong())).thenReturn(5);

        mockMvc.perform(get("/api/chat/unread").with(withClientUser))
                .andExpect(status().isOk());
    }

    // ------------------------------------------------------------------ GET /api/chat/moderator

    @Test
    @DisplayName("GET /api/chat/moderator — 200 con info del moderatore assegnato")
    void getModerator_returns200() throws Exception {
        ClientBasicInfoResponse modInfo = ClientBasicInfoResponse.builder()
                .id(5L).firstName("Mod").lastName("Eratore")
                .email("moderator1@test.com").role("MODERATOR")
                .build();
        when(chatFacade.getModerator(any(User.class))).thenReturn(modInfo);

        mockMvc.perform(get("/api/chat/moderator").with(withClientUser))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5));
    }

    // ------------------------------------------------------------------ POST /api/chat/{chatId}/close

    @Test
    @DisplayName("POST /api/chat/{chatId}/close — 204 quando chat chiusa con successo")
    void closeChat_returns204() throws Exception {
        doNothing().when(chatFacade).closeChat(anyLong(), anyLong());

        mockMvc.perform(post("/api/chat/42/close").with(withClientUser))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("POST /api/chat/{chatId}/close — 403 quando non autorizzato")
    void closeChat_notAllowed_returns403() throws Exception {
        org.mockito.Mockito.doThrow(new ChatNotAllowedException("Non autorizzato"))
                .when(chatFacade).closeChat(anyLong(), anyLong());

        mockMvc.perform(post("/api/chat/42/close").with(withClientUser))
                .andExpect(status().isForbidden());
    }
}
