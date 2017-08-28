package controllers;

import com.stripe.Stripe;
import com.stripe.model.Charge;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import controllers.actions.AuthorizationCheckAction;
import controllers.repositories.ProductRepository;
import controllers.utils.FileUploadUtils;
import io.ebean.Ebean;
import io.ebean.text.PathProperties;
import io.ebean.text.json.JsonWriteOptions;
import models.Product;
import models.Users;
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
	private final ProductRepository productRepository;
	private final FileUploadUtils fileUploadUtils;

	@Inject
	public ProductsController(FormFactory formFactory, ProductRepository productRepository, FileUploadUtils fileUploadUtils)
	{
		this.formFactory = formFactory;
		this.productRepository = productRepository;
		this.fileUploadUtils = fileUploadUtils;
	}

	public Result products()
	{
		return ok(views.html.products.render());
	}

	public Result productsList()
	{
		JsonWriteOptions jwo = new JsonWriteOptions();
		jwo.setPathProperties(PathProperties.parse("(id, title, description, price, images)"));
		return ok(Ebean.json().toJson(productRepository.getNotMyProducts(
				((Users)ctx().args.get("user")).getUserId())));
	}

	public Result myProducts()
	{
		JsonWriteOptions jwo = new JsonWriteOptions();
		jwo.setPathProperties(PathProperties.parse("(id, title, description, price, images)"));
		return ok(Ebean.json().toJson(productRepository.getMyProducts(
				((Users)ctx().args.get("user")).getUserId())));
	}

	public Result addProduct()
	{
		DynamicForm requestData = formFactory.form().bindFromRequest();

		String title = requestData.get("title");
		String description = requestData.get("description");
		String priceString = requestData.get("price");

		List<Http.MultipartFormData.FilePart<Object>> filePartList = request().body().asMultipartFormData().getFiles();

		if (title == null || description == null || filePartList == null || priceString == null)
		{
			return badRequest("Missing fields");
		}
		if (priceString.equals("") || !priceString.matches("([0-9]+)|([0-9]+\\.[0-9]{2})|([0-9]+,[0-9]{2})"))
		{
			return badRequest("Provide a valid price.");
		}

		int price = (int)Math.round(Double.parseDouble(priceString) * 100);

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
					String imageUrl = fileUploadUtils.uploadImageAndShrink((File) filePart.getFile(), 1024);
					if (imageUrl == null)
					{
						return badRequest("Unable to read file as image.");
					}
					imagesUrls.add(imageUrl);
				}
			}

			Product product = new Product();
			product.setOwner(((Users)ctx().args.get("user")));
			product.setTitle(title);
			product.setDescription(description);
			product.setPrice(price);
			product.setImages(imagesUrls);
			productRepository.saveProduct(product);

			JsonWriteOptions jwo = new JsonWriteOptions();
			jwo.setPathProperties(PathProperties.parse("(id, title, description, price, images)"));

			return ok(Ebean.json().toJson(product, jwo));
		}
		else
		{
			return badRequest("Title and content cannot be empty. Price cannot be less or equal to 0.");
		}
	}

	public Result removeProduct(String productId)
	{
		Product product = productRepository.findById(UUID.fromString(productId));
		if (product == null || !product.getOwner().getUserId().toString().equals(
				((Users)ctx().args.get("user")).getUserId().toString()))
		{
			return badRequest("Wrong product UUID");
		}
		else
		{
			productRepository.saveProduct(product);
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

			Product product = productRepository.findById(UUID.fromString(productId));
			if (product == null || !product.getOwner().getUserId().toString()
					.equals(((Users)ctx().args.get("user")).getUserId().toString()))
			{
				return badRequest();
			}
			product.setPrice(newPrice);
			productRepository.saveProduct(product);

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
		Product product = productRepository.findById(UUID.fromString(productId));
		if (product != null && !product.getOwner().getUserId().toString()
				.equals(((Users)ctx().args.get("user")).getUserId().toString()))
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
		Product product = productRepository.findById(UUID.fromString(productId));
		if (product != null && !product.getOwner().getUserId().toString()
				.equals(((Users)ctx().args.get("user")).getUserId().toString()))
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
				Charge charge = Charge.create(params);
			}
			catch (Exception e)
			{
				e.printStackTrace();
				return badRequest("An error occured during payment.");
			}
			product.setOwner(((Users)ctx().args.get("user")));
			productRepository.saveProduct(product);
			flash("notification", "Traiding success!");

			return redirect(routes.ProductsController.products());
		}
		else
		{
			return redirect(routes.HomeController.index());
		}
	}
}