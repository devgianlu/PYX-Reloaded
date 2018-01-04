package net.socialgamer.cah;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class Constants {
    public static final int CHAT_FLOOD_MESSAGE_COUNT = 4;
    public static final int CHAT_FLOOD_TIME = 30 * 1000;
    public static final int CHAT_MAX_LENGTH = 200;

    /**
     * Reason why a client disconnected.
     */
    public enum DisconnectReason {
        /**
         * The client was banned by the server administrator.
         */
        BANNED("B&"),
        /**
         * The client made no user-caused requests within the timeout window.
         */
        IDLE_TIMEOUT("it"),
        /**
         * The client was kicked by the server administrator.
         */
        KICKED("k"),
        /**
         * The user clicked the "log out" button.
         */
        MANUAL("man"),
        /**
         * The client failed to make any queries within the timeout window.
         */
        PING_TIMEOUT("pt");

        private final String reason;

        DisconnectReason(final String reason) {
            this.reason = reason;
        }

        @Override
        public String toString() {
            return reason;
        }
    }

    /**
     * The next thing the client should do during reconnect phase.
     * <p>
     * Leaving these as longer strings as they are only used once per client.
     */
    public enum ReconnectNextAction {
        /**
         * The client should load a game as part of the reconnect process.
         */
        GAME("game"),
        /**
         * There is nothing for the client to reload, perhaps because they were not in any special
         * state, or they are a new client.
         */
        NONE("none");

        private final String action;

        ReconnectNextAction(final String action) {
            this.action = action;
        }

        @Override
        public String toString() {
            return action;
        }
    }

    /**
     * Valid client request operations.
     */
    public enum AjaxOperation {
        /**
         * Ban a user from the server
         * <p>
         * Admin only
         */
        BAN("b"),
        /**
         * Add a Cardcast deck to the current game
         */
        CARDCAST_ADD_CARDSET("cac"),
        /**
         * List current available Cardcast decks in the game
         */
        CARDCAST_LIST_CARDSETS("clc"),
        /**
         * Remove a Cardcast deck from the current game
         */
        CARDCAST_REMOVE_CARDSET("crc"),
        /**
         * Change the game options
         */
        CHANGE_GAME_OPTIONS("cgo"),
        /**
         * Send a message on the global chat
         */
        CHAT("c"),
        /**
         * Create a game
         */
        CREATE_GAME("cg"),
        /**
         * Try to resume user session and returns available card sets
         */
        FIRST_LOAD("fl"),
        /**
         * Send a message on the game chat
         */
        GAME_CHAT("GC"),
        /**
         * List all available games
         */
        GAME_LIST("ggl"),
        /**
         * Get all cards for a particular game: black, hand, and round white cards.
         */
        GET_CARDS("gc"),
        /**
         * Get current game info: players, spectators, game options, status, host, id
         */
        GET_GAME_INFO("ggi"),
        /**
         * Join a game, may require password
         */
        JOIN_GAME("jg"),
        /**
         * Spectate a game, may require password
         */
        SPECTATE_GAME("vg"),
        /**
         * Select the winning card, judge only
         */
        JUDGE_SELECT("js"),
        /**
         * Kick a user from the server
         * <p>
         * Admin only
         */
        KICK("K"),
        /**
         * Leave the current game (both for player and spectator)
         */
        LEAVE_GAME("lg"),
        /**
         * Log out and invalidate the current session
         */
        LOG_OUT("lo"),
        /**
         * Get the names of all clients connected to the server.
         */
        NAMES("gn"),
        /**
         * Play card, non-judge player only
         */
        PLAY_CARD("pc"),
        /**
         * Register with a nickname, set PYX-Session
         */
        REGISTER("r"),
        /**
         * Change a player score
         * <p>
         * Admin only
         */
        SCORE("SC"),
        /**
         * Start the game, checking the requirements
         */
        START_GAME("sg"),
        /**
         * Stop the game
         */
        STOP_GAME("Sg"),
        /**
         * Like a game
         */
        LIKE("lk"),
        /**
         * Dislike a game
         */
        DISLIKE("dlk");

        private final String op;

        AjaxOperation(final String op) {
            this.op = op;
        }

        @Override
        public String toString() {
            return op;
        }
    }

    /**
     * Parameters for client requests.
     */
    public enum AjaxRequest {
        CARD_ID("cid"),
        CARDCAST_ID("cci"),
        EMOTE("me"),
        GAME_ID("gid"),
        GAME_OPTIONS("go"),
        MESSAGE("m"),
        NICKNAME("n"),
        OP("o"),
        PASSWORD("pw"),
        PERSISTENT_ID("pid"),
        SERIAL("s"),
        WALL("wall"),
        ADMIN_TOKEN("at");

        private final String field;

        AjaxRequest(final String field) {
            this.field = field;
        }

        @Override
        public String toString() {
            return field;
        }
    }

    /**
     * Keys for client request responses.
     */
    public enum AjaxResponse implements ReturnableData {
        BLACK_CARD("bc"),
        @DuplicationAllowed
        CARD_ID(AjaxRequest.CARD_ID),
        @DuplicationAllowed
        CARDCAST_ID("cci"),
        CARD_SETS("css"),
        ERROR("e"),
        ERROR_CODE("ec"),
        @DuplicationAllowed
        GAME_ID(AjaxRequest.GAME_ID),
        GAME_INFO("gi"),
        @DuplicationAllowed
        GAME_OPTIONS(AjaxRequest.GAME_OPTIONS),
        GAMES("gl"),
        HAND("h"),
        /**
         * Whether this client is reconnecting or not.
         */
        IN_PROGRESS("ip"),
        MAX_GAMES("mg"),
        NAMES("nl"),
        /**
         * Next thing that should be done in reconnect process. Used once, long string OK.
         */
        NEXT("next"),
        @DuplicationAllowed
        NICKNAME(AjaxRequest.NICKNAME),
        @DuplicationAllowed
        PERSISTENT_ID(AjaxRequest.PERSISTENT_ID),
        PLAYER_INFO("pi"),
        @DuplicationAllowed
        SERIAL(AjaxRequest.SERIAL),
        WHITE_CARDS("wc"),
        /**
         * Whether the user successfully registered as an admin
         */
        IS_ADMIN("ia");

        private final String field;

        AjaxResponse(final String field) {
            this.field = field;
        }

        AjaxResponse(final Enum<?> field) {
            this.field = field.toString();
        }

        @Override
        public String toString() {
            return field;
        }
    }

    public enum ErrorInformation implements ReturnableData {
        BLACK_CARDS_PRESENT("bcp"),
        BLACK_CARDS_REQUIRED("bcr"),
        WHITE_CARDS_PRESENT("wcp"),
        WHITE_CARDS_REQUIRED("wcr");

        private final String code;

        ErrorInformation(final String code) {
            this.code = code;
        }

        @Override
        public String toString() {
            return code;
        }
    }

    /**
     * Client request and long poll response errors.
     */
    public enum ErrorCode implements Localizable {
        ACCESS_DENIED("ad", "Access denied."),
        ALREADY_STARTED("as", "The game has already started."),
        ALREADY_STOPPED("aS", "The game has already stopped."),
        BAD_OP("bo", "Invalid operation."),
        BAD_REQUEST("br", "Bad request."),
        @DuplicationAllowed
        BANNED(DisconnectReason.BANNED, "Banned."),
        CANNOT_JOIN_ANOTHER_GAME("cjag", "You cannot join another game."),
        CARDCAST_CANNOT_FIND("ccf", "Cannot find Cardcast deck with given ID. If you just added this deck to Cardcast, wait a few minutes and try again."),
        CARDCAST_INVALID_ID("cii", "Invalid Cardcast ID. Must be exactly 5 characters."),
        DO_NOT_HAVE_CARD("dnhc", "You don't have that card."),
        GAME_FULL("gf", "That game is full. Join another."),
        INVALID_CARD("ic", "Invalid card specified."),
        INVALID_GAME("ig", "Invalid game specified."),
        INVALID_NICK("in", "Nickname must contain only upper and lower case letters, numbers, or underscores, must be 3 to 30 characters long, and must not start with a number."),
        MESSAGE_TOO_LONG("mtl", "Messages cannot be longer than " + CHAT_MAX_LENGTH + " characters."),
        NICK_IN_USE("niu", "Nickname is already in use."),
        NO_CARD_SPECIFIED("ncs", "No card specified."),
        NO_GAME_SPECIFIED("ngs", "No game specified."),
        NO_MSG_SPECIFIED("nms", "No message specified."),
        NO_NICK_SPECIFIED("nns", "No nickname specified."),
        NO_SESSION("ns", "Session not detected. Make sure you have cookies enabled."),
        NO_SUCH_USER("nsu", "No such user."),
        NOT_ADMIN("na", "You are not an administrator."),
        NOT_ENOUGH_CARDS("nec", "You must add card sets to match the game requirements."),
        NOT_ENOUGH_PLAYERS("nep", "There are not enough players to start the game."),
        NOT_GAME_HOST("ngh", "Only the game host can do that."),
        NOT_IN_THAT_GAME("nitg", "You are not in that game."),
        NOT_JUDGE("nj", "You are not the judge."),
        NOT_REGISTERED("nr", "Not registered. Refresh the page."),
        NOT_YOUR_TURN("nyt", "It is not your turn to play a card."),
        OP_NOT_SPECIFIED("ons", "Operation not specified."),
        RESERVED_NICK("rn", "That nick is reserved."),
        SERVER_ERROR("serr", "An error occurred on the server."),
        SESSION_EXPIRED("se", "Your session has expired. Refresh the page."),
        TOO_FAST("tf", "You are chatting too fast. Wait a few seconds and try again."),
        TOO_MANY_GAMES("tmg", "There are too many games already in progress. Either join an existing game, or wait for one to become available."),
        TOO_MANY_USERS("tmu", "There are too many users connected. Either join another server, or wait for a user to disconnect."),
        WRONG_PASSWORD("wp", "That password is incorrect.");

        private final String code;
        private final String message;

        /**
         * @param code    Error code to send over the wire to the client.
         * @param message Message the client should display for the error code.
         */
        ErrorCode(final String code, final String message) {
            this.code = code;
            this.message = message;
        }

        ErrorCode(final Enum<?> code, final String message) {
            this.code = code.toString();
            this.message = message;
        }

        @Override
        public String toString() {
            return code;
        }

        @Override
        public String getString() {
            return message;
        }
    }

    /**
     * Events that can be returned in a long poll response.
     */
    public enum LongPollEvent {
        @DuplicationAllowed
        BANNED(DisconnectReason.BANNED),
        @DuplicationAllowed
        CARDCAST_ADD_CARDSET(AjaxOperation.CARDCAST_ADD_CARDSET),
        @DuplicationAllowed
        CARDCAST_REMOVE_CARDSET(AjaxOperation.CARDCAST_REMOVE_CARDSET),
        @DuplicationAllowed
        CHAT(AjaxOperation.CHAT),
        /**
         * The black deck has been reshuffled
         */
        GAME_BLACK_RESHUFFLE("gbr"),
        /**
         * The judge left the game
         */
        GAME_JUDGE_LEFT("gjl"),
        /**
         * Skipped judge
         */
        GAME_JUDGE_SKIPPED("gjs"),
        /**
         * Client should refresh games list
         */
        GAME_LIST_REFRESH("glr"),
        /**
         * Game options changed
         */
        GAME_OPTIONS_CHANGED("goc"),
        /**
         * A player's info changed
         */
        GAME_PLAYER_INFO_CHANGE("gpic"),
        /**
         * A new player joined the game
         */
        GAME_PLAYER_JOIN("gpj"),
        /**
         * Kicked from game for being idle too much
         */
        GAME_PLAYER_KICKED_IDLE("gpki"),
        /**
         * A player joined the game
         */
        GAME_PLAYER_LEAVE("gpl"),
        /**
         * A player has been skipped for being idle
         */
        GAME_PLAYER_SKIPPED("gps"),
        /**
         * A spectator joined the game
         */
        GAME_SPECTATOR_JOIN("gvj"),
        /**
         * A spectator left the game
         */
        GAME_SPECTATOR_LEAVE("gvl"),
        /**
         * Round completed
         */
        GAME_ROUND_COMPLETE("grc"),
        /**
         * The game state changed
         */
        GAME_STATE_CHANGE("gsc"),
        /**
         * The white deck has been reshuffled
         */
        GAME_WHITE_RESHUFFLE("gwr"),
        /**
         * Your cards
         */
        HAND_DEAL("hd"),
        /**
         * Play or you'll be skipped!
         */
        HURRY_UP("hu"),
        @DuplicationAllowed
        KICKED(DisconnectReason.KICKED),
        /**
         * A player has been kicked from the game for being idle too many rounds
         */
        KICKED_FROM_GAME_IDLE("kfgi"),
        /**
         * A new player joined the server
         */
        NEW_PLAYER("np"),
        /**
         * There has been no other action to inform the client about in a certain time frame, so inform
         * the client that we have nothing to inform them so the client doesn't think we went away.
         */
        NOOP("_"),
        /**
         * A player has left the server
         */
        PLAYER_LEAVE("pl");

        private final String event;

        LongPollEvent(final String event) {
            this.event = event;
        }

        LongPollEvent(final Enum<?> event) {
            this.event = event.toString();
        }

        @Override
        public String toString() {
            return event;
        }
    }

    /**
     * Data keys that can be in a long poll response.
     */
    public enum LongPollResponse implements ReturnableData {
        @DuplicationAllowed
        BLACK_CARD(AjaxResponse.BLACK_CARD),
        CARDCAST_DECK_INFO("cdi"),
        @DuplicationAllowed
        EMOTE(AjaxRequest.EMOTE),
        @DuplicationAllowed
        ERROR(AjaxResponse.ERROR),
        @DuplicationAllowed
        ERROR_CODE(AjaxResponse.ERROR_CODE),
        EVENT("E"),
        /**
         * Player a chat message is from.
         */
        FROM("f"),
        /**
         * A chat message is from an admin. This is going to be done with IP addresses for now.
         */
        FROM_ADMIN("fa"),
        @DuplicationAllowed
        GAME_ID(AjaxResponse.GAME_ID),
        @DuplicationAllowed
        GAME_INFO(AjaxResponse.GAME_INFO),
        GAME_STATE("gs"),
        @DuplicationAllowed
        HAND(AjaxResponse.HAND),
        /**
         * The delay until the next game round begins.
         */
        INTERMISSION("i"),
        @DuplicationAllowed
        MESSAGE(AjaxRequest.MESSAGE),
        @DuplicationAllowed
        NICKNAME(AjaxRequest.NICKNAME),
        PLAY_TIMER("Pt"),
        @DuplicationAllowed
        PLAYER_INFO(AjaxResponse.PLAYER_INFO),
        /**
         * Reason why a player disconnected.
         */
        REASON("qr"),
        ROUND_WINNER("rw"),
        TIMESTAMP("ts"),
        @DuplicationAllowed
        WALL(AjaxRequest.WALL),
        @DuplicationAllowed
        WHITE_CARDS(AjaxResponse.WHITE_CARDS),
        WINNING_CARD("WC");

        private final String field;

        LongPollResponse(final String field) {
            this.field = field;
        }

        LongPollResponse(final Enum<?> field) {
            this.field = field.toString();
        }

        @Override
        public String toString() {
            return field;
        }
    }

    /**
     * Data fields for white cards.
     */
    public enum WhiteCardData {
        @DuplicationAllowed
        ID(AjaxRequest.CARD_ID),
        TEXT("T"),
        WATERMARK("W"),
        WRITE_IN("wi");

        private final String key;

        WhiteCardData(final String key) {
            this.key = key;
        }

        WhiteCardData(final Enum<?> key) {
            this.key = key.toString();
        }

        @Override
        public String toString() {
            return key;
        }
    }

    /**
     * Data fields for black cards.
     */
    public enum BlackCardData {
        DRAW("D"),
        @DuplicationAllowed
        ID(WhiteCardData.ID),
        PICK("PK"),
        @DuplicationAllowed
        TEXT(WhiteCardData.TEXT),
        @DuplicationAllowed
        WATERMARK(WhiteCardData.WATERMARK);

        private final String key;

        BlackCardData(final String key) {
            this.key = key;
        }

        BlackCardData(final Enum<?> key) {
            this.key = key.toString();
        }

        @Override
        public String toString() {
            return key;
        }
    }

    /**
     * Data fields for card sets.
     */
    public enum CardSetData {
        BASE_DECK("bd"),
        BLACK_CARDS_IN_DECK("bcid"),
        CARD_SET_DESCRIPTION("csd"),
        CARD_SET_NAME("csn"),
        @DuplicationAllowed
        ID(WhiteCardData.ID),
        WEIGHT("w"),
        WHITE_CARDS_IN_DECK("wcid");

        private final String key;

        CardSetData(final String key) {
            this.key = key;
        }

        CardSetData(final Enum<?> key) {
            this.key = key.toString();
        }

        @Override
        public String toString() {
            return key;
        }
    }

    /**
     * A game's current state.
     */
    public enum GameState implements Localizable {
        DEALING("d", "In Progress"),
        JUDGING("j", "In Progress"),
        LOBBY("l", "Not Started"),
        PLAYING("p", "In Progress"),
        ROUND_OVER("ro", "In Progress");

        private final String state;
        private final String message;

        GameState(final String state, final String message) {
            this.state = state;
            this.message = message;
        }

        @Override
        public String toString() {
            return state;
        }

        @Override
        public String getString() {
            return message;
        }
    }

    /**
     * Fields for information about a game.
     */
    public enum GameInfo {
        HOST("H"),
        @DuplicationAllowed
        ID(AjaxRequest.GAME_ID),
        @DuplicationAllowed
        GAME_OPTIONS(AjaxRequest.GAME_OPTIONS),
        HAS_PASSWORD("hp"),
        PLAYERS("P"),
        SPECTATORS("V"),
        LIKES("LK"),
        I_LIKE("iLK"),
        I_DISLIKE("iDLK"),
        DISLIKES("DLK"),
        STATE("S");

        private final String key;

        GameInfo(final String key) {
            this.key = key;
        }

        GameInfo(final Enum<?> key) {
            this.key = key.toString();
        }

        @Override
        public String toString() {
            return key;
        }
    }

    /**
     * Fields for options about a game.
     */
    public enum GameOptionData {
        BLANKS_LIMIT("bl"),
        @DuplicationAllowed
        CARD_SETS(AjaxResponse.CARD_SETS),
        @DuplicationAllowed
        PASSWORD(AjaxRequest.PASSWORD),
        PLAYER_LIMIT("pL"),
        SPECTATOR_LIMIT("vL"),
        SCORE_LIMIT("sl"),
        TIMER_MULTIPLIER("tm");

        private final String key;

        GameOptionData(final String key) {
            this.key = key;
        }

        GameOptionData(final Enum<?> key) {
            this.key = key.toString();
        }

        @Override
        public String toString() {
            return key;
        }
    }

    /**
     * Keys for the information about players in a game.
     */
    public enum GamePlayerInfo {
        NAME("N"),
        SCORE("sc"),
        STATUS("st");

        private final String key;

        GamePlayerInfo(final String key) {
            this.key = key;
        }

        @Override
        public String toString() {
            return key;
        }
    }

    /**
     * States that a player in a game can be in. The first client string is displayed in the
     * scoreboard, and the second one is displayed in a banner at the top, telling the user what to
     * do.
     */
    public enum GamePlayerStatus implements DoubleLocalizable {
        HOST("sh", "Host", "Wait for players then click Start Game."),
        IDLE("si", "", "Waiting for players..."),
        JUDGE("sj", "Card Czar", "You are the Card Czar."),
        JUDGING("sjj", "Selecting", "Select a winning card."),
        PLAYING("sp", "Playing", "Select a card to play."),
        WINNER("sw", "Winner!", "You have won!"),
        SPECTATOR("sv", "Spectator", "You are just spectating.");

        private final String status;
        private final String message;
        private final String message2;

        GamePlayerStatus(final String status, final String message, final String message2) {
            this.status = status;
            this.message = message;
            this.message2 = message2;
        }

        @Override
        public String toString() {
            return status;
        }

        @Override
        public String getString() {
            return message;
        }

        @Override
        public String getString2() {
            return message2;
        }
    }

    /**
     * Enums that implement this interface are valid keys for data returned to clients.
     */
    interface ReturnableData {
    }

    /**
     * Enums that implement this interface have a user-visible string associated with them.
     * <p>
     * There presently is not support for localization, but the name fits.
     */
    public interface Localizable {
        /**
         * @return The user-visible string that is associated with this enum value.
         */
        String getString();
    }

    /**
     * Enums that implement this interface have two user-visible strings associated with them.
     * <p>
     * There presently is not support for localization, but the name fits.
     */
    public interface DoubleLocalizable {
        /**
         * @return The first user-visible string that is associated with this enum value.
         */
        String getString();

        /**
         * @return The second user-visible string that is associated with this enum value.
         */
        String getString2();
    }

    /**
     * Mark an enum value as being allowed to be the same as another enum value. Should only be used
     * when another enum's value is directly used as the value. This will prevent the test from
     * flagging it as an invalid reuse.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @interface DuplicationAllowed {
    }
}
