package controllers;

import com.google.inject.ImplementedBy;
import models.Card;

import java.util.List;
import java.util.UUID;

@ImplementedBy(CardRepositoryImpl.class)
public interface CardRepository
{
	Card findCardById(UUID cardId);
	List<Card> findUsersCard(UUID userId);
	void saveCard(Card card);
	void deleteCard(Card card);
}