package controllers;

import controllers.actions.AuthorizationCheckAction;
import controllers.repositories.CardRepository;
import controllers.utils.FileUploadUtils;
import controllers.utils.Utils;
import io.ebean.Ebean;
import io.ebean.text.PathProperties;
import io.ebean.text.json.JsonWriteOptions;
import models.Card;
import models.Users;
import play.data.DynamicForm;
import play.data.FormFactory;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.With;

import javax.inject.Inject;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@With(AuthorizationCheckAction.class)
public class CardsController extends Controller
{
	private final FormFactory formFactory;
	private final CardRepository cardRepository;
	private final FileUploadUtils fileUploadUtils;
	private final Utils utils;

	@Inject
	public CardsController(FormFactory formFactory, CardRepository cardRepository, FileUploadUtils fileUploadUtils,
						   Utils utils)
	{
		this.formFactory = formFactory;
		this.cardRepository = cardRepository;
		this.fileUploadUtils = fileUploadUtils;
		this.utils = utils;
	}

	public Result cards()
	{
		return ok(views.html.cards.render());
	}

	public Result loadCards()
	{
		JsonWriteOptions jwo = new JsonWriteOptions();
		jwo.setPathProperties(PathProperties.parse("(id, title, content, images)"));
		return ok(Ebean.json().toJson(cardRepository.findUsersCard(
				((Users)ctx().args.get("user")).getUserId()), jwo));
	}

	public Result addCard()
	{
		DynamicForm requestData = formFactory.form().bindFromRequest();
		String title = requestData.get("title");
		String content = requestData.get("content");

		if (title == null || content == null || request().body().asMultipartFormData() == null)
		{
			return badRequest("Missing fields.");
		}

		List<Http.MultipartFormData.FilePart<Object>> filePartList = request().body().asMultipartFormData().getFiles();

		if (title.length() > 0 && content.length() > 0)
		{
			if (filePartList.size() > 5){
				return badRequest("Can't load more than 5 files in one card.");
			}
			List<String> imagesUrls = new ArrayList<>();
			for(Http.MultipartFormData.FilePart<Object> filePart : filePartList)
			{
				if (filePart != null && ((File)filePart.getFile()).length() > 0)
				{
					String imageUrl = fileUploadUtils.uploadImageAndShrink((File)filePart.getFile(), 1024);
					if (imageUrl != null) {
						imagesUrls.add(imageUrl);
					} else {
						return badRequest("Unable to read file as image.");
					}
				}
			}

			Card card = new Card();
			card.setOwner(((Users)ctx().args.get("user")));
			card.setTitle(title);
			card.setContent(content);
			card.setImages(imagesUrls);
			cardRepository.saveCard(card);

			JsonWriteOptions jwo = new JsonWriteOptions();
			jwo.setPathProperties(PathProperties.parse("(id,title,content,images)"));

			return ok(Ebean.json().toJson(card, jwo));
		}
		else
		{
			return badRequest("Title and content cannot be empty.");
		}
	}

	public Result deleteCard(String cardId)
	{
		if (cardId == null || !utils.isUUIDValid(cardId)) {
			return badRequest("Wrong card UUID");
		}
		Card card = cardRepository.findCardById(UUID.fromString(cardId));
		if (card == null || !card.getOwner().getUserId().toString().equals(
				((Users)ctx().args.get("user")).getUserId().toString()))
		{
			return badRequest("Wrong card UUID");
		}
		else
		{
			cardRepository.deleteCard(card);
			return ok("");
		}
	}
}