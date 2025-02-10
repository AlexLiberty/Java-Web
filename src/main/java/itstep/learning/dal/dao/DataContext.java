package itstep.learning.dal.dao;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;

import java.sql.SQLException;
import java.util.logging.Logger;

@Singleton
public class DataContext {
    private final UserDao userDao;
    private final Injector injector;
    private final Logger logger;

    public UserDao getUserDao() {
        return userDao;
    }
@Inject
    public DataContext( Injector injector, Logger logger) throws SQLException {
    this.injector = injector;
    this.logger = logger;
    userDao = injector.getInstance(UserDao.class);
    }
}
