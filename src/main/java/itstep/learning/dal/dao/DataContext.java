package itstep.learning.dal.dao;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;

import java.sql.SQLException;

@Singleton
public class DataContext {
    private final UserDao userDao;
    private final AccessTokenDao accessTokenDao;
    private final Injector injector;
    private final CategoryDao categoryDao;
    private final ProductDao productDao;

    @Inject
    public DataContext(Injector injector) throws SQLException
    {
        this.injector = injector;
        userDao = injector.getInstance(UserDao.class);
        accessTokenDao = injector.getInstance(AccessTokenDao.class);
        categoryDao = injector.getInstance(CategoryDao.class);
        productDao = injector.getInstance(ProductDao.class);
    }

    public UserDao getUserDao() {
        return userDao;
    }

    public AccessTokenDao getAccessTokenDao() {
        return accessTokenDao;
    }

    public CategoryDao getCategoryDao() {
        return categoryDao;
    }

    public ProductDao getProductDao() {
        return productDao;
    }
}
