package com.vend.Service.impl;

import com.vend.Entity.User;
import com.vend.Service.CommunicationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;


@Service("EmailService")
public class EmailServiceImpl implements CommunicationService {

    @Value("${VenD.userName}")
    String userName;
    @Value("${VenD.password}")
    String password;
    @Value("${VenD.smptpHost}")
    String smtpHost;
    @Value("${VenD.socketFactoryPort}")
    String socketFactoryPort;
    @Value("${VenD.socketFactoryClass}")
    String SocketFactoryClass;
    @Value("${VenD.smtpPort}")
    String smtpPort;
    @Value("${VenD.fromEmail}")
    String fromEmail;

    @Override
    public boolean sendForgotPasswordCode(User user, String code) {
        String message = "This is a forgot password code for " + user.getName() + ". The code is "+code;
        return send(user.getEmail(),"Forgot Password Code Requested",message);
    }

    @Override
    public boolean sendVerificationCode(User user, String code) {
        String message = "This is an account verification code for " + user.getName() + ". The code is "+code;
        return send(user.getEmail(),"Account Verification Code Requested",message);
    }

    private boolean send(String to, String subject, String content) {

        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.socketFactory.port", "465");
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", "465");

        Session session = Session.getDefaultInstance(props,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(userName, password);
                    }
                });
        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromEmail));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject);
            message.setContent(content, "text/html");
            Transport.send(message);
        } catch (MessagingException e) {
            return false;
        }
        return true;
    }
}
