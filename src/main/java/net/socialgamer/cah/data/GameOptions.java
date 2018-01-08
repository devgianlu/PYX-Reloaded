package net.socialgamer.cah.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.socialgamer.cah.Constants.GameOptionData;
import net.socialgamer.cah.Preferences;
import net.socialgamer.cah.Utils;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class GameOptions {
    public static final int DEFAULT_SCORE_MIN = 4;
    public static final int DEFAULT_SCORE_DEF = 8;
    public static final int DEFAULT_SCORE_MAX = 69;
    public static final int DEFAULT_PLAYER_MIN = 3;
    public static final int DEFAULT_PLAYER_DEF = 10;
    public static final int DEFAULT_PLAYER_MAX = 20;
    public static final int DEFAULT_SPECTATOR_MIN = 0;
    public static final int DEFAULT_SPECTATOR_DEF = 10;
    public static final int DEFAULT_SPECTATOR_MAX = 20;
    public static final int DEFAULT_BLANKS_MIN = 0;
    public static final int DEFAULT_BLANKS_DEF = 0;
    public static final int DEFAULT_BLANKS_MAX = 30;
    public static final int DEFAULT_WIN_BY_MIN = 0;
    public static final int DEFAULT_WIN_BY_DEF = 0;
    public static final int DEFAULT_WIN_BY_MAX = 5;
    public static final TimeMultiplier DEFAULT_TIME_MULTIPLIER = TimeMultiplier.X1;

    public final Set<Integer> cardSetIds = new HashSet<>();
    public final Set<String> cardcastSetCodes = new HashSet<>();
    public int winBy;
    public int blanksInDeck;
    public int playerLimit;
    public int spectatorLimit;
    public int scoreGoal;
    public String password = "";
    public TimeMultiplier timerMultiplier = DEFAULT_TIME_MULTIPLIER;

    private GameOptions(Preferences preferences) {
        blanksInDeck = getBlanksLimit(preferences).def;
        scoreGoal = getScoreLimit(preferences).def;
        playerLimit = getPlayerLimit(preferences).def;
        spectatorLimit = getSpectatorLimit(preferences).def;
        winBy = getWinBy(preferences).def;
    }

    public static JsonObject getOptionsDefaultsJson(Preferences preferences) {
        JsonObject obj = new JsonObject();
        obj.add(GameOptionData.BLANKS_LIMIT.toString(), getBlanksLimit(preferences).toJson());
        obj.add(GameOptionData.PLAYER_LIMIT.toString(), getPlayerLimit(preferences).toJson());
        obj.add(GameOptionData.SPECTATOR_LIMIT.toString(), getSpectatorLimit(preferences).toJson());
        obj.add(GameOptionData.SCORE_LIMIT.toString(), getScoreLimit(preferences).toJson());
        obj.add(GameOptionData.WIN_BY.toString(), getWinBy(preferences).toJson());

        JsonObject tm = new JsonObject();
        tm.add("values", TimeMultiplier.validValuesJson());
        tm.addProperty("default", DEFAULT_TIME_MULTIPLIER.val);
        obj.add(GameOptionData.TIMER_MULTIPLIER.toString(), tm);

        return obj;
    }

    private static Preferences.MinDefaultMax getBlanksLimit(Preferences preferences) {
        return preferences.getMinDefaultMax("blankCardsLimit", DEFAULT_BLANKS_MIN, DEFAULT_BLANKS_DEF, DEFAULT_BLANKS_MAX);
    }

    private static Preferences.MinDefaultMax getPlayerLimit(Preferences preferences) {
        return preferences.getMinDefaultMax("playerLimit", DEFAULT_PLAYER_MIN, DEFAULT_PLAYER_DEF, DEFAULT_PLAYER_MAX);
    }

    private static Preferences.MinDefaultMax getSpectatorLimit(Preferences preferences) {
        return preferences.getMinDefaultMax("spectatorLimit", DEFAULT_SPECTATOR_MIN, DEFAULT_SPECTATOR_DEF, DEFAULT_SPECTATOR_MAX);
    }

    private static Preferences.MinDefaultMax getScoreLimit(Preferences preferences) {
        return preferences.getMinDefaultMax("scoreLimit", DEFAULT_SCORE_MIN, DEFAULT_SCORE_DEF, DEFAULT_SCORE_MAX);
    }

    private static Preferences.MinDefaultMax getWinBy(Preferences preferences) {
        return preferences.getMinDefaultMax("winBy", DEFAULT_WIN_BY_MIN, DEFAULT_WIN_BY_DEF, DEFAULT_WIN_BY_MAX);
    }

    @NotNull
    public static GameOptions deserialize(Preferences preferences, String text) {
        GameOptions options = new GameOptions(preferences);
        if (text == null || text.isEmpty()) return options;

        JsonObject json = new JsonParser().parse(text).getAsJsonObject();
        JsonArray cardSetIds = json.getAsJsonArray(GameOptionData.CARD_SETS.toString());
        if (cardSetIds != null) {
            for (JsonElement cardSetId : cardSetIds) options.cardSetIds.add(cardSetId.getAsInt());
        }

        JsonArray cardcastSetCodes = json.getAsJsonArray(GameOptionData.CARDCAST_SETS.toString());
        if (cardSetIds != null) {
            for (JsonElement code : cardcastSetCodes) options.cardcastSetCodes.add(code.getAsString());
        }

        Preferences.MinDefaultMax blankCards = getBlanksLimit(preferences);
        Preferences.MinDefaultMax score = getScoreLimit(preferences);
        Preferences.MinDefaultMax player = getPlayerLimit(preferences);
        Preferences.MinDefaultMax spectator = getSpectatorLimit(preferences);
        Preferences.MinDefaultMax winBy = getWinBy(preferences);

        options.blanksInDeck = Math.max(blankCards.min, Math.min(blankCards.max, Utils.optInt(json, GameOptionData.BLANKS_LIMIT.toString(), options.blanksInDeck)));
        options.playerLimit = Math.max(player.min, Math.min(player.max, Utils.optInt(json, GameOptionData.PLAYER_LIMIT.toString(), options.playerLimit)));
        options.spectatorLimit = Math.max(spectator.min, Math.min(spectator.max, Utils.optInt(json, GameOptionData.SPECTATOR_LIMIT.toString(), options.spectatorLimit)));
        options.scoreGoal = Math.max(score.min, Math.min(score.max, Utils.optInt(json, GameOptionData.SCORE_LIMIT.toString(), options.scoreGoal)));
        options.winBy = Math.max(winBy.min, Math.min(winBy.max, Utils.optInt(json, GameOptionData.WIN_BY.toString(), options.winBy)));
        options.timerMultiplier = TimeMultiplier.opt(json, GameOptionData.TIMER_MULTIPLIER.toString(), options.timerMultiplier);
        options.password = Utils.optString(json, GameOptionData.PASSWORD.toString(), options.password);

        return options;
    }

    /**
     * Update the options in-place (so that the Game doesn't need more locks).
     *
     * @param newOptions The new options to use.
     */
    public void update(GameOptions newOptions) {
        this.scoreGoal = newOptions.scoreGoal;
        this.playerLimit = newOptions.playerLimit;
        this.spectatorLimit = newOptions.spectatorLimit;
        this.winBy = newOptions.winBy;
        this.blanksInDeck = newOptions.blanksInDeck;
        this.password = newOptions.password;
        this.timerMultiplier = newOptions.timerMultiplier;

        synchronized (this.cardSetIds) {
            this.cardSetIds.clear();
            this.cardSetIds.addAll(newOptions.cardSetIds);
        }
    }

    /**
     * Get the options in a form that can be sent to clients.
     *
     * @param includePassword Include the actual password with the information. This should only be
     *                        sent to people in the game.
     * @return This game's general information: ID, host, state, player list, etc.
     */
    public Map<GameOptionData, Object> serialize(final boolean includePassword) {
        Map<GameOptionData, Object> info = new HashMap<>();
        info.put(GameOptionData.CARD_SETS, cardSetIds);
        info.put(GameOptionData.CARDCAST_SETS, cardcastSetCodes);
        info.put(GameOptionData.BLANKS_LIMIT, blanksInDeck);
        info.put(GameOptionData.PLAYER_LIMIT, playerLimit);
        info.put(GameOptionData.SPECTATOR_LIMIT, spectatorLimit);
        info.put(GameOptionData.SCORE_LIMIT, scoreGoal);
        info.put(GameOptionData.WIN_BY, winBy);
        info.put(GameOptionData.TIMER_MULTIPLIER, timerMultiplier);
        if (includePassword) info.put(GameOptionData.PASSWORD, password);
        return info;
    }

    public JsonObject toJson(boolean includePassword) {
        JsonObject obj = new JsonObject();
        obj.add(GameOptionData.CARD_SETS.toString(), Utils.toIntsJsonArray(cardSetIds));
        obj.add(GameOptionData.CARDCAST_SETS.toString(), Utils.toStringsJsonArray(cardcastSetCodes));
        obj.addProperty(GameOptionData.BLANKS_LIMIT.toString(), blanksInDeck);
        obj.addProperty(GameOptionData.PLAYER_LIMIT.toString(), playerLimit);
        obj.addProperty(GameOptionData.SPECTATOR_LIMIT.toString(), spectatorLimit);
        obj.addProperty(GameOptionData.SCORE_LIMIT.toString(), scoreGoal);
        obj.addProperty(GameOptionData.WIN_BY.toString(), winBy);
        obj.addProperty(GameOptionData.TIMER_MULTIPLIER.toString(), timerMultiplier.val);
        if (includePassword) obj.addProperty(GameOptionData.PASSWORD.toString(), password);
        return obj;
    }

    /**
     * @return Selected card set IDs which are local to PYX, for querying the database.
     */
    public Set<Integer> getPyxCardSetIds() {
        Set<Integer> pyxCardSetIds = new HashSet<>();
        for (int cardSetId : cardSetIds) {
            if (cardSetId > 0) pyxCardSetIds.add(cardSetId);
        }

        return pyxCardSetIds;
    }

    public enum TimeMultiplier {
        X0_25("0.25x"), X0_50("0.50x"), X0_75("0.75x"),
        X1("1x"), X1_25("1.25x"), X1_50("1.50x"),
        X1_75("1.75x"), X2("2x"), X2_5("2.50x"),
        X3("3x"), X4("4x"), X5("5x"),
        X10("10x"), UNLIMITED("Unlimited");

        private final String val;

        TimeMultiplier(String val) {
            this.val = val;
        }

        public static TimeMultiplier opt(JsonObject obj, String key, TimeMultiplier fallback) {
            if (obj.has(key)) return TimeMultiplier.parse(obj.get(key).getAsString(), fallback);
            else return fallback;
        }

        public static JsonArray validValuesJson() {
            JsonArray array = new JsonArray();
            for (TimeMultiplier item : values()) array.add(item.val);
            return array;
        }

        @NotNull
        public static TimeMultiplier parse(String val, TimeMultiplier fallback) {
            for (TimeMultiplier item : values())
                if (item.val.equals(val)) return item;

            return fallback;
        }

        public double factor() {
            if (this == UNLIMITED) throw new IllegalArgumentException("Cannot get factor for unlimited");
            return Double.valueOf(val.substring(0, val.length() - 1));
        }
    }
}
