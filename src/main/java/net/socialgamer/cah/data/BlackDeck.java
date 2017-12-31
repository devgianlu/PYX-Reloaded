package net.socialgamer.cah.data;

import java.util.*;


/**
 * Deck of Black Cards.
 * <p>
 * This class is thread-safe.
 *
 * @author Andy Janata (ajanata@socialgamer.net)
 */
public class BlackDeck {
    private final List<BlackCard> deck;
    private final List<BlackCard> discard;

    /**
     * Create a new black card deck, loading the cards from the database and shuffling them.
     */
    public BlackDeck(Collection<CardSet> cardSets) {
        Set<BlackCard> allCards = new HashSet<>();
        for (CardSet cardSet : cardSets) allCards.addAll(cardSet.getBlackCards());
        deck = new ArrayList<>(allCards);
        Collections.shuffle(deck);
        discard = new ArrayList<>(deck.size());
    }

    /**
     * Get the next card from the top of deck.
     *
     * @return The next card.
     * @throws OutOfCardsException There are no more cards in the deck.
     */
    public synchronized BlackCard getNextCard() throws OutOfCardsException {
        if (deck.size() == 0) throw new OutOfCardsException();
        // we have an ArrayList here, so this is faster
        return deck.remove(deck.size() - 1);
    }

    /**
     * Add a card to the discard pile.
     *
     * @param card Card to add to discard pile.
     */
    public synchronized void discard(final BlackCard card) {
        if (card != null) discard.add(card);
    }

    /**
     * Shuffles the discard pile and puts the cards under the cards remaining in the deck.
     */
    public synchronized void reshuffle() {
        Collections.shuffle(discard);
        deck.addAll(0, discard);
        discard.clear();
    }

    public synchronized int totalCount() {
        return deck.size() + discard.size();
    }
}
