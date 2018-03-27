package com.gianlu.pyxreloaded.singletons;

import com.gianlu.pyxreloaded.Utils;
import com.gianlu.pyxreloaded.data.accounts.PasswordAccount;
import com.gianlu.pyxreloaded.data.accounts.UserAccount;
import com.gianlu.pyxreloaded.server.BaseCahHandler;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
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

public final class Emails {
    private static final int SEND_TIMEOUT = (int) TimeUnit.SECONDS.toMillis(10);
    private static final int TOKEN_LENGTH = 24;
    private static final Logger logger = Logger.getLogger(Emails.class);
    private final Mailer mailer;
    private final UsersWithAccount accounts;
    private final ServerDatabase db;
    private final String senderEmail;
    private final String senderName;
    private final String verifyCallback;

    public Emails(ServerDatabase db, Preferences preferences, UsersWithAccount accounts) {
        this.db = db;
        this.accounts = accounts;
        this.senderEmail = preferences.getString("emails/senderEmail", null);
        this.senderName = preferences.getString("emails/senderName", "PYX-Reloaded");
        this.verifyCallback = preferences.getString("emails/verifyCallback", null);

        this.mailer = mailer(preferences);
        if (mailer == null) {
            logger.info("Mailer is disabled.");
        } else {
            logger.info("Started mailer connection test...");
            this.mailer.testConnection();
            logger.info("Mailer connection test was successful!");
        }
    }

    @NotNull
    public String senderEmail() {
        return senderEmail;
    }

    @Nullable
    private Mailer mailer(Preferences preferences) {
        if (senderEmail == null || senderEmail.isEmpty()) return null;
        if (senderName == null || senderName.isEmpty()) return null;
        if (verifyCallback == null || verifyCallback.isEmpty()) return null;

        String smtpServer = preferences.getString("emails/smtpServer", null);
        if (smtpServer == null || smtpServer.isEmpty()) return null;

        int smtpPort = preferences.getInt("emails/smtpPort", 587);
        if (smtpPort <= 0 || smtpPort >= 65536) return null;

        String smtpUsername = preferences.getString("emails/smtpUsername", null);
        if (smtpUsername == null || smtpUsername.isEmpty()) return null;

        String smtpPassword = preferences.getString("emails/smtpPassword", null);
        if (smtpPassword == null || smtpPassword.isEmpty()) return null;

        return MailerBuilder
                .withSMTPServer(smtpServer, smtpPort, smtpUsername, smtpPassword)
                .withTransportStrategy(TransportStrategy.SMTP_TLS)
                .withSessionTimeout(SEND_TIMEOUT)
                .buildMailer();
    }

    private void send(EmailPopulatingBuilder builder) {
        Email email = builder.from(senderName, senderEmail).buildEmail();
        mailer.sendMail(email, true);
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

    public void tryVerify(@NotNull String token) throws SQLException, BaseCahHandler.CahException {
        try (ResultSet set = db.statement().executeQuery("SELECT email FROM email_verifications WHERE token='" + token + "'")) {
            set.next();
            String email = set.getString("email");

            try (Statement statement = db.statement()) {
                statement.executeUpdate("DELETE FROM email_verifications WHERE token='" + token + "' AND email='" + email + "'");
            }

            PasswordAccount account = accounts.getPasswordAccountForEmail(email);
            if (account != null) accounts.updateVerifiedStatus(account, true);
        }
    }

    @Contract(pure = true)
    public boolean enabled() {
        return mailer != null;
    }
}
