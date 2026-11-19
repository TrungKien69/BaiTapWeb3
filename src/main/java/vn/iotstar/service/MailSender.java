package vn.iotstar.service;
@FunctionalInterface
public interface MailSender { void send(String to,String purpose,String otp); }
