package com.gianlu.pyxreloaded.data;

import org.jetbrains.annotations.Nullable;

import java.util.*;


/**
 * Class to track which card(s) have been played by players. Can get the card(s) for a player, and
 * also which player played a given card.
 * <p>
 * All methods in this class are synchronized.
 *
 * @author Andy Janata (ajanata@socialgamer.net)
 */
public class PlayerPlayedCardsTracker {
    /**
     * Forward mapping of player to cards.
     */
    private final Map<Player, List<WhiteCard>> playerCardMap = new HashMap<>();
    /**
     * Reverse mapping of cards to player.
     */
    private final Map<Integer, Player> reverseIdMap = new HashMap<>();

    /**
     * Add a played card to the mappings.
     *
     * @param player Player which played the card.
     * @param card   The card the player played.
     */
    public synchronized void addCard(Player player, WhiteCard card) {
        List<WhiteCard> cards = playerCardMap.computeIfAbsent(player, k -> new ArrayList<>(3));
        reverseIdMap.put(card.getId(), player);
        cards.add(card);
    }

    /**
     * Get the {@code Player} that played a card, given the card's ID.
     *
     * @param id Card ID to check.
     * @return The {@code Player} that played the card.
     */
    public synchronized Player getPlayerForId(int id) {
        return reverseIdMap.get(id);
    }

    /**
     * Determine whether a player has played any cards this round.
     *
     * @param player Player to check.
     * @return True if the player has played any cards this round.
     */
    public synchronized boolean hasPlayer(Player player) {
        return playerCardMap.containsKey(player);
    }

    /**
     * @param player The given player
     * @return The list of cards {@code player} has played this round, or {@code null} if they have
     * not played any cards.
     */
    @Nullable
    public synchronized List<WhiteCard> getCards(Player player) {
        return playerCardMap.get(player);
    }

    /**
     * Remove and return a player's cards from the played cards tracking.
     *
     * @param player Player to remove.
     * @return The cards the player had played, or {@code null} if the player had not played cards.
     */
    public synchronized List<WhiteCard> remove(Player player) {
        final List<WhiteCard> cards = playerCardMap.remove(player);
        if (cards != null && cards.size() > 0) reverseIdMap.remove(cards.get(0).getId());
        return cards;
    }

    /**
     * @return The number of players that have played this round.
     */
    public synchronized int size() {
        return playerCardMap.size();
    }

    /**
     * @return The number of played cards per player
     */
    public synchronized int playedCardsCount(Player player) {
        List<WhiteCard> cards = getCards(player);
        return cards == null ? 0 : cards.size();
    }

    /**
     * @return The number of player blank cards per player
     */
    public synchronized int playedWriteInCardsCount(Player player) {
        List<WhiteCard> cards = getCards(player);
        if (cards == null) return 0;

        int count = 0;
        for (WhiteCard card : cards)
            if (WhiteDeck.isBlankCard(card)) count++;

        return count;
    }

    /**
     * @return A {@code Set} of all players that have played this round.
     */
    public synchronized Set<Player> playedPlayers() {
        return playerCardMap.keySet();
    }

    /**
     * Clear both the forward and reverse card mappings.
     */
    public synchronized void clear() {
        playerCardMap.clear();
        reverseIdMap.clear();
    }

    /**
     * @return A {@code Collection} of all played card lists.
     */
    public synchronized Collection<List<WhiteCard>> cards() {
        return playerCardMap.values();
    }

    /**
     * @return A {@code Map} of users to a {@code List} of the cards they played.
     */
    public synchronized Map<User, List<WhiteCard>> cardsByUser() {
        final Map<User, List<WhiteCard>> cardsByUser = new HashMap<>();
        playerCardMap.forEach((key, value) -> cardsByUser.put(key.getUser(), value));
        return Collections.unmodifiableMap(cardsByUser);
    }
}
