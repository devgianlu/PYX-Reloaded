package net.socialgamer.cah.data;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.socialgamer.cah.Constants.GameOptionData;
import net.socialgamer.cah.Utils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class GameOptions {
    // TODO move these out to pyx.properties
    public static final int MIN_SCORE_LIMIT = 4;
    public static final int DEFAULT_SCORE_LIMIT = 8;
    public static final int MAX_SCORE_LIMIT = 69;
    public static final int MIN_PLAYER_LIMIT = 3;
    public static final int DEFAULT_PLAYER_LIMIT = 10;
    public static final int MAX_PLAYER_LIMIT = 20;
    public static final int MIN_SPECTATOR_LIMIT = 0;
    public static final int DEFAULT_SPECTATOR_LIMIT = 10;
    public static final int MAX_SPECTATOR_LIMIT = 20;
    public static final int MIN_BLANK_CARD_LIMIT = 0;
    public static final int DEFAULT_BLANK_CARD_LIMIT = 0;
    public static final int MAX_BLANK_CARD_LIMIT = 30;
    public final Set<Integer> cardSetIds = new HashSet<>();
    // These are the default values new games get.
    public int blanksInDeck = DEFAULT_BLANK_CARD_LIMIT;
    public int playerLimit = DEFAULT_PLAYER_LIMIT;
    public int spectatorLimit = DEFAULT_SPECTATOR_LIMIT;
    public int scoreGoal = DEFAULT_SCORE_LIMIT;
    public String password = "";
    public String timerMultiplier = "1.0x";

    public static GameOptions deserialize(final String text) {
        GameOptions options = new GameOptions();
        if (text == null || text.isEmpty()) return options;

        JsonObject json = new Gson().toJsonTree(text).getAsJsonObject();
        String[] cardSetsParsed = json.get(GameOptionData.CARD_SETS.toString()).getAsString().split(",");
        for (String cardSetId : cardSetsParsed) {
            if (!cardSetId.isEmpty()) options.cardSetIds.add(Integer.parseInt(cardSetId));
        }

        options.blanksInDeck = Math.max(MIN_BLANK_CARD_LIMIT, Math.min(MAX_BLANK_CARD_LIMIT, Utils.optInt(json, GameOptionData.BLANKS_LIMIT.toString(), options.blanksInDeck)));
        options.playerLimit = Math.max(MIN_PLAYER_LIMIT, Math.min(MAX_PLAYER_LIMIT, Utils.optInt(json, GameOptionData.PLAYER_LIMIT.toString(), options.playerLimit)));
        options.spectatorLimit = Math.max(MIN_SPECTATOR_LIMIT, Math.min(MAX_SPECTATOR_LIMIT, Utils.optInt(json, GameOptionData.SPECTATOR_LIMIT.toString(), options.spectatorLimit)));
        options.scoreGoal = Math.max(MIN_SCORE_LIMIT, Math.min(MAX_SCORE_LIMIT, Utils.optInt(json, GameOptionData.SCORE_LIMIT.toString(), options.scoreGoal)));
        options.timerMultiplier = Utils.optString(json, GameOptionData.TIMER_MULTIPLIER.toString(), options.timerMultiplier);
        options.password = Utils.optString(json, GameOptionData.PASSWORD.toString(), options.password);

        return options;
    }

    /**
     * Update the options in-place (so that the Game doesn't need more locks).
     *
     * @param newOptions The new options to use.
     */
    public void update(final GameOptions newOptions) {
        this.scoreGoal = newOptions.scoreGoal;
        this.playerLimit = newOptions.playerLimit;
        this.spectatorLimit = newOptions.spectatorLimit;
        synchronized (this.cardSetIds) {
            this.cardSetIds.clear();
            this.cardSetIds.addAll(newOptions.cardSetIds);
        }
        this.blanksInDeck = newOptions.blanksInDeck;
        this.password = newOptions.password;
        this.timerMultiplier = newOptions.timerMultiplier;
    }

    /**
     * Get the options in a form that can be sent to clients.
     *
     * @param includePassword Include the actual password with the information. This should only be
     *                        sent to people in the game.
     * @return This game's general information: ID, host, state, player list, etc.
     */
    public Map<GameOptionData, Object> serialize(final boolean includePassword) {
        final Map<GameOptionData, Object> info = new HashMap<>();

        info.put(GameOptionData.CARD_SETS, cardSetIds);
        info.put(GameOptionData.BLANKS_LIMIT, blanksInDeck);
        info.put(GameOptionData.PLAYER_LIMIT, playerLimit);
        info.put(GameOptionData.SPECTATOR_LIMIT, spectatorLimit);
        info.put(GameOptionData.SCORE_LIMIT, scoreGoal);
        info.put(GameOptionData.TIMER_MULTIPLIER, timerMultiplier);
        if (includePassword) info.put(GameOptionData.PASSWORD, password);

        return info;
    }

    public JsonObject toJson(boolean includePassword) {
        JsonObject obj = new JsonObject();

        obj.add(GameOptionData.CARD_SETS.toString(), Utils.toJsonArray(cardSetIds));
        obj.addProperty(GameOptionData.BLANKS_LIMIT.toString(), blanksInDeck);
        obj.addProperty(GameOptionData.PLAYER_LIMIT.toString(), playerLimit);
        obj.addProperty(GameOptionData.SPECTATOR_LIMIT.toString(), spectatorLimit);
        obj.addProperty(GameOptionData.SCORE_LIMIT.toString(), scoreGoal);
        obj.addProperty(GameOptionData.TIMER_MULTIPLIER.toString(), timerMultiplier);
        if (includePassword) obj.addProperty(GameOptionData.PASSWORD.toString(), password);

        return obj;
    }

    /**
     * @return Selected card set IDs which are local to PYX, for querying the database.
     */
    public Set<Integer> getPyxCardSetIds() {
        Set<Integer> pyxCardSetIds = new HashSet<>();
        for (Integer cardSetId : cardSetIds) {
            if (cardSetId > 0) pyxCardSetIds.add(cardSetId);
        }

        return pyxCardSetIds;
    }
}
