package org.example.email;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.example.ConfigReader;

import java.util.Properties;

public class EmailSender {

    public static void sendEmail(String recipient, String subject, String messageContent) throws MessagingException {
        Properties properties = new Properties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", "smtp.gmail.com");
        properties.put("mail.smtp.port", "587");
        Properties config = ConfigReader.loadProperties("config.properties");
        String myEmail = config.getProperty("email");
        String myPassword = config.getProperty("password");

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

