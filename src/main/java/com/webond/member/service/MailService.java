package com.webond.member.service;

import org.springframework.stereotype.Component;
import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.*;

@Component
public class MailService {

    public void sendMail(String to, String subject, String messageText) {
        try {
            Properties props = new Properties();
            props.put("mail.smtp.host", "smtp.gmail.com");
            props.put("mail.smtp.socketFactory.port", "465");
            props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.port", "465");

            final String myGmail = "ixlogic.wu@gmail.com";
            final String myGmail_password = "ddjomltcnypgcstn"; // 應用程式密碼
            
            Session session = Session.getInstance(props, new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(myGmail, myGmail_password);
                }
            });

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(myGmail));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject);
            message.setText(messageText);

            Transport.send(message);
            System.out.println("郵件發送成功！至: " + to);
        } catch (MessagingException e) {
            System.err.println("郵件發送失敗！");
            e.printStackTrace();
        }
    }
}