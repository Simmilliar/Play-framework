package controllers;

import controllers.actions.AuthorizationCheckAction;
import controllers.utils.ImageMagickService;
import io.ebean.Ebean;
import io.ebean.text.PathProperties;
import io.ebean.text.json.JsonWriteOptions;
import models.Card;
import models.S3File;
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
	private final ImageMagickService imageMagickService;
	private final S3FileRepository s3FileRepository;

	@Inject
	public CardsController(FormFactory formFactory, CardRepository cardRepository,
						   ImageMagickService imageMagickService, S3FileRepository s3FileRepository)
	{
		this.formFactory = formFactory;
		this.cardRepository = cardRepository;
		this.imageMagickService = imageMagickService;
		this.s3FileRepository = s3FileRepository;
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
					if (imageMagickService.shrinkImage(((File) filePart.getFile()).getAbsolutePath(), 1024))
					{
						S3File s3File = new S3File();
						s3File.file = (File) filePart.getFile();
						s3FileRepository.saveFile(s3File);

						imagesUrls.add(s3File.getUrl());
					}
					else
					{
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