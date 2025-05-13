package com.reportai.reportaiserver.service;

import com.reportai.reportaiserver.exception.CustomException;
import com.reportai.reportaiserver.exception.ErrorDictionary;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${SMTP_FROM}")
    private String remetente;

    @Value("${WEB_URL}")
    private String webURL;

    public boolean enviarEmailRecuperacaoSenha(String to, String codigo) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(remetente);
            helper.setTo(to);
            helper.setSubject("Recuperação de Senha - Reportaí");

            String linkRedefinicao = webURL + "/redefinir-senha?token=" + codigo + "&email=" + to;

            String content = "<div style=\"font-family: Arial, sans-serif; max-width: 600px; margin: auto; padding: 20px; border: 1px solid #e0e0e0; border-radius: 8px;\">" +
                    "<div style=\"text-align: center; margin-bottom: 20px;\">" +
                    "<img src=\"https://storage.googleapis.com/reportai/resources/logo.png\" alt=\"Reportaí\" style=\"max-width: 150px;\">" +
                    "</div>" +
                    "<h2 style=\"color: #333;\">Recuperação de Senha</h2>" +
                    "<p style=\"color: #555;\">Olá,</p>" +
                    "<p style=\"color: #555;\">Recebemos uma solicitação para redefinir a senha da sua conta no <strong>Reportaí</strong>. Caso você não tenha solicitado essa alteração, pode ignorar este email.</p>" +
                    "<p style=\"color: #555;\">Para redefinir sua senha de forma segura, clique no botão abaixo:</p>" +
                    "<div style=\"text-align: center; margin: 30px 0;\">" +
                    "<a href=\"" + linkRedefinicao + "\" style=\"background-color: #ffca2c; color: #000000; text-decoration: none; padding: 12px 24px; border-radius: 5px; font-size: 16px;\">Redefinir Senha</a>" +
                    "</div>" +
                    "<p style=\"color: #999; font-size: 12px;\">Se o botão acima não funcionar, copie e cole o seguinte link no seu navegador:</p>" +
                    "<p style=\"word-break: break-all; color: #1976d2; font-size: 12px;\"><a href=\"" + linkRedefinicao + "\">" + linkRedefinicao + "</a></p>" +
                    "<hr style=\"border:none; border-top:1px solid #eee; margin:20px 0\">" +
                    "<p style=\"color: #aaa; font-size: 12px; text-align: center;\">Este é um email automático enviado pelo sistema Reportaí. Por favor, não responda a este endereço.</p>" +
                    "</div>";

            helper.setText(content, true);

            mailSender.send(message);
            return true;
        } catch (MessagingException e) {
            throw new CustomException(ErrorDictionary.ERRO_EMAIL);
        }
    }
}
