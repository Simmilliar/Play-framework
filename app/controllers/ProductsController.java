package controllers;

import com.stripe.Stripe;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import controllers.actions.AuthorizationCheckAction;
import io.ebean.Ebean;
import io.ebean.text.PathProperties;
import io.ebean.text.json.JsonWriteOptions;
import models.data.Product;
import models.data.S3File;
import org.im4java.core.ConvertCmd;
import org.im4java.core.IMOperation;
import play.Logger;
import play.data.DynamicForm;
import play.data.FormFactory;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.With;

import javax.inject.Inject;
import java.io.File;
import java.util.*;

@With(AuthorizationCheckAction.class)
public class ProductsController extends Controller
{
	private final FormFactory formFactory;
	private final Config configFactory = ConfigFactory.load();

	@Inject
	public ProductsController(FormFactory formFactory)
	{
		this.formFactory = formFactory;
	}

	public Result products()
	{
		return ok(views.html.products.render());
	}

	public Result productsList()
	{
		return ok(Ebean.json().toJson(
				Ebean.find(Product.class)
						.select("id, title, description, price, images")
						.where()
						.ne("owner_user_id", request().attrs().get(AuthorizationCheckAction.USER).getUserId())
						.findList()
		));
	}

	public Result myProducts()
	{
		return ok(Ebean.json().toJson(
				Ebean.find(Product.class)
						.select("id, title, description, price, images")
						.where()
						.eq("owner_user_id", request().attrs().get(AuthorizationCheckAction.USER).getUserId())
						.findList()
		));
	}

	public Result addProduct()
	{
		DynamicForm requestData = formFactory.form().bindFromRequest();
		String title = requestData.get("title");
		String description = requestData.get("description");
		if (requestData.get("price") == null || requestData.get("price").equals(""))
		{
			return badRequest("Provide a valid price.");
		}
		int price = (int)Math.round(Double.parseDouble(requestData.get("price")) * 100);
		List<Http.MultipartFormData.FilePart<Object>> filePartList = request().body().asMultipartFormData().getFiles();

		if (title.length() > 0 && description.length() > 0 && price > 0)
		{
			if (filePartList.size() > 5){
				return badRequest("Can't load more than 5 images in one product.");
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

			Product product = new Product();
			product.setOwner(request().attrs().get(AuthorizationCheckAction.USER));
			product.setTitle(title);
			product.setDescription(description);
			product.setPrice(price);
			product.setImages(imagesUrls);
			product.save();

			JsonWriteOptions jwo = new JsonWriteOptions();
			jwo.setPathProperties(PathProperties.parse("(id,title,description,price,images)"));

			return ok(Ebean.json().toJson(product, jwo));
		}
		else
		{
			return badRequest("Title and content cannot be empty. Price cannot be less or equal to 0.");
		}
	}

	public Result removeProduct(String productId)
	{
		Product product = Ebean.find(Product.class, UUID.fromString(productId));
		if (product == null || !product.getOwner().getUserId().toString().equals(
				request().attrs().get(AuthorizationCheckAction.USER).getUserId().toString()))
		{
			return badRequest("Wrong product UUID");
		}
		else
		{
			product.delete();
			return ok("");
		}
	}

	public Result changePrice()
	{
		try
		{
			DynamicForm requestData = formFactory.form().bindFromRequest();
			String productId = requestData.get("productId");
			int newPrice = (int)Math.round(Double.parseDouble(requestData.get("newPrice")) * 100);

			Product product = Ebean.find(Product.class, UUID.fromString(productId));
			if (product == null || !product.getOwner().getUserId().toString().equals(request().attrs().get(AuthorizationCheckAction.USER).getUserId().toString()))
			{
				return badRequest();
			}
			product.setPrice(newPrice);
			product.save();

			JsonWriteOptions jwo = new JsonWriteOptions();
			jwo.setPathProperties(PathProperties.parse("(id,title,description,price,images)"));

			return ok(Ebean.json().toJson(product, jwo));
		}
		catch (Exception e)
		{
			return badRequest();
		}
	}

	public Result buyProduct(String productId)
	{
		Product product = Ebean.find(Product.class, UUID.fromString(productId));
		if (product != null
				&& !product.getOwner().getUserId().toString().equals(request().attrs().get(AuthorizationCheckAction.USER).getUserId().toString()))
		{
			return ok(views.html.buying.render(product, configFactory.getString("stripePublicKey")));
		}
		else
		{
			return redirect(routes.HomeController.index());
		}
	}

	public Result paying(String productId)
	{
		Product product = Ebean.find(Product.class, UUID.fromString(productId));
		if (product != null
				&& !product.getOwner().getUserId().toString().equals(request().attrs().get(AuthorizationCheckAction.USER).getUserId().toString()))
		{
			Stripe.apiKey = configFactory.getString("stripeSecretKey");

			// Token is created using Stripe.js or Checkout!
			DynamicForm requestData = formFactory.form().bindFromRequest();
			// Get the payment token ID submitted by the form:
			String token = requestData.get("stripeToken");

			// Charge the user's card:
			Map<String, Object> params = new HashMap<>();
			params.put("amount", product.getPrice());
			Logger.debug("" + product.getPrice());
			params.put("currency", "uah");
			params.put("description", "Example charge");
			params.put("source", token);

			try
			{
				// todo Charge charge = Charge.create(params);
			}
			catch (Exception e)
			{
				e.printStackTrace();
				return badRequest("An error occured during payment.");
			}
			product.setOwner(request().attrs().get(AuthorizationCheckAction.USER));
			product.save();
			flash("notification", "Traiding success!");

			return redirect(routes.ProductsController.products());
		}
		else
		{
			return redirect(routes.HomeController.index());
		}
	}
}