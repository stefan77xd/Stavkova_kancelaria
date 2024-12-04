package org.example;

import jakarta.mail.*;
import jakarta.mail.internet.*;

import java.util.Properties;

public class EmailSender {

    public static void sendEmail(String recipient, String subject, String messageContent) throws MessagingException {
        Properties properties = new Properties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", "smtp.gmail.com"); // SMTP server
        properties.put("mail.smtp.port", "587");
        Properties config = ConfigReader.loadProperties("config.properties");
        String myEmail = config.getProperty("email"); // Nahradiť svojou e-mailovou adresou
        String myPassword = config.getProperty("password");    // Nahradiť svojim heslom alebo App Password (odporúčané)

        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(myEmail, myPassword);
            }
        });

        Message message = prepareMessage(session, myEmail, recipient, subject, messageContent);
        Transport.send(message);
    }

    private static Message prepareMessage(Session session, String myEmail, String recipient, String subject, String messageContent) throws MessagingException {
        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(myEmail));
        message.setRecipient(Message.RecipientType.TO, new InternetAddress(recipient));
        message.setSubject(subject);
        message.setText(messageContent);
        return message;
    }
}

