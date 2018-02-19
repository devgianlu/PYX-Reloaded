package com.gianlu.pyxreloaded.cards;

import com.gianlu.pyxreloaded.game.OutOfCardsException;

import java.util.*;


/**
 * Deck of White Cards.
 * <p>
 * This class is thread-safe.
 *
 * @author Andy Janata (ajanata@socialgamer.net)
 */
public class WhiteDeck {
    private final List<WhiteCard> deck;
    private final List<WhiteCard> discard;
    private int lastBlankCardId = -1;

    /**
     * Create a new white card deck, loading the cards from the database and shuffling them.
     */
    public WhiteDeck(Collection<CardSet> cardSets, int numBlanks) {
        Set<WhiteCard> allCards = new HashSet<>();
        for (CardSet cardSet : cardSets) allCards.addAll(cardSet.getWhiteCards());
        deck = new ArrayList<>(allCards);
        for (int i = 0; i < numBlanks; i++) deck.add(createBlankCard());
        Collections.shuffle(deck);
        discard = new ArrayList<>(deck.size());
    }

    /**
     * Checks if a particular card is a blank card.
     *
     * @param card Card to check.
     * @return True if the card is a blank card.
     */
    public static boolean isBlankCard(WhiteCard card) {
        return card instanceof BlankWhiteCard;
    }

    /**
     * Get the next card from the top of deck.
     *
     * @return The next card.
     * @throws OutOfCardsException There are no more cards in the deck.
     */
    public synchronized WhiteCard getNextCard() throws OutOfCardsException {
        if (deck.size() == 0) throw new OutOfCardsException();
        // we have an ArrayList here, so this is faster
        return deck.remove(deck.size() - 1);
    }

    /**
     * Add a card to the discard pile.
     *
     * @param card Card to add to discard pile.
     */
    public synchronized void discard(WhiteCard card) {
        if (card != null) {
            // clear any player text
            if (isBlankCard(card)) ((BlankWhiteCard) card).clear();

            discard.add(card);
        }
    }

    /**
     * Shuffles the discard pile and puts the cards under the cards remaining in the deck.
     */
    public synchronized void reshuffle() {
        Collections.shuffle(discard);
        deck.addAll(0, discard);
        discard.clear();
    }

    /**
     * Creates a new blank card.
     *
     * @return A newly created blank card.
     */
    public WhiteCard createBlankCard() {
        return new BlankWhiteCard(--lastBlankCardId);
    }
}
