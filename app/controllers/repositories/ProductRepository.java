package controllers.repositories;

import com.google.inject.ImplementedBy;
import models.Product;

import java.util.List;
import java.util.UUID;

@ImplementedBy(ProductRepositoryImpl.class)
public interface ProductRepository
{
	Product findById(UUID id);
	List<Product> getNotMyProducts(UUID myId);
	List<Product> getMyProducts(UUID myId);
	void saveProduct(Product product);
}