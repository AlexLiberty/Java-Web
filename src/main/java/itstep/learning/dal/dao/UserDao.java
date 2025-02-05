package itstep.learning.dal.dao;


import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Logger;

public class UserDao {
    private final Connection connection;
    private final Logger logger;

    public UserDao(Connection connection, Logger logger) {
        this.connection = connection;
        this.logger = logger;
    }

    public boolean installTables(){
        return installUsers() && installUserAccess();
    }

    private boolean installUsers(){
        String sql = "CREATE TABLE IF NOT EXISTS users("
                + "userId  CHAR(36)     PRIMARY KEY DEFAULT( UUID() ),"
                + "name    VARCHAR(128) NOT NULL,"
                + "email   VARCHAR(256)     NULL,"
                + "phone   VARCHAR(32)      NULL"
                + ") Engine = InnoDB, DEFAULT CHARSET = utf8mb4";
        try(Statement statement = connection.createStatement())
        {
            statement.executeUpdate(sql);
            logger.info("UserDao::installUsers OK");
            return true;
        }
        catch (SQLException ex)
        {
            logger.warning("UserDao::installUsers");
            ex.getMessage();
        }
        return  false;
    }

    private boolean installUserAccess(){
        String sql = "CREATE TABLE IF NOT EXISTS user_access("
                + "user_access_id  CHAR(36)     PRIMARY KEY DEFAULT( UUID() ),"
                + "user_id    CHAR(36) NOT NULL,"
                + "role_id    VARCHAR(16) NOT NULL,"
                + "login   VARCHAR(128)     NULL,"
                + "salt   VARCHAR(16)      NULL,"
                + "dk   VARCHAR(20)      NULL,"
                + "UNIQUE(login)"
                + ") Engine = InnoDB, DEFAULT CHARSET = utf8mb4";
        try(Statement statement = connection.createStatement())
        {
            statement.executeUpdate(sql);
            logger.info("UserDao::installUserAccess OK");
            return true;
        }
        catch (SQLException ex)
        {
           logger.warning("UserDao::installUserAccess");
            ex.getMessage();
        }
        return  false;
    }
}
