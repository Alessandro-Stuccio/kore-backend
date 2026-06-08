package com.project.kore.service;

import org.springframework.validation.annotation.Validated;

import com.project.kore.dto.request.JobApplicationRequest;
import com.project.kore.exception.email.EmailDeliveryException;
import org.springframework.web.multipart.MultipartFile;

/** Invio email tramite l'API di Resend. */
@Validated
public interface EmailService {

    /**
     * Inoltra all'admin una candidatura di lavoro con il CV in allegato. L'invio vero e proprio
     * avviene in modo asincrono; qui si validano gli input e si leggono i byte del CV.
     *
     * @param request dati della candidatura
     * @param cv      file del CV (opzionale); se presente deve essere un PDF
     * @throws IllegalArgumentException se il CV è presente ma non è in formato PDF
     */
    void sendJobApplication(JobApplicationRequest request, MultipartFile cv);

    /**
     * Parte asincrona dell'invio candidatura: il file è già stato letto in memoria. Eventuali
     * errori SMTP vengono loggati e non propagati (invio best-effort).
     *
     * @param request        dati della candidatura
     * @param cvBytes        contenuto binario del CV (può essere nullo)
     * @param cvFileName     nome del file CV da usare in allegato
     * @param cvContentType  MIME type dell'allegato
     */
    void sendEmailAsync(JobApplicationRequest request, byte[] cvBytes, String cvFileName, String cvContentType);

    /**
     * Email di benvenuto al nuovo utente registrato.
     *
     * @param toEmail   email del destinatario
     * @param firstName nome del destinatario
     * @throws IllegalArgumentException se l'email del destinatario è mancante
     * @throws EmailDeliveryException   se l'invio SMTP fallisce o la configurazione è assente
     */
    void sendWelcomeEmail(String toEmail, String firstName);

    /**
     * Promemoria per una prenotazione imminente. Il testo cambia se il destinatario è il cliente.
     *
     * @param toEmail        email del destinatario
     * @param recipientName  nome del destinatario
     * @param otherPartyName nome dell'altra parte dell'appuntamento
     * @param startTime      orario di inizio dell'appuntamento
     * @param meetingLink    link alla videoconferenza
     * @param isForClient    {@code true} se il destinatario è il cliente, {@code false} se è il professionista
     * @throws IllegalArgumentException se l'email del destinatario o il link meeting sono mancanti
     * @throws EmailDeliveryException   se l'invio SMTP fallisce o la configurazione è assente
     */
    void sendBookingReminderEmail(String toEmail, String recipientName, String otherPartyName,
                                   java.time.LocalDateTime startTime, String meetingLink, boolean isForClient);

    /**
     * Email con il link per reimpostare la password (resetToken da inserire nel link).
     *
     * @param toEmail    email del destinatario
     * @param firstName  nome del destinatario
     * @param resetToken token di reset da inserire nel link
     * @throws IllegalArgumentException se l'email del destinatario è mancante
     * @throws EmailDeliveryException   se l'invio SMTP fallisce o la configurazione è assente
     */
    void sendPasswordResetEmail(String toEmail, String firstName, String resetToken);

    /**
     * Notifica che la password è stata cambiata.
     *
     * @param toEmail   email del destinatario
     * @param firstName nome del destinatario
     * @throws IllegalArgumentException se l'email del destinatario è mancante
     * @throws EmailDeliveryException   se l'invio SMTP fallisce o la configurazione è assente
     */
    void sendPasswordChangeEmail(String toEmail, String firstName);

    /**
     * Conferma di una prenotazione appena creata, con il link Jitsi.
     *
     * @param toEmail        email del destinatario
     * @param recipientName  nome del destinatario
     * @param otherPartyName nome dell'altra parte dell'appuntamento
     * @param startTime      orario di inizio dell'appuntamento
     * @param meetingLink    link alla videoconferenza
     * @throws IllegalArgumentException se l'email del destinatario è mancante
     * @throws EmailDeliveryException   se l'invio SMTP fallisce o la configurazione è assente
     */
    void sendBookingConfirmationEmail(String toEmail, String recipientName, String otherPartyName,
                                   java.time.LocalDateTime startTime, String meetingLink);

    /**
     * Notifica l'annullamento di una prenotazione.
     *
     * @param toEmail        email del destinatario
     * @param recipientName  nome del destinatario
     * @param otherPartyName nome dell'altra parte dell'appuntamento
     * @param startTime      orario di inizio dell'appuntamento annullato
     * @throws IllegalArgumentException se l'email del destinatario è mancante
     * @throws EmailDeliveryException   se l'invio SMTP fallisce o la configurazione è assente
     */
    void sendBookingCancellationEmail(String toEmail, String recipientName, String otherPartyName,
                                   java.time.LocalDateTime startTime);
}
