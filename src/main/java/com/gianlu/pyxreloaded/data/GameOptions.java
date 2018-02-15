package com.gianlu.pyxreloaded.data;

import com.gianlu.pyxreloaded.Consts;
import com.gianlu.pyxreloaded.JsonWrapper;
import com.gianlu.pyxreloaded.Preferences;
import com.gianlu.pyxreloaded.Utils;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashSet;
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

    public final Set<Integer> cardSetIds = Collections.synchronizedSet(new HashSet<>());
    public final Set<String> cardcastSetCodes = Collections.synchronizedSet(new HashSet<>());
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

    public static JsonWrapper getOptionsDefaultsJson(Preferences preferences) {
        JsonWrapper obj = new JsonWrapper();
        obj.add(Consts.GameOptionData.BLANKS_LIMIT, getBlanksLimit(preferences).toJson());
        obj.add(Consts.GameOptionData.PLAYER_LIMIT, getPlayerLimit(preferences).toJson());
        obj.add(Consts.GameOptionData.SPECTATOR_LIMIT, getSpectatorLimit(preferences).toJson());
        obj.add(Consts.GameOptionData.SCORE_LIMIT, getScoreLimit(preferences).toJson());
        obj.add(Consts.GameOptionData.WIN_BY, getWinBy(preferences).toJson());

        JsonWrapper tm = new JsonWrapper();
        tm.add(Consts.TimeMultiplierData.VALUES, TimeMultiplier.validValuesJson());
        tm.add(Consts.TimeMultiplierData.DEFAULT, DEFAULT_TIME_MULTIPLIER.val);
        obj.add(Consts.GameOptionData.TIMER_MULTIPLIER, tm);

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
        JsonArray cardSetIds = json.getAsJsonArray(Consts.GameOptionData.CARD_SETS.toString());
        if (cardSetIds != null) {
            for (JsonElement cardSetId : cardSetIds) options.cardSetIds.add(cardSetId.getAsInt());
        }

        JsonArray cardcastSetCodes = json.getAsJsonArray(Consts.GameOptionData.CARDCAST_SETS.toString());
        if (cardSetIds != null) {
            for (JsonElement code : cardcastSetCodes) options.cardcastSetCodes.add(code.getAsString());
        }

        Preferences.MinDefaultMax blankCards = getBlanksLimit(preferences);
        Preferences.MinDefaultMax score = getScoreLimit(preferences);
        Preferences.MinDefaultMax player = getPlayerLimit(preferences);
        Preferences.MinDefaultMax spectator = getSpectatorLimit(preferences);
        Preferences.MinDefaultMax winBy = getWinBy(preferences);

        options.blanksInDeck = assign(blankCards, options.blanksInDeck, json, Consts.GameOptionData.BLANKS_LIMIT);
        options.playerLimit = assign(player, options.playerLimit, json, Consts.GameOptionData.PLAYER_LIMIT);
        options.spectatorLimit = assign(spectator, options.spectatorLimit, json, Consts.GameOptionData.SPECTATOR_LIMIT);
        options.scoreGoal = assign(score, options.scoreGoal, json, Consts.GameOptionData.SCORE_LIMIT);
        options.winBy = assign(winBy, options.winBy, json, Consts.GameOptionData.WIN_BY);
        options.timerMultiplier = TimeMultiplier.opt(json, options.timerMultiplier);
        options.password = assign(json, options.password, Consts.GameOptionData.PASSWORD);

        return options;
    }

    private static int assign(Preferences.MinDefaultMax minDefaultMax, int current, JsonObject obj, Consts.GameOptionData field) {
        int value;
        JsonElement element = obj.get(field.toString());
        if (element == null) value = current;
        else value = element.getAsInt();

        return Math.max(minDefaultMax.min, Math.min(minDefaultMax.max, value));
    }

    private static String assign(JsonObject obj, String current, Consts.GameOptionData field) {
        JsonElement element = obj.get(field.toString());
        if (element == null) return current;
        else return element.getAsString();
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

    public JsonWrapper toJson(boolean includePassword) {
        JsonWrapper obj = new JsonWrapper();
        obj.add(Consts.GameOptionData.CARD_SETS, Utils.toIntsJsonArray(cardSetIds));
        obj.add(Consts.GameOptionData.CARDCAST_SETS, Utils.toStringsJsonArray(cardcastSetCodes));
        obj.add(Consts.GameOptionData.BLANKS_LIMIT, blanksInDeck);
        obj.add(Consts.GameOptionData.PLAYER_LIMIT, playerLimit);
        obj.add(Consts.GameOptionData.SPECTATOR_LIMIT, spectatorLimit);
        obj.add(Consts.GameOptionData.SCORE_LIMIT, scoreGoal);
        obj.add(Consts.GameOptionData.WIN_BY, winBy);
        obj.add(Consts.GameOptionData.TIMER_MULTIPLIER, timerMultiplier.val);
        if (includePassword) obj.add(Consts.GameOptionData.PASSWORD, password);
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

        public static TimeMultiplier opt(JsonObject obj, TimeMultiplier current) {
            if (obj.has(Consts.GameOptionData.TIMER_MULTIPLIER.toString()))
                return TimeMultiplier.parse(obj.get(Consts.GameOptionData.TIMER_MULTIPLIER.toString()).getAsString(), current);
            else
                return current;
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