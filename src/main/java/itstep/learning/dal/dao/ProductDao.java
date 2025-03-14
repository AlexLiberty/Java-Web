package itstep.learning.dal.dao;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import itstep.learning.dal.dto.Product;
import itstep.learning.services.db.DbService;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

@Singleton
public class ProductDao
{
    private final Logger logger;
    private final DbService dbService;

    @Inject
    public ProductDao(Logger logger, DbService dbService) {
        this.logger = logger;
        this.dbService = dbService;
    }

    public boolean installTables()
    {
        String sql = "CREATE TABLE IF NOT EXISTS products("
                + "product_id VARCHAR(36) PRIMARY KEY DEFAULT ( UUID()),"
                + "category_id VARCHAR(36) NOT NULL,"
                + "product_title VARCHAR(255) NOT NULL,"
                + "product_description VARCHAR(255) NOT NULL,"
                + "product_slug VARCHAR(255)  NULL,"
                + "product_imageId VARCHAR(255)  NULL,"
                + "product_price FLOAT NOT  NULL,"
                + "product_stock INT  NOT NULL,"
                + "product_delete_moment DATETIME NULL,"
                + "UNIQUE(product_slug)"
                + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;";
        try(Statement statement = dbService.getConnection().createStatement())
        {
            statement.executeUpdate(sql);
            dbService.getConnection().commit();
            logger.info("Product::installTables OK");
            return true;

        }
        catch (SQLException ex)
        {
            logger.log(
                    Level.WARNING,
                    "ProductDao::installTables {0} sql: '{1}'",
                    new Object[] {ex.getMessage(), sql});
        }
        return false;
    }

    public Product addNewProduct(Product product)
    {
        product.setProductId(UUID.randomUUID());
        String sql = "INSERT INTO products (product_id, category_id, product_title, product_description, product_slug, product_imageId, product_price, product_stock, product_delete_moment )"
                + "VALUES(?,?,?,?,?,?,?,?,?)";
        try(PreparedStatement prep = dbService.getConnection().prepareStatement(sql))
        {
            prep.setString(1, product.getProductId().toString());
            prep.setString(2, product.getCategoryId().toString());
            prep.setString(3, product.getProductTitle());
            prep.setString(4, product.getProductDescription());
            prep.setString(5, product.getProductSlug());
            prep.setString(6, product.getProductImageId());
            prep.setDouble(7, product.getPrice());
            prep.setInt(8, product.getStock());
            prep.setString(9, product.getDeleteMoment() == null? null:
                    String.valueOf(new Timestamp(product.getDeleteMoment().getTime())));
            prep.executeUpdate();
            dbService.getConnection().commit();
            return product;
        }
        catch (SQLException ex)
        {
            logger.log(
                    Level.WARNING,
                    "ProductDao::AddNewProduct {0} sql: '{1}'",
                    new Object[] {ex.getMessage(), sql});
            return null;
        }
    }

    public List<Product> getProductsByCategory(UUID categoryId) {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT * FROM products WHERE category_id = ?";
        try (PreparedStatement prep = dbService.getConnection().prepareStatement(sql)) {
            prep.setString(1, categoryId.toString());
            ResultSet resultSet = prep.executeQuery();
            while (resultSet.next()) {
                Product product = new Product();
                product.setProductId(UUID.fromString(resultSet.getString("product_id")));
                product.setCategoryId(UUID.fromString(resultSet.getString("category_id")));
                product.setProductTitle(resultSet.getString("product_title"));
                product.setProductDescription(resultSet.getString("product_description"));
                product.setProductSlug(resultSet.getString("product_slug"));
                product.setProductImageId(resultSet.getString("product_imageId"));
                product.setPrice(resultSet.getDouble("product_price"));
                product.setStock(resultSet.getInt("product_stock"));
                product.setDeleteMoment(resultSet.getTimestamp("product_delete_moment"));
                products.add(product);
            }
        } catch (SQLException ex) {
            logger.log(Level.WARNING, "ProductDao::getProductsByCategory {0} sql: '{1}'", new Object[]{ex.getMessage(), sql});
        }
        return products;
    }

    public List<Product> getAllProducts() {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT * FROM products";
        try (PreparedStatement prep = dbService.getConnection().prepareStatement(sql)) {
            ResultSet resultSet = prep.executeQuery();
            while (resultSet.next()) {
                Product product = new Product();
                product.setProductId(UUID.fromString(resultSet.getString("product_id")));
                product.setCategoryId(UUID.fromString(resultSet.getString("category_id")));
                product.setProductTitle(resultSet.getString("product_title"));
                product.setProductDescription(resultSet.getString("product_description"));
                product.setProductSlug(resultSet.getString("product_slug"));
                product.setProductImageId(resultSet.getString("product_imageId"));
                product.setPrice(resultSet.getDouble("product_price"));
                product.setStock(resultSet.getInt("product_stock"));
                product.setDeleteMoment(resultSet.getTimestamp("product_delete_moment"));
                products.add(product);
            }
        } catch (SQLException ex) {
            logger.log(Level.WARNING, "ProductDao::getAllProducts {0} sql: '{1}'", new Object[]{ex.getMessage(), sql});
        }
        return products;
    }
}
