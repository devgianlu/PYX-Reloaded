package com.gianlu.pyxreloaded.game;

import com.gianlu.pyxreloaded.Consts;
import com.gianlu.pyxreloaded.Utils;
import com.gianlu.pyxreloaded.data.JsonWrapper;
import com.gianlu.pyxreloaded.singletons.Preferences;
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

    public GameOptions(Preferences preferences, @NotNull String text) {
        this(preferences);

        if (text.isEmpty()) return;

        JsonObject json = new JsonParser().parse(text).getAsJsonObject();
        JsonArray cardSetIds = json.getAsJsonArray(Consts.GameOptionsData.CARD_SETS.toString());
        if (cardSetIds != null) {
            for (JsonElement cardSetId : cardSetIds) this.cardSetIds.add(cardSetId.getAsInt());
        }

        JsonArray cardcastSetCodes = json.getAsJsonArray(Consts.GameOptionsData.CARDCAST_SETS.toString());
        if (cardSetIds != null) {
            for (JsonElement code : cardcastSetCodes) this.cardcastSetCodes.add(code.getAsString());
        }

        Preferences.MinDefaultMax blankCards = getBlanksLimit(preferences);
        Preferences.MinDefaultMax score = getScoreLimit(preferences);
        Preferences.MinDefaultMax player = getPlayerLimit(preferences);
        Preferences.MinDefaultMax spectator = getSpectatorLimit(preferences);
        Preferences.MinDefaultMax winBy = getWinBy(preferences);

        this.blanksInDeck = assign(blankCards, this.blanksInDeck, json, Consts.GameOptionsData.BLANKS_LIMIT);
        this.playerLimit = assign(player, this.playerLimit, json, Consts.GameOptionsData.PLAYER_LIMIT);
        this.spectatorLimit = assign(spectator, this.spectatorLimit, json, Consts.GameOptionsData.SPECTATOR_LIMIT);
        this.scoreGoal = assign(score, this.scoreGoal, json, Consts.GameOptionsData.SCORE_LIMIT);
        this.winBy = assign(winBy, this.winBy, json, Consts.GameOptionsData.WIN_BY);
        this.timerMultiplier = TimeMultiplier.opt(json, this.timerMultiplier);
        this.password = assign(json, this.password, Consts.GameOptionsData.PASSWORD);
    }

    public static JsonWrapper getOptionsDefaultsJson(Preferences preferences) {
        JsonWrapper obj = new JsonWrapper();
        obj.add(Consts.GameOptionsData.BLANKS_LIMIT, getBlanksLimit(preferences).toJson());
        obj.add(Consts.GameOptionsData.PLAYER_LIMIT, getPlayerLimit(preferences).toJson());
        obj.add(Consts.GameOptionsData.SPECTATOR_LIMIT, getSpectatorLimit(preferences).toJson());
        obj.add(Consts.GameOptionsData.SCORE_LIMIT, getScoreLimit(preferences).toJson());
        obj.add(Consts.GameOptionsData.WIN_BY, getWinBy(preferences).toJson());

        JsonWrapper tm = new JsonWrapper();
        tm.add(Consts.TimeMultiplierData.VALUES, TimeMultiplier.validValuesJson());
        tm.add(Consts.TimeMultiplierData.DEFAULT, DEFAULT_TIME_MULTIPLIER.val);
        obj.add(Consts.GameOptionsData.TIMER_MULTIPLIER, tm);

        return obj;
    }

    private static Preferences.MinDefaultMax getBlanksLimit(Preferences preferences) {
        return preferences.getMinDefaultMax("game/blankCardsLimit", DEFAULT_BLANKS_MIN, DEFAULT_BLANKS_DEF, DEFAULT_BLANKS_MAX);
    }

    private static Preferences.MinDefaultMax getPlayerLimit(Preferences preferences) {
        return preferences.getMinDefaultMax("game/playerLimit", DEFAULT_PLAYER_MIN, DEFAULT_PLAYER_DEF, DEFAULT_PLAYER_MAX);
    }

    private static Preferences.MinDefaultMax getSpectatorLimit(Preferences preferences) {
        return preferences.getMinDefaultMax("game/spectatorLimit", DEFAULT_SPECTATOR_MIN, DEFAULT_SPECTATOR_DEF, DEFAULT_SPECTATOR_MAX);
    }

    private static Preferences.MinDefaultMax getScoreLimit(Preferences preferences) {
        return preferences.getMinDefaultMax("game/scoreLimit", DEFAULT_SCORE_MIN, DEFAULT_SCORE_DEF, DEFAULT_SCORE_MAX);
    }

    private static Preferences.MinDefaultMax getWinBy(Preferences preferences) {
        return preferences.getMinDefaultMax("game/winBy", DEFAULT_WIN_BY_MIN, DEFAULT_WIN_BY_DEF, DEFAULT_WIN_BY_MAX);
    }

    private static int assign(Preferences.MinDefaultMax minDefaultMax, int current, JsonObject obj, Consts.GameOptionsData field) {
        int value;
        JsonElement element = obj.get(field.toString());
        if (element == null) value = current;
        else value = element.getAsInt();

        return Math.max(minDefaultMax.min, Math.min(minDefaultMax.max, value));
    }

    private static String assign(JsonObject obj, String current, Consts.GameOptionsData field) {
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

        synchronized (this.cardcastSetCodes) {
            this.cardcastSetCodes.clear();
            this.cardcastSetCodes.addAll(newOptions.cardcastSetCodes);
        }
    }

    public JsonWrapper toJson(boolean includePassword) {
        JsonWrapper obj = new JsonWrapper();
        obj.add(Consts.GameOptionsData.CARD_SETS, Utils.toIntsJsonArray(cardSetIds));
        obj.add(Consts.GameOptionsData.CARDCAST_SETS, Utils.toStringsJsonArray(cardcastSetCodes));
        obj.add(Consts.GameOptionsData.BLANKS_LIMIT, blanksInDeck);
        obj.add(Consts.GameOptionsData.PLAYER_LIMIT, playerLimit);
        obj.add(Consts.GameOptionsData.SPECTATOR_LIMIT, spectatorLimit);
        obj.add(Consts.GameOptionsData.SCORE_LIMIT, scoreGoal);
        obj.add(Consts.GameOptionsData.WIN_BY, winBy);
        obj.add(Consts.GameOptionsData.TIMER_MULTIPLIER, timerMultiplier.val);
        if (includePassword) obj.add(Consts.GameOptionsData.PASSWORD, password);
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

    @Override
    @SuppressWarnings("SimplifiableIfStatement")
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof GameOptions)) return false;

        GameOptions that = (GameOptions) o;

        if (winBy != that.winBy) return false;
        if (blanksInDeck != that.blanksInDeck) return false;
        if (playerLimit != that.playerLimit) return false;
        if (spectatorLimit != that.spectatorLimit) return false;
        if (scoreGoal != that.scoreGoal) return false;
        if (!cardSetIds.equals(that.cardSetIds)) return false;
        if (!cardcastSetCodes.equals(that.cardcastSetCodes)) return false;
        if (password != null ? !password.equals(that.password) : that.password != null) return false;
        return timerMultiplier == that.timerMultiplier;
    }

    @Override
    public int hashCode() {
        int result = cardSetIds.hashCode();
        result = 31 * result + cardcastSetCodes.hashCode();
        result = 31 * result + winBy;
        result = 31 * result + blanksInDeck;
        result = 31 * result + playerLimit;
        result = 31 * result + spectatorLimit;
        result = 31 * result + scoreGoal;
        result = 31 * result + (password != null ? password.hashCode() : 0);
        result = 31 * result + timerMultiplier.hashCode();
        return result;
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
            if (obj.has(Consts.GameOptionsData.TIMER_MULTIPLIER.toString()))
                return TimeMultiplier.parse(obj.get(Consts.GameOptionsData.TIMER_MULTIPLIER.toString()).getAsString(), current);
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
