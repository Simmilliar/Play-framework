package controllers;

import controllers.actions.AuthorizationCheckAction;
import io.ebean.Ebean;
import io.ebean.text.PathProperties;
import io.ebean.text.json.JsonWriteOptions;
import models.data.Card;
import models.data.S3File;
import org.im4java.core.ConvertCmd;
import org.im4java.core.IMOperation;
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

	@Inject
	public CardsController(FormFactory formFactory)
	{
		this.formFactory = formFactory;
	}

	public Result cards()
	{
		return ok(views.html.cards.render());
	}

	public Result loadCards()
	{
		return ok(Ebean.json().toJson(
				Ebean.find(Card.class)
						.select("id, title, content, images")
						.where()
						.eq("owner_user_id", request().attrs().get(AuthorizationCheckAction.USER).getUserId())
						.findList()
		));
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
					try
					{
						ConvertCmd convertCmd = new ConvertCmd();
						IMOperation imOperation = new IMOperation();
						imOperation.addImage(((File) filePart.getFile()).getAbsolutePath());
						imOperation.resize(1024, 1024, '>');
						imOperation.addImage(((File) filePart.getFile()).getAbsolutePath());
						convertCmd.run(imOperation);

						S3File s3File = new S3File();
						s3File.file = (File) filePart.getFile();
						s3File.save();

						imagesUrls.add(s3File.getUrl());
					}
					catch (Exception e)
					{
						e.printStackTrace();
						return badRequest("Unable to read file as image.");
					}
				}
			}

			Card card = new Card();
			card.setOwner(request().attrs().get(AuthorizationCheckAction.USER));
			card.setTitle(title);
			card.setContent(content);
			card.setImages(imagesUrls);
			card.save();

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
		Card card = Ebean.find(Card.class, UUID.fromString(cardId));
		if (card == null || !card.getOwner().getUserId().toString().equals(
				request().attrs().get(AuthorizationCheckAction.USER).getUserId().toString()))
		{
			return badRequest("Wrong card UUID");
		}
		else
		{
			card.delete();
			return ok("");
		}
	}
}