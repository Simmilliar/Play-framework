package controllers;

import io.ebean.Ebean;
import models.Card;

import java.util.List;
import java.util.UUID;

public class CardRepositoryImpl implements CardRepository
{
	@Override
	public Card findCardById(UUID cardId)
	{
		return Ebean.find(Card.class, cardId);
	}

	@Override
	public List<Card> findUsersCard(UUID userId)
	{
		return Ebean.find(Card.class).where().eq("owner_user_id", userId).findList();
	}

	@Override
	public void saveCard(Card card)
	{
		card.save();
	}

	@Override
	public void deleteCard(Card card)
	{
		card.delete();
	}
}
