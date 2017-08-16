package controllers;

import io.ebean.Ebean;
import models.Product;

import java.util.List;
import java.util.UUID;

public class ProductRepositoryImpl implements ProductRepository
{
	@Override
	public Product findById(UUID id)
	{
		return Ebean.find(Product.class, id);
	}

	@Override
	public List<Product> getNotMyProducts(UUID myId)
	{
		return Ebean.find(Product.class)
				.where()
				.ne("owner_user_id", myId)
				.findList();
	}

	@Override
	public List<Product> getMyProducts(UUID myId)
	{
		return Ebean.find(Product.class)
				.where()
				.eq("owner_user_id", myId)
				.findList();
	}

	@Override
	public void saveProduct(Product product)
	{
		product.save();
	}
}