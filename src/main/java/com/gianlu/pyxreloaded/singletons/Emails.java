package com.gianlu.pyxreloaded.singletons;

import com.gianlu.pyxreloaded.Utils;
import com.gianlu.pyxreloaded.data.accounts.PasswordAccount;
import com.gianlu.pyxreloaded.data.accounts.UserAccount;
import org.jetbrains.annotations.NotNull;
import org.simplejavamail.email.Email;
import org.simplejavamail.email.EmailBuilder;
import org.simplejavamail.email.EmailPopulatingBuilder;
import org.simplejavamail.mailer.Mailer;
import org.simplejavamail.mailer.MailerBuilder;
import org.simplejavamail.mailer.config.TransportStrategy;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public final class Emails {
    private static final int SEND_TIMEOUT = (int) TimeUnit.SECONDS.toMillis(10);
    private static final int TOKEN_LENGTH = 24;
    private static final Logger logger = Logger.getLogger(Emails.class.getSimpleName());
    private final Mailer mailer;
    private final UsersWithAccount accounts;
    private final ServerDatabase db;
    private final String senderEmail;
    private final String senderName;
    private final String verifyCallback;

    public Emails(ServerDatabase db, Preferences preferences, UsersWithAccount accounts) {
        this.db = db;
        this.accounts = accounts;
        this.senderEmail = preferences.getString("emails/senderEmail", "");
        this.senderName = preferences.getString("emails/senderName", "PYX-Reloaded");
        this.verifyCallback = preferences.getString("emails/verifyCallback", "");

        this.mailer = MailerBuilder
                .withSMTPServer(preferences.getString("emails/smtpServer", ""),
                        preferences.getInt("emails/smtpPort", 587),
                        preferences.getString("emails/smtpUsername", ""),
                        preferences.getString("emails/smtpPassword", ""))
                .withTransportStrategy(TransportStrategy.SMTP_TLS)
                .withSessionTimeout(SEND_TIMEOUT)
                .buildMailer();

        logger.info("Started mailer connection test...");
        this.mailer.testConnection();
        logger.info("Mailer connection test was successful!");
    }

    private void send(EmailPopulatingBuilder builder) {
        Email email = builder.from(senderName, senderEmail).buildEmail();
        mailer.sendMail(email);
    }

    private void saveToken(UserAccount account, String token) {
        try (Statement statement = db.statement()) {
            int result = statement.executeUpdate("INSERT INTO email_verifications (email, token) VALUES ('" + account.email + "', '" + token + "')");

            if (result != 1) throw new IllegalStateException();
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    private String generateVerificationToken(UserAccount account) {
        String token = Utils.generateAlphanumericString(TOKEN_LENGTH);
        saveToken(account, token);
        return token;
    }

    public void sendEmailVerification(UserAccount account) {
        String token = generateVerificationToken(account);
        String callback = verifyCallback + "?token=" + token;

        send(EmailBuilder.startingBlank()
                .withSubject("PYX-Reloaded email verification")
                .withPlainText("Click on the following link to verify your email address for PYX-Reloaded: " + callback)
                .to(account.username, account.email));
    }

    public boolean tryVerify(@NotNull String token) {
        try (ResultSet set = db.statement().executeQuery("SELECT email FROM email_verifications WHERE token='" + token + "'")) {
            set.next();
            String email = set.getString("email");

            try (Statement statement = db.statement()) {
                statement.executeUpdate("DELETE FROM email_verifications WHERE token='" + token + "' AND email='" + email + "'");
            }

            PasswordAccount account = accounts.getPasswordAccountForEmail(email);
            if (account != null) accounts.updateVerifiedStatus(account, true);
            return true;
        } catch (SQLException ignored) {
        }

        return false;
    }
}
