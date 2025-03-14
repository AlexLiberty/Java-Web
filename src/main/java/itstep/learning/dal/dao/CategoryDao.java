package itstep.learning.dal.dao;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import itstep.learning.dal.dto.Category;
import itstep.learning.dal.dto.Product;
import itstep.learning.services.db.DbService;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Singleton
public class CategoryDao
{
    private final DbService dbService;
    private final Logger logger;

    @Inject
    public CategoryDao(DbService dbService, Logger logger) {
        this.dbService = dbService;
        this.logger = logger;
    }

    public boolean seedData()
    {
        String sql = "INSERT INTO categories VALUES"
                + "('14780dcf-fb75-11ef-90a1-62517600596c', 'Вироби зі скла', 'Декоративні вироби зі скла, а також скляний посуд', 'glass', 'glass.jpg', NULL ),"
                + "('24780dcf-fb75-11ef-90a1-62517600596c', 'Офісні товари', 'Настільні сувеніри', 'office', 'office.jpg', NULL ),"
                + "('34780dcf-fb75-11ef-90a1-62517600596c', 'Вироби з каменю', 'Декоративні вироби зі каменю, а також кам`яний посуд', 'stone', 'stone.jpg', NULL ),"
                + "('44780dcf-fb75-11ef-90a1-62517600596c', 'Вироби з дерева', 'Декоративні вироби з дерева, а також дерев''яний посуд', 'wood', 'wood.jpg', NULL )";
        try(Statement statement = dbService.getConnection().createStatement())
        {
            statement.executeUpdate(sql);
            dbService.getConnection().commit();
            logger.info("Category::seedData OK");
            return true;

        }
        catch (SQLException ex)
        {
            logger.log(Level.WARNING, "categoryDao::seedData {0} sql: '{1}'", new Object[] {ex.getMessage(), sql});
        }
        return false;
    }

    public boolean installTables()
    {
        String sql = "CREATE TABLE IF NOT EXISTS categories("
                + "category_id VARCHAR(36) PRIMARY KEY DEFAULT ( UUID()),"
                + "category_title VARCHAR(255) NOT NULL,"
                + "category_description VARCHAR(255) NOT NULL,"
                + "category_slug VARCHAR(255) NOT NULL,"
                + "category_imageId VARCHAR(255) NOT NULL,"
                + "category_delete_moment DATETIME NULL,"
                + "UNIQUE(category_slug)"
                + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;";
        try(Statement statement = dbService.getConnection().createStatement())
        {
            statement.executeUpdate(sql);
            dbService.getConnection().commit();
            logger.info("Category::installTables OK");
            return true;

        }
        catch (SQLException ex)
        {
            logger.log(
                    Level.WARNING,
                    "categoryDao::installTables {0} sql: '{1}'",
                    new Object[] {ex.getMessage(), sql});
        }
        return false;
    }

    public List<Category> getList()
    {
        List<Category> res = new ArrayList<>();
        String sql = "SELECT * FROM categories";
        try(Statement statement = dbService.getConnection().createStatement())
        {
            ResultSet rs = statement.executeQuery(sql);
            while (rs.next())
            {
                res.add(Category.fromResultSet(rs));
            }

        }
        catch (SQLException ex)
        {
            logger.log(
                    Level.WARNING,
                    "categoryDao::getList {0} sql: '{1}'",
                    new Object[] {ex.getMessage(), sql});
        }

        return res;
    }

    public Category getCategoryBySlug(String slug)
    {
        if(slug == null)
        {
            return null;
        }
        Category category = null;

        String sql = "SELECT * FROM categories c LEFT JOIN products p ON "
                + "c.category_id = p.category_id WHERE c.category_slug = ?";
        try(PreparedStatement prep = dbService.getConnection().prepareStatement(sql))
        {
            prep.setString(1, slug);
            ResultSet rs = prep.executeQuery();
            if (rs.next())
            {
                category = Category.fromResultSet(rs);
                List<Product> products = new ArrayList<>();
                do {
                    try {
                        products.add(Product.fromResultSet(rs));
                    } catch (Exception ignore) {}
                } while (rs.next());
                category.setProducts(products);
            }
        }
        catch (SQLException ex)
        {
            logger.log(
                    Level.WARNING,
                    "categoryDao::getCategoryBySlug {0} sql: '{1}'",
                    new Object[] {ex.getMessage(), sql});
        }
        return category;
    }
}
