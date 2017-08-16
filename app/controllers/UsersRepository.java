package controllers;

import com.google.inject.ImplementedBy;
import models.Users;

import java.util.List;
import java.util.UUID;

@ImplementedBy(UsersRepositoryImpl.class)
public interface UsersRepository
{
	Users findByEmail(String email);
	Users findById(UUID id);
	Users findByFacebookId(long facebookId);
	Users findByTwitterId(long twitterId);
	List<Users> usersList();
	void saveUser(Users user);
	Users findConfirmedByConfirmationKey(String confirmationKey);
	Users findUnconfirmedByConfirmationKey(String confirmationKey);
}