package com.gianlu.pyxreloaded.singletons;

import org.simplejavamail.email.Email;
import org.simplejavamail.email.EmailPopulatingBuilder;
import org.simplejavamail.mailer.Mailer;
import org.simplejavamail.mailer.MailerBuilder;
import org.simplejavamail.mailer.config.TransportStrategy;

import java.util.concurrent.TimeUnit;

public final class Emails {
    private static final int SEND_TIMEOUT = (int) TimeUnit.SECONDS.toMillis(10);
    private final Mailer mailer;
    private final String senderEmail;
    private final String senderName;

    public Emails(Preferences preferences) {
        senderEmail = preferences.getString("emails/senderEmail", "");
        senderName = preferences.getString("emails/senderName", "PYX-Reloaded");

        mailer = MailerBuilder
                .withSMTPServer(preferences.getString("emails/smtpServer", ""),
                        preferences.getInt("emails/smtpPort", 587),
                        preferences.getString("emails/smtpUsername", ""),
                        preferences.getString("emails/smtpPassword", ""))
                .withTransportStrategy(TransportStrategy.SMTP_TLS)
                .withSessionTimeout(SEND_TIMEOUT)
                .buildMailer();

        mailer.testConnection();
    }

    public void send(EmailPopulatingBuilder builder) {
        Email email = builder.from(senderName, senderEmail).buildEmail();
        mailer.sendMail(email);
    }
}
