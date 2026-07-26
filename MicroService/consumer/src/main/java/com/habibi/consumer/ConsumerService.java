package com.habibi.consumer;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class ConsumerService {

    @Autowired
    private JavaMailSender mailSender;

    @RabbitListener(queues = "orderQueue")
    public void receivedMessage(String message) {

        System.out.println("Received: " + message);

        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setTo("tahfizhhabibie122@gmail.com");
        mail.setSubject("Order Berhasil");
        String emailText = "Halo,\n\n" +
                "Pesanan Anda telah berhasil diproses.\n\n" +
                "Detail Pesanan:\n" +
                "---------------------------\n" +
                message + "\n" +
                "---------------------------\n\n" +
                "Terima kasih telah berbelanja.\n" +
                "Salam,\n" +
                "Tim Sistem Order";

        mail.setText(emailText);


        mailSender.send(mail);

        System.out.println("Email terkirim!");
    }
}