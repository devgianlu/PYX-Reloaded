package com.gianlu.pyxreloaded.singletons;

import org.simplejavamail.email.Email;
import org.simplejavamail.email.EmailBuilder;
import org.simplejavamail.mailer.Mailer;
import org.simplejavamail.mailer.MailerBuilder;
import org.simplejavamail.mailer.config.TransportStrategy;

import java.util.concurrent.TimeUnit;

public final class Emails {
    private static final int SEND_TIMEOUT = (int) TimeUnit.SECONDS.toMillis(10);
    private final Mailer mailer;

    public Emails(Preferences preferences) {
        mailer = MailerBuilder
                .withSMTPServer("smtp.someServer.com", 587, "fakeUsername!", "fakePassword!")
                .withTransportStrategy(TransportStrategy.SMTP_TLS)
                .withSessionTimeout(SEND_TIMEOUT)
                .buildMailer();
    }

    public void send() {
        Email email = EmailBuilder.startingBlank()
                .to("ME!", "me@meee.com")
                .withSubject("Test123")
                .from("PYX-Reloaded", "someEmail@someStuff.xyz")
                .withPlainText("Please view this email in a modern email client!")
                .buildEmail();

        mailer.sendMail(email);
    }
}
