package com.reportai.reportaiserver.service;

import com.reportai.reportaiserver.exception.CustomException;
import com.reportai.reportaiserver.exception.ErrorDictionary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

   @Autowired
   private JavaMailSender mailSender;

   @Value("${SMTP_FROM}")
   private String remetente;

   public boolean enviarEmail(String to, String subject, String body) {
      try {
         SimpleMailMessage message = new SimpleMailMessage();
         message.setFrom(remetente);
         message.setTo(to);
         message.setSubject(subject);
         message.setText(body);

         mailSender.send(message);
         return true;
      } catch (Exception e) {
          throw new CustomException(ErrorDictionary.ERRO_EMAIL);
      }
   }
}
