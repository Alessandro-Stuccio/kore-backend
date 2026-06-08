package com.project.kore.service.impl;

import com.project.kore.dto.request.JobApplicationRequest;
import com.project.kore.exception.email.EmailDeliveryException;
import com.project.kore.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

/**
 * Invio email via SMTP. L'invio è lento e può andare in timeout, quindi quasi tutto gira
 * in @Async per non bloccare la richiesta HTTP. Ci iniettiamo l'interfaccia di noi stessi
 * (self) perché le chiamate asincrone interne passino dal proxy Spring, altrimenti @Async
 * verrebbe ignorato.
 */
@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailServiceImpl.class);

    @Value("${spring.mail.username}")
    private String mailFrom;
    private final String adminEmail="admin@example.com";
    private final EmailService self;
    private final JavaMailSender javaMailSender;

    @Autowired
    public EmailServiceImpl(
            Optional<JavaMailSender> javaMailSenderOptional,
            @Lazy EmailService self) {
        this.javaMailSender = javaMailSenderOptional.orElse(null);
        this.self = self;
    }

    @PostConstruct
    void validateEmailConfiguration() {
        log.info("Configurazione email attiva: provider='smtp', mail.from='{}'", mailFrom);
        if (javaMailSender == null) {
            log.error("Provider email impostato a SMTP ma JavaMailSender non è disponibile. Configura spring.mail.*.");
        }
        if (mailFrom == null || mailFrom.isBlank()) {
            log.error("Configurazione mittente mancante: imposta 'mail.from' o 'spring.mail.username'.");
        }
    }

    @Override
    public void sendJobApplication(JobApplicationRequest request, MultipartFile cv) {
        // Il CV è accettato solo in PDF.
        if (cv != null && !cv.isEmpty()) {
            String contentType = cv.getContentType();
            if (contentType == null || !contentType.equals("application/pdf")) {
                throw new IllegalArgumentException("Il CV deve essere in formato PDF.");
            }
        }

        // Leggiamo i byte del CV qui, nel thread chiamante: il MultipartFile non sopravvive
        // al passaggio nel thread asincrono.
        byte[] cvBytes = null;
        String cvFileName = null;

        if (cv != null && !cv.isEmpty()) {
            try {
                cvBytes = cv.getBytes();
                cvFileName = cv.getOriginalFilename();
            } catch (IOException e) {
                log.error("Errore nella lettura del CV", e);
            }
        }

        // Via self per attraversare il proxy e far valere l'@Async.
        self.sendEmailAsync(request, cvBytes, cvFileName, "application/pdf");
    }

    @Override
    @Async("emailTaskExecutor")
    public void sendEmailAsync(JobApplicationRequest request, byte[] cvBytes, String cvFileName,
            String cvContentType) {
        try {
            String roleName = "PERSONAL_TRAINER".equals(request.role()) ? "Personal Trainer" : "Nutrizionista";
            String subject = "Nuova Candidatura — " + request.firstName() + " " + request.lastName() + " — "
                    + roleName;
            String htmlBody = buildHtmlBody(request, roleName);

            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(mailFrom);
            helper.setTo(adminEmail);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);

            if (cvBytes != null && cvBytes.length > 0) {
                String safeFileName = (cvFileName != null && !cvFileName.isBlank()) ? cvFileName : "CV.pdf";
                helper.addAttachment(safeFileName, new ByteArrayResource(cvBytes), cvContentType);
            }

            javaMailSender.send(message);
            log.info("Email candidatura inviata via SMTP (messageId={})", message.getMessageID());

        } catch (MessagingException | MailException e) {
            log.error("Errore SMTP durante l'invio della candidatura", e);
        }
    }

    private String buildHtmlBody(JobApplicationRequest request, String roleName) {
        return "<div style=\"font-family: 'Segoe UI', Arial, sans-serif; max-width: 600px; margin: 0 auto; background: #f8f9fa; border-radius: 12px; overflow: hidden;\">"
                + "<div style=\"background: linear-gradient(135deg, #1a3462, #112240); padding: 30px; text-align: center;\">"
                + "<h1 style=\"color: #c9a96e; margin: 0; font-size: 24px;\">&#127947; Nuova Candidatura</h1>"
                + "<p style=\"color: #b1c0d4; margin: 8px 0 0;\">Richiesta di collaborazione su Naval Gold</p>"
                + "</div>"
                + "<div style=\"padding: 30px;\">"
                + "<table style=\"width: 100%; border-collapse: collapse;\">"
                + "<tr><td style=\"padding: 12px 0; border-bottom: 1px solid #e9ecef; font-weight: bold; color: #495057; width: 140px;\">Nome</td>"
                + "<td style=\"padding: 12px 0; border-bottom: 1px solid #e9ecef; color: #212529;\">"
                + request.firstName() + "</td></tr>"
                + "<tr><td style=\"padding: 12px 0; border-bottom: 1px solid #e9ecef; font-weight: bold; color: #495057;\">Cognome</td>"
                + "<td style=\"padding: 12px 0; border-bottom: 1px solid #e9ecef; color: #212529;\">"
                + request.lastName() + "</td></tr>"
                + "<tr><td style=\"padding: 12px 0; border-bottom: 1px solid #e9ecef; font-weight: bold; color: #495057;\">Email</td>"
                + "<td style=\"padding: 12px 0; border-bottom: 1px solid #e9ecef; color: #212529;\"><a href=\"mailto:"
                + request.email() + "\" style=\"color: #1a3462;\">" + request.email() + "</a></td></tr>"
                + "<tr><td style=\"padding: 12px 0; border-bottom: 1px solid #e9ecef; font-weight: bold; color: #495057;\">Ruolo richiesto</td>"
                + "<td style=\"padding: 12px 0; border-bottom: 1px solid #e9ecef; color: #212529;\"><span style=\"background: #c9a96e; color: #1a3462; padding: 4px 12px; border-radius: 20px; font-weight: bold; font-size: 13px;\">"
                + roleName + "</span></td></tr>"
                + "</table>"
                + "<div style=\"margin-top: 24px; padding: 20px; background: #fff; border-radius: 8px; border: 1px solid #e9ecef;\">"
                + "<h3 style=\"margin: 0 0 12px; color: #495057; font-size: 15px;\">&#128221; Messaggio Motivazionale</h3>"
                + "<p style=\"margin: 0; color: #212529; line-height: 1.6; white-space: pre-wrap;\">"
                + request.message() + "</p>"
                + "</div></div>"
                + "<div style=\"background: #e9ecef; padding: 16px; text-align: center; font-size: 13px; color: #6c757d;\">"
                + "Email generata automaticamente da Naval Gold Platform"
                + "</div></div>";
    }

    @Override
    @Async("emailTaskExecutor")
    public void sendWelcomeEmail(String toEmail, String firstName) {
        try {
            validateRecipient(toEmail);
            String subject = "Benvenuto su Kore, " + firstName + "! 🎉";
            String html = buildWelcomeHtml(firstName);
            sendSimpleEmail(toEmail, subject, html);
            log.info("Email di benvenuto inviata a {}", toEmail);
        } catch (Exception e) {
            log.error("Errore nell'invio dell'email di benvenuto a {}", toEmail, e);
        }
    }

    private String buildWelcomeHtml(String firstName) {
        return "<div style=\"font-family:'Segoe UI',Arial,sans-serif;max-width:600px;margin:0 auto;background:#f8f9fa;border-radius:12px;overflow:hidden\">"
             + "<div style=\"background:linear-gradient(135deg,#1a3462,#112240);padding:40px 30px;text-align:center\">"
             + "<h1 style=\"color:#c9a96e;margin:0;font-size:28px\">Benvenuto su Kore!</h1>"
             + "<p style=\"color:#b1c0d4;margin:12px 0 0;font-size:15px\">Il tuo account è stato creato con successo</p>"
             + "</div>"
             + "<div style=\"padding:30px\">"
             + "<p style=\"color:#212529;font-size:16px;line-height:1.6;margin:0 0 20px\">Ciao <strong>" + firstName + "</strong>,</p>"
             + "<p style=\"color:#495057;font-size:15px;line-height:1.6;margin:0 0 20px\">Siamo felici di averti con noi! "
             + "Il tuo account è attivo e pronto all'uso. Ora puoi accedere alla piattaforma e iniziare a prenotare le tue consulenze.</p>"
             + "<div style=\"text-align:center;margin:30px 0\">"
             + "<a href=\"http://localhost:4200/login\" style=\"background:linear-gradient(135deg,#e2b93b,#c49a20);color:#1a3462;padding:14px 40px;border-radius:10px;text-decoration:none;font-weight:bold;font-size:15px;display:inline-block\">Accedi alla Piattaforma</a>"
             + "</div>"
             + "<p style=\"color:#6c757d;font-size:14px;line-height:1.5;margin:0\">Se hai bisogno di aiuto, rispondi a questa email o contattaci nella chat di supporto.</p>"
             + "</div>"
             + "<div style=\"background:#e9ecef;padding:16px;text-align:center;font-size:13px;color:#6c757d\">"
             + "Email generata automaticamente da Kore Platform"
             + "</div></div>";
    }

    @Override
    @Async("emailTaskExecutor")
    public void sendBookingReminderEmail(String toEmail, String recipientName, String otherPartyName,
                                          LocalDateTime startTime, String meetingLink, boolean isForClient) {
        validateRecipient(toEmail);
        if (meetingLink == null || meetingLink.isBlank()) {
            throw new IllegalArgumentException("Link meeting mancante per il promemoria.");
        }

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy 'alle' HH:mm");
        String formattedTime = startTime.format(fmt);
        String subject = "\uD83D\uDD14 Promemoria: appuntamento tra 30 minuti — " + formattedTime;
        String html = buildReminderHtml(recipientName, otherPartyName, formattedTime, meetingLink, isForClient);
        sendSimpleEmail(toEmail, subject, html);
        log.info("Email promemoria inviata a {} per appuntamento delle {}", toEmail, formattedTime);
    }

    private String buildReminderHtml(String recipientName, String otherPartyName,
                                      String formattedTime, String meetingLink, boolean isForClient) {
        String roleLabel = isForClient ? "il tuo professionista" : "il tuo cliente";
        return "<div style=\"font-family:'Segoe UI',Arial,sans-serif;max-width:600px;margin:0 auto;background:#f8f9fa;border-radius:12px;overflow:hidden\">"
             + "<div style=\"background:linear-gradient(135deg,#1a3462,#112240);padding:40px 30px;text-align:center\">"
             + "<h1 style=\"color:#c9a96e;margin:0;font-size:26px\">\uD83D\uDD14 Promemoria Appuntamento</h1>"
             + "<p style=\"color:#b1c0d4;margin:12px 0 0;font-size:15px\">Il tuo appuntamento è tra 30 minuti</p>"
             + "</div>"
             + "<div style=\"padding:30px\">"
             + "<p style=\"color:#212529;font-size:16px;line-height:1.6;margin:0 0 20px\">Ciao <strong>" + recipientName + "</strong>,</p>"
             + "<p style=\"color:#495057;font-size:15px;line-height:1.6;margin:0 0 24px\">Ti ricordiamo che hai un appuntamento programmato con " + roleLabel + " <strong>" + otherPartyName + "</strong>.</p>"
             + "<div style=\"background:#fff;border:1px solid #e9ecef;border-radius:10px;padding:20px;margin:0 0 24px\">"
             + "<table style=\"width:100%;border-collapse:collapse\">"
             + "<tr><td style=\"padding:8px 0;font-weight:bold;color:#495057;width:120px\">\uD83D\uDCC5 Data e ora</td>"
             + "<td style=\"padding:8px 0;color:#212529\">" + formattedTime + "</td></tr>"
             + "<tr><td style=\"padding:8px 0;font-weight:bold;color:#495057\">\uD83D\uDC64 Con</td>"
             + "<td style=\"padding:8px 0;color:#212529\">" + otherPartyName + "</td></tr>"
             + "</table></div>"
             + "<div style=\"text-align:center;margin:30px 0\">"
             + "<a href=\"" + meetingLink + "\" style=\"background:linear-gradient(135deg,#e2b93b,#c49a20);color:#1a3462;padding:14px 40px;border-radius:10px;text-decoration:none;font-weight:bold;font-size:15px;display:inline-block\">Unisciti alla Videochiamata</a>"
             + "</div>"
             + "<p style=\"color:#6c757d;font-size:13px;line-height:1.5;margin:0;text-align:center\">Assicurati di avere una connessione stabile e una webcam funzionante.</p>"
             + "</div>"
             + "<div style=\"background:#e9ecef;padding:16px;text-align:center;font-size:13px;color:#6c757d\">"
             + "Email generata automaticamente da Kore Platform"
             + "</div></div>";
    }

    @Override
    @Async("emailTaskExecutor")
    public void sendBookingConfirmationEmail(String toEmail, String recipientName, String otherPartyName,
                                          LocalDateTime startTime, String meetingLink) {
        try {
            validateRecipient(toEmail);
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy 'alle' HH:mm");
            String formattedTime = startTime.format(fmt);
            String subject = "✅ Conferma Prenotazione — " + formattedTime;

            // Riusiamo il template del promemoria adattando i testi, invece di scriverne uno nuovo.
            String html = buildReminderHtml(recipientName, otherPartyName, formattedTime, meetingLink, true)
                .replace("Promemoria Appuntamento", "Conferma Prenotazione")
                .replace("Il tuo appuntamento è tra 30 minuti", "Il tuo appuntamento è stato confermato con successo")
                .replace("Ti ricordiamo che hai un appuntamento programmato", "Ti confermiamo l'appuntamento programmato");
                
            sendSimpleEmail(toEmail, subject, html);
            log.info("Email conferma prenotazione inviata a {} per appuntamento delle {}", toEmail, formattedTime);
        } catch (Exception e) {
            log.error("Errore nell'invio dell'email di conferma prenotazione a {}", toEmail, e);
        }
    }

    @Override
    @Async("emailTaskExecutor")
    public void sendBookingCancellationEmail(String toEmail, String recipientName, String otherPartyName,
                                          LocalDateTime startTime) {
        try {
            validateRecipient(toEmail);
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy 'alle' HH:mm");
            String formattedTime = startTime.format(fmt);
            String subject = "❌ Annullamento Prenotazione — " + formattedTime;

            // Stesso template, ma qui nascondiamo il bottone della videochiamata: l'incontro non c'è più.
            String html = buildReminderHtml(recipientName, otherPartyName, formattedTime, "#", true)
                .replace("Promemoria Appuntamento", "Annullamento Prenotazione")
                .replace("Il tuo appuntamento è tra 30 minuti", "Il tuo appuntamento è stato annullato")
                .replace("Ti ricordiamo che hai un appuntamento programmato", "Ti informiamo l'appuntamento programmato")
                .replace("<div style=\"text-align:center;margin:30px 0\">", "<div style=\"display:none;\">");

            sendSimpleEmail(toEmail, subject, html);
            log.info("Email annullamento prenotazione inviata a {} per appuntamento delle {}", toEmail, formattedTime);
        } catch (Exception e) {
            log.error("Errore nell'invio dell'email di annullamento prenotazione a {}", toEmail, e);
        }
    }

    @Override
    @Async("emailTaskExecutor")
    public void sendPasswordResetEmail(String toEmail, String firstName, String resetToken) {
        try {
            validateRecipient(toEmail);
            String subject = "🔑 Recupero Password — Kore";
            String resetLink = "http://localhost:4200/reset-password?token=" + resetToken;
            String html = buildPasswordResetHtml(firstName, resetLink);
            sendSimpleEmail(toEmail, subject, html);
            log.info("Email di reset password inviata a {}", toEmail);
        } catch (Exception e) {
            log.error("Errore nell'invio dell'email di reset password a {}", toEmail, e);
        }
    }

    private String buildPasswordResetHtml(String firstName, String resetLink) {
        return "<div style=\"font-family:'Segoe UI',Arial,sans-serif;max-width:600px;margin:0 auto;background:#f8f9fa;border-radius:12px;overflow:hidden\">"
             + "<div style=\"background:linear-gradient(135deg,#1a3462,#112240);padding:40px 30px;text-align:center\">"
             + "<h1 style=\"color:#c9a96e;margin:0;font-size:28px\">🔑 Recupero Password</h1>"
             + "<p style=\"color:#b1c0d4;margin:12px 0 0;font-size:15px\">Hai richiesto di reimpostare la tua password</p>"
             + "</div>"
             + "<div style=\"padding:30px\">"
             + "<p style=\"color:#212529;font-size:16px;line-height:1.6;margin:0 0 20px\">Ciao <strong>" + firstName + "</strong>,</p>"
             + "<p style=\"color:#495057;font-size:15px;line-height:1.6;margin:0 0 20px\">Abbiamo ricevuto una richiesta di reset della tua password. "
             + "Clicca il pulsante qui sotto per impostare una nuova password:</p>"
             + "<div style=\"text-align:center;margin:30px 0\">"
             + "<a href=\"" + resetLink + "\" style=\"background:linear-gradient(135deg,#e2b93b,#c49a20);color:#1a3462;padding:14px 40px;border-radius:10px;text-decoration:none;font-weight:bold;font-size:15px;display:inline-block\">Reimposta Password</a>"
             + "</div>"
             + "<p style=\"color:#6c757d;font-size:14px;line-height:1.5;margin:0 0 10px\">Il link scadrà tra <strong>30 minuti</strong>.</p>"
             + "<p style=\"color:#6c757d;font-size:13px;line-height:1.5;margin:0\">Se non hai richiesto tu il reset della password, ignora questa email. Il tuo account rimarrà sicuro.</p>"
             + "</div>"
             + "<div style=\"background:#e9ecef;padding:16px;text-align:center;font-size:13px;color:#6c757d\">"
             + "Email generata automaticamente da Kore Platform"
             + "</div></div>";
    }

    @Override
    @Async("emailTaskExecutor")
    public void sendPasswordChangeEmail(String toEmail, String firstName) {
        try {
            validateRecipient(toEmail);
            String subject = "🔒 Password Aggiornata — Kore";
            String html = buildPasswordChangeHtml(firstName);
            sendSimpleEmail(toEmail, subject, html);
            log.info("Email di avvenuto cambio password inviata a {}", toEmail);
        } catch (Exception e) {
            log.error("Errore nell'invio dell'email di cambio password a {}", toEmail, e);
        }
    }

    private String buildPasswordChangeHtml(String firstName) {
        return "<div style=\"font-family:'Segoe UI',Arial,sans-serif;max-width:600px;margin:0 auto;background:#f8f9fa;border-radius:12px;overflow:hidden\">"
             + "<div style=\"background:linear-gradient(135deg,#1a3462,#112240);padding:40px 30px;text-align:center\">"
             + "<h1 style=\"color:#c9a96e;margin:0;font-size:28px\">🔒 Password Aggiornata</h1>"
             + "<p style=\"color:#b1c0d4;margin:12px 0 0;font-size:15px\">La tua password è stata modificata con successo</p>"
             + "</div>"
             + "<div style=\"padding:30px\">"
             + "<p style=\"color:#212529;font-size:16px;line-height:1.6;margin:0 0 20px\">Ciao <strong>" + firstName + "</strong>,</p>"
             + "<p style=\"color:#495057;font-size:15px;line-height:1.6;margin:0 0 20px\">Ti confermiamo che la password del tuo account Kore è stata aggiornata con successo. "
             + "Ora puoi accedere alla piattaforma utilizzando la tua nuova password.</p>"
             + "<p style=\"color:#6c757d;font-size:13px;line-height:1.5;margin:0\">Se non sei stato tu a modificare la password, ti preghiamo di contattare immediatamente il nostro supporto.</p>"
             + "</div>"
             + "<div style=\"background:#e9ecef;padding:16px;text-align:center;font-size:13px;color:#6c757d\">"
             + "Email generata automaticamente da Kore Platform"
             + "</div></div>";
    }

    private void sendSimpleEmail(String to, String subject, String html) {
        validateRecipient(to);
        sendSimpleEmailViaSmtp(to, subject, html);
    }

    private void sendSimpleEmailViaSmtp(String to, String subject, String html) {
        if (javaMailSender == null) {
            throw new EmailDeliveryException("JavaMailSender non disponibile: controlla la configurazione SMTP.");
        }

        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");
            helper.setFrom(mailFrom);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);
            javaMailSender.send(message);
            log.info("Email inviata via SMTP a {} (messageId={})", to, message.getMessageID());
        } catch (MessagingException | MailException ex) {
            throw new EmailDeliveryException("Invio SMTP fallito", ex);
        }
    }

    private void validateRecipient(String toEmail) {
        if (toEmail == null || toEmail.isBlank()) {
            throw new IllegalArgumentException("Email destinatario mancante.");
        }
    }
}
