package com.gianlu.pyxreloaded;

import com.gianlu.pyxreloaded.data.User;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.text.ParseException;

public final class Consts {
    public static final int CHAT_FLOOD_MESSAGE_COUNT = 4;
    public static final int CHAT_FLOOD_TIME = 30 * 1000;
    public static final int CHAT_MAX_LENGTH = 200;
    public static final String VALID_NAME_PATTERN = "[a-zA-Z_][a-zA-Z0-9_]{2,29}";

    /**
     * Possible events.
     */
    public enum Event {
        /**
         * Banned user
         */
        BANNED("B&"),
        /**
         * Added Cardcast deck
         */
        CARDCAST_ADD_CARDSET("cAc"),
        /**
         * Removed Cardcast deck
         */
        CARDCAST_REMOVE_CARDSET("cRc"),
        /**
         * Chat event
         */
        CHAT("C"),
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
        /**
         * Player has been kicked
         */
        KICKED("kk"),
        /**
         * The player has been kicked from the game for being idle too many rounds
         */
        KICKED_FROM_GAME_IDLE("kfgi"),
        /**
         * A new player joined the server
         */
        NEW_PLAYER("np"),
        /**
         * A player has left the server
         */
        PLAYER_LEAVE("pl"),
        /**
         * Ping request, should respond with pong
         */
        PING("pp"),
        /**
         * Someone disliked this game
         */
        GAME_DISLIKE("gdlk"),
        /**
         * Someone liked this game
         */
        GAME_LIKE("glk"),
        /**
         * Someone suggested to modify the game options
         */
        GAME_OPTIONS_MODIFICATION_SUGGESTED("goms"),
        /**
         * Suggested game options has been accepted
         */
        GAME_ACCEPTED_SUGGESTED_OPTIONS("gaso"),
        /**
         * Suggested game options has been declined
         */
        GAME_DECLINED_SUGGESTED_OPTIONS("gdso");

        private final String event;

        Event(String event) {
            this.event = event;
        }

        @Override
        public String toString() {
            return event;
        }
    }

    /**
     * Error codes.
     */
    public enum ErrorCode {
        /**
         * The game has already started.
         */
        ALREADY_STARTED("as"),
        /**
         * The game has already stopped.
         */
        ALREADY_STOPPED("aS"),
        /**
         * Invalid operation.
         */
        BAD_OP("bo"),
        /**
         * Bad request.
         */
        BAD_REQUEST("br"),
        /**
         * Banned.
         */
        BANNED("Bd"),
        /**
         * You cannot join another game.
         */
        CANNOT_JOIN_ANOTHER_GAME("cjag"),
        /**
         * Cannot find Cardcast deck with given ID. If you just added this deck to Cardcast, wait a few minutes and try again.
         */
        CARDCAST_CANNOT_FIND("ccf"),
        /**
         * Invalid Cardcast ID. Must be exactly 5 characters.
         */
        CARDCAST_INVALID_ID("cii"),
        /**
         * You don't have that card.
         */
        DO_NOT_HAVE_CARD("dnhc"),
        /**
         * That game is full. Join another.
         */
        GAME_FULL("gf"),
        /**
         * Invalid card specified.
         */
        INVALID_CARD("ic"),
        /**
         * Invalid game specified.
         */
        INVALID_GAME("ig"),
        /**
         * Nickname must contain only upper and lower case letters, numbers, or underscores, must be 3 to 30 characters long, and must not start with a number.
         */
        INVALID_NICK("in"),
        /**
         * Messages cannot be longer than CHAT_MAX_LENGTH characters.
         */
        MESSAGE_TOO_LONG("mtl"),
        /**
         * Nickname is already in use.
         */
        NICK_IN_USE("niu"),
        /**
         * No card specified.
         */
        NO_CARD_SPECIFIED("ncs"),
        /**
         * No game specified.
         */
        NO_GAME_SPECIFIED("ngs"),
        /**
         * No message specified.
         */
        NO_MSG_SPECIFIED("nms"),
        /**
         * No such user.
         */
        NO_SUCH_USER("nsu"),
        /**
         * You are not an administrator.
         */
        NOT_ADMIN("na"),
        /**
         * You must add card sets to match the game requirements.
         */
        NOT_ENOUGH_CARDS("nec"),
        /**
         * There are not enough players to start the game.
         */
        NOT_ENOUGH_PLAYERS("nep"),
        /**
         * Only the game host can do that.
         */
        NOT_GAME_HOST("ngh"),
        /**
         * You are not in that game.
         */
        NOT_IN_THAT_GAME("nitg"),
        /**
         * You are not the judge.
         */
        NOT_JUDGE("nj"),
        /**
         * Not registered. Refresh the page.
         */
        NOT_REGISTERED("nr"),
        /**
         * It is not your turn to play a card.
         */
        NOT_YOUR_TURN("nyt"),
        /**
         * Operation not specified.
         */
        OP_NOT_SPECIFIED("ons"),
        /**
         * Your session has expired. Refresh the page.
         */
        SESSION_EXPIRED("se"),
        /**
         * You are chatting too fast. Wait a few seconds and try again.
         */
        TOO_FAST("tf"),
        /**
         * There are too many games already in progress. Either join an existing game, or wait for one to become available.
         */
        TOO_MANY_GAMES("tmg"),
        /**
         * There are too many users connected. Either join another server, or wait for a user to disconnect.
         */
        TOO_MANY_USERS("tmu"),
        /**
         * That password is incorrect.
         */
        WRONG_PASSWORD("wp"),
        /**
         * You have already played all the necessary cards.
         */
        ALREADY_PLAYED("ap"),
        /**
         * The user already played all pick cards and should draw the remaining.
         */
        SHOULD_DRAW_CARD("sdc"),
        /**
         * Given suggested options id is invalid.
         */
        INVALID_SUGGESTED_OPTIONS_ID("isoi"),
        /**
         * The user has already suggested a modification.
         */
        ALREADY_SUGGESTED("AS"),
        /**
         * Email already in use.
         */
        EMAIL_IN_USE("emiu"),
        /**
         * Google error.
         */
        GOOGLE_ERROR("ge"),
        /**
         * User hasn't a Google account.
         */
        GOOGLE_NOT_REGISTERED("gnr"),
        /**
         * User sent an invalid Google ID token.
         */
        GOOGLE_INVALID_TOKEN("git"),
        /**
         * Facebook error.
         */
        FACEBOOK_ERROR("fe"),
        /**
         * User sent an invalid Facebook access token.
         */
        FACEBOOK_INVALID_TOKEN("fit"),
        /**
         * User hasn't a Facebook account.
         */
        FACEBOOK_NOT_REGISTERED("fnr"),
        /**
         * User hasn't verified his Facebook email.
         */
        FACEBOOK_EMAIL_NOT_VERIFIED("fbemnv"),
        /**
         * Github error.
         */
        GITHUB_ERROR("ghe"),
        /**
         * User hasn't a Github account.
         */
        GITHUB_NOT_REGISTERED("ghnr"),
        /**
         * User sent an invalid Github access token.
         */
        GITHUB_INVALID_TOKEN("ghit"),
        /**
         * User sent an invalid Twitter access token.
         */
        TWITTER_INVALID_TOKEN("twit"),
        /**
         * Twitter error.
         */
        TWITTER_ERROR("twe"),
        /**
         * User hasn't a Twitter account.
         */
        TWITTER_NOT_REGISTERED("twnr"),
        /**
         * User hasn't verified his Twitter email.
         */
        TWITTER_EMAIL_NOT_VERIFIED("twemnv");

        private final String code;

        ErrorCode(String code) {
            this.code = code;
        }

        @Override
        public String toString() {
            return code;
        }
    }

    /**
     * Reason why a client disconnected.
     */
    public enum DisconnectReason {
        /**
         * The client was banned by the server administrator.
         */
        BANNED("b&"),
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

        DisconnectReason(String reason) {
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

        ReconnectNextAction(String action) {
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
    public enum Operation {
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
        DISLIKE("dlk"),
        /**
         * Get logged user info
         */
        ME("gme"),
        /**
         * Response to a ping
         */
        PONG("PP"),
        /**
         * Get a list of suggested game options.
         */
        GET_SUGGESTED_GAME_OPTIONS("ggso"),
        /**
         * Decide whether to accept or decline the game options suggested modification.
         */
        GAME_OPTIONS_SUGGESTION_DECISION("gosd"),
        /**
         * Create an user account.
         */
        CREATE_ACCOUNT("ca");

        private final String op;

        Operation(String op) {
            this.op = op;
        }

        @Override
        public String toString() {
            return op;
        }
    }

    /**
     * Possible game state.
     */
    public enum GameState {
        JUDGING("j"),
        LOBBY("l"),
        @IgnoreDuplicateIn(User.class)
        PLAYING("p"),
        ROUND_OVER("ro");

        private final String state;

        GameState(String state) {
            this.state = state;
        }

        @Override
        public String toString() {
            return state;
        }
    }

    /**
     * Possible player state.
     */
    public enum GamePlayerState {
        HOST("sh"), IDLE("si"), JUDGE("sj"), JUDGING("sjj"), WINNER("sw"), PLAYING("sp");

        private final String state;

        GamePlayerState(String state) {
            this.state = state;
        }

        @Override
        public String toString() {
            return state;
        }
    }

    // ************************
    // Returnable data
    // ************************

    /**
     * General keys that can be used both in requests and responses.
     */
    public enum GeneralKeys implements ReceivableKey, ReturnableKey {
        /**
         * Request operation.
         */
        OP("o"),
        /**
         * Whether the response contains an error.
         */
        ERROR("e"),
        /**
         * The error code.
         */
        ERROR_CODE("ec"),
        /**
         * User nickname.
         */
        NICKNAME("n"),
        /**
         * Card id, an int.
         */
        CARD_ID("cid"),
        /**
         * Game id, an int.
         */
        GAME_ID("gid"),
        /**
         * Cardcast ID of type 'XXXXX'.
         */
        CARDCAST_ID("cci"),
        /**
         * Event key
         */
        EVENT("E"),
        /**
         * Event list
         */
        EVENTS("Es"),
        /**
         * Games list
         */
        GAMES("gl"),
        /**
         * Whether this client is reconnecting or not.
         */
        IN_PROGRESS("ip"),
        /**
         * Names of the connected users.
         */
        NAMES("nl"),
        /**
         * Next thing that should be done. Long string is fine.
         */
        NEXT("next"),
        /**
         * Whether the user successfully registered as an admin
         */
        IS_ADMIN("ia"),
        /**
         * Text for write in card.
         */
        WRITE_IN_TEXT("wit"),
        /**
         * Maximum number of games for this server.
         */
        MAX_GAMES("mg"),
        /**
         * Cardcast deck info.
         */
        CARDCAST_DECK_INFO("cdi"),
        /**
         * Reason why a player disconnected.
         */
        DISCONNECT_REASON("qr"),
        /**
         * Authentication type.
         */
        AUTH_TYPE("aT"),
        /**
         * User account data.
         */
        ACCOUNT("a");

        private final String key;

        GeneralKeys(String key) {
            this.key = key;
        }

        @Override
        public String toString() {
            return key;
        }
    }

    /**
     * Fields for chat communication
     */
    public enum ChatData implements ReturnableKey, ReceivableKey {
        /**
         * General message
         */
        MESSAGE("m"),
        /**
         * Message sender.
         */
        FROM("f"),
        /**
         * Whether the message sender is an admin.
         */
        FROM_ADMIN("fa");

        private final String key;

        ChatData(String key) {
            this.key = key;
        }

        @Override
        public String toString() {
            return key;
        }
    }

    /**
     * Fields for white cards.
     */
    public enum WhiteCardData implements ReturnableKey {
        @IgnoreDuplicateIn(BlackCardData.class)
        TEXT("T"),
        @IgnoreDuplicateIn(BlackCardData.class)
        WATERMARK("W"),
        WRITE_IN("wi");

        private final String key;

        WhiteCardData(String key) {
            this.key = key;
        }

        @Override
        public String toString() {
            return key;
        }
    }

    /**
     * Fields for black cards.
     */
    public enum BlackCardData implements ReturnableKey {
        DRAW("D"),
        PICK("PK"),
        @IgnoreDuplicateIn(WhiteCardData.class)
        TEXT("T"),
        @IgnoreDuplicateIn(WhiteCardData.class)
        WATERMARK("W");

        private final String key;

        BlackCardData(String key) {
            this.key = key;
        }

        @Override
        public String toString() {
            return key;
        }
    }

    /**
     * Fields for card sets.
     */
    public enum CardSetData implements ReturnableKey {
        /**
         * Whether this is a base deck.
         */
        BASE_DECK("bd"),
        /**
         * Number of black cards in the deck.
         */
        BLACK_CARDS_IN_DECK("bcid"),
        /**
         * Deck description.
         */
        CARD_SET_DESCRIPTION("csd"),
        /**
         * Deck name.
         */
        CARD_SET_NAME("csn"),
        /**
         * Deck id, an int or a string for Cardcast decks.
         */
        ID("csi"),
        /**
         * Deck importance.
         */
        WEIGHT("w"),
        /**
         * Number of white cards in the deck.
         */
        WHITE_CARDS_IN_DECK("wcid");

        private final String key;

        CardSetData(String key) {
            this.key = key;
        }

        @Override
        public String toString() {
            return key;
        }
    }

    /**
     * Fields for MixMax objects
     */
    public enum MinMaxData implements ReturnableKey {
        MIN("min"),
        MAX("max"),
        DEFAULT("def");

        private final String key;

        MinMaxData(String key) {
            this.key = key;
        }

        @Override
        public String toString() {
            return key;
        }
    }

    /**
     * General fields for game info.
     */
    public enum GeneralGameData implements ReturnableKey {
        BLACK_CARDS_PRESENT("bcp"),
        BLACK_CARDS_REQUIRED("bcr"),
        WHITE_CARDS_PRESENT("wcp"),
        WHITE_CARDS_REQUIRED("wcr"),
        STATE("gs");

        private final String key;

        GeneralGameData(String key) {
            this.key = key;
        }

        @Override
        public String toString() {
            return key;
        }
    }

    public enum GameSuggestedOptionsData implements ReturnableKey, ReceivableKey {
        /**
         * The options object itself.
         */
        OPTIONS("sgo"),
        /**
         * Suggested options id
         */
        ID("soid"),
        /**
         * Suggester nickname
         */
        SUGGESTER("s"),
        /**
         * Accepted or declined, boolean
         */
        DECISION("d");

        private final String key;

        GameSuggestedOptionsData(String key) {
            this.key = key;
        }

        @Override
        public String toString() {
            return key;
        }
    }

    /**
     * Fields for options about a game.
     */
    public enum GameOptionsData implements ReturnableKey, ReceivableKey {
        /**
         * The options object itself.
         */
        OPTIONS("go"),
        /**
         * The default options object.
         */
        DEFAULT_OPTIONS("dgo"),
        /**
         * Maximum number of blank cards.
         */
        BLANKS_LIMIT("bl"),
        /**
         * PYX card sets.
         */
        CARD_SETS("css"),
        /**
         * Cardcast card sets.
         */
        CARDCAST_SETS("CCs"),
        /**
         * Game password.
         */
        @IgnoreDuplicateIn(AuthType.class)
        PASSWORD("pw"),
        /**
         * Maximum number of players.
         */
        PLAYER_LIMIT("pL"),
        /**
         * Maximum number of spectators.
         */
        SPECTATOR_LIMIT("vL"),
        /**
         * Goal.
         */
        SCORE_LIMIT("sl"),
        /**
         * Must score additional X points over the second highest scored player after reaching the goal.
         */
        WIN_BY("wb"),
        /**
         * Time multiplier.
         */
        TIMER_MULTIPLIER("tm");

        private final String key;

        GameOptionsData(String key) {
            this.key = key;
        }

        @Override
        public String toString() {
            return key;
        }
    }

    /**
     * Fields for ongoing games data.
     */
    public enum OngoingGameData implements ReturnableKey {
        /**
         * Black card.
         */
        BLACK_CARD("bc"),
        /**
         * Cards left to play to complete round.
         */
        LEFT_TO_PICK("ltp"),
        /**
         * Cards left to draw to complete round.
         */
        LEFT_TO_DRAW("ltd"),
        /**
         * White (table) cards.
         */
        WHITE_CARDS("wc"),
        /**
         * Player hand.
         */
        HAND("h"),
        /**
         * Round winner nickname
         */
        ROUND_WINNER("rw"),
        /**
         * Winning card ID(s)
         */
        WINNING_CARD("WC"),
        /**
         * Round intermission
         */
        INTERMISSION("i"),
        /**
         * Time available to play.
         */
        PLAY_TIMER("Pt"),
        /**
         * Whether the game will be stopped.
         */
        WILL_STOP("ws"),
        /**
         * Whether the hand should be cleared from the old cards
         */
        CLEAR_HAND("ch");

        private final String key;

        OngoingGameData(String key) {
            this.key = key;
        }

        @Override
        public String toString() {
            return key;
        }
    }

    /**
     * Fields for info about a game.
     */
    public enum GameInfoData implements ReturnableKey {
        /**
         * The info object itself.
         */
        INFO("gi"),
        /**
         * Who created the game, may change if it leaves.
         */
        HOST("H"),
        /**
         * Whether the game is protected by a password.
         */
        HAS_PASSWORD("hp"),
        /**
         * Players in game.
         */
        PLAYERS("P"),
        /**
         * Spectators in game.
         */
        SPECTATORS("V"),
        /**
         * Number of likes.
         */
        LIKES("LK"),
        /**
         * Number of dislikes.
         */
        DISLIKES("DLK"),
        /**
         * Whether the user that submitted the request likes the game.
         */
        I_LIKE("iLK"),
        /**
         * Whether the user that submitted the request dislikes the game.
         */
        I_DISLIKE("iDLK");


        private final String key;

        GameInfoData(String key) {
            this.key = key;
        }

        @Override
        public String toString() {
            return key;
        }
    }

    /**
     * Fields for TimeMultiplier object
     */
    public enum TimeMultiplierData implements ReturnableKey {
        @IgnoreDuplicateIn(MinMaxData.class)
        DEFAULT("def"),
        VALUES("v");

        private final String key;

        TimeMultiplierData(String key) {
            this.key = key;
        }

        @Override
        public String toString() {
            return key;
        }
    }

    /**
     * Fields for the information about players in a game.
     */
    public enum GamePlayerInfo implements ReturnableKey {
        /**
         * The info object itself.
         */
        INFO("pi"),
        NAME("N"), SCORE("sc"), STATUS("st");

        private final String key;

        GamePlayerInfo(String key) {
            this.key = key;
        }

        @Override
        public String toString() {
            return key;
        }
    }

    /**
     * Identify auth type in database, also used to send authentication data
     */
    public enum AuthType implements ReceivableKey {
        @IgnoreDuplicateIn(GameOptionsData.class)
        PASSWORD("pw"),
        GOOGLE("g"),
        FACEBOOK("fb"),
        GITHUB("gh"),
        TWITTER("tw");

        private final String key;

        AuthType(String key) {
            this.key = key;
        }

        @NotNull
        public static AuthType parse(String key) throws ParseException {
            for (AuthType type : values())
                if (type.key.equals(key))
                    return type;

            throw new ParseException(key, 0);
        }

        @Override
        public String toString() {
            return key;
        }
    }

    /**
     * User data
     */
    public enum UserData implements ReceivableKey, ReturnableKey {
        /**
         * Profile picture URL.
         */
        @IgnoreDuplicateIn(GameState.class)
        PICTURE("p"),
        /**
         * Email address.
         */
        EMAIL("em"),
        /**
         * Whether the user verified its email.
         */
        EMAIL_VERIFIED("emv");

        private final String key;

        UserData(String key) {
            this.key = key;
        }

        @Override
        public String toString() {
            return key;
        }
    }

    /**
     * Can be used in responses as key
     */
    public interface ReturnableKey {
    }

    /**
     * Can be used to retrieve parameters from requests
     */
    public interface ReceivableKey {
    }

    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface IgnoreDuplicateIn {
        Class value();
    }
}
