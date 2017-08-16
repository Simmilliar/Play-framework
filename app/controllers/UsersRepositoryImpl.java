package controllers;

import controllers.utils.Utils;
import io.ebean.Ebean;
import models.Users;

import javax.inject.Inject;
import java.util.List;
import java.util.UUID;

public class UsersRepositoryImpl implements UsersRepository
{
	private final Utils utils;

	@Inject
	public UsersRepositoryImpl(Utils utils)
	{
		this.utils = utils;
	}

	@Override
	public Users findByEmail(String email)
	{
		return Ebean.find(Users.class).where().eq("email", email).findUnique();
	}

	@Override
	public Users findById(UUID id)
	{
		return Ebean.find(Users.class, id);
	}

	@Override
	public Users findByFacebookId(long facebookId)
	{
		return Ebean.find(Users.class).where().eq("facebook_id", facebookId).findOne();
	}

	@Override
	public Users findByTwitterId(long twitterId)
	{
		return Ebean.find(Users.class).where().eq("twitter_id", twitterId).findOne();
	}

	@Override
	public List<Users> usersList()
	{
		return Ebean.find(Users.class)
				.where()
				.eq("confirmed", true)
				.findList();
	}

	@Override
	public void saveUser(Users user)
	{
		user.save();
	}

	@Override
	public Users findConfirmedByConfirmationKey(String confirmationKey)
	{
		return Ebean.find(Users.class)
				.where()
				.and()
				.eq("confirmation_key_hash", utils.hashString(confirmationKey, confirmationKey))
				.eq("confirmed", true)
				.gt("confirmation_key_expiration_date", System.currentTimeMillis())
				.endAnd()
				.findOne();
	}

	@Override
	public Users findUnconfirmedByConfirmationKey(String confirmationKey)
	{
		return Ebean.find(Users.class)
				.where()
				.and()
				.eq("confirmation_key_hash", utils.hashString(confirmationKey, confirmationKey))
				.eq("confirmed", false)
				.gt("confirmation_key_expiration_date", System.currentTimeMillis())
				.endAnd()
				.findOne();
	}
}
