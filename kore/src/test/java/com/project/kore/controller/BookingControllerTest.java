package com.project.kore.controller;

import com.project.kore.dto.request.BookingRequest;
import com.project.kore.dto.response.BookingResponse;
import com.project.kore.enums.BookingStatus;
import com.project.kore.enums.Role;
import com.project.kore.facade.BookingFacade;
import com.project.kore.model.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingControllerTest {

    @Mock private BookingFacade bookingFacade;

    @InjectMocks
    private BookingController bookingController;

    @Test
    @DisplayName("createBooking — restituisce 200 con la prenotazione")
    void createBooking() {
        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setEmail("test@test.com");
        mockUser.setPassword("testpass");
        mockUser.setRole(Role.CLIENT);
        BookingRequest req = new BookingRequest(10L);
        BookingResponse resp = BookingResponse.builder().id(1L).status(BookingStatus.CONFIRMED).build();
        when(bookingFacade.createBooking(req, 1L)).thenReturn(resp);

        ResponseEntity<BookingResponse> response = bookingController.createBooking(req, mockUser);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody().getStatus()).isEqualTo(BookingStatus.CONFIRMED);
    }
}
