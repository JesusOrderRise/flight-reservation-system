package com.frsystem.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendReservationConfirmation(String toEmail, String seatNumber, Long flightId) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("noreply@frsystem.com");
        message.setTo(toEmail);
        message.setSubject("Reservation Confirmed");
        message.setText("Your reservation for flight #" + flightId + ", seat " + seatNumber + " has been confirmed.");

        mailSender.send(message);
    }
}