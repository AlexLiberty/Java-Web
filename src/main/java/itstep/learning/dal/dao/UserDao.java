package itstep.learning.dal.dao;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import itstep.learning.dal.dto.User;
import itstep.learning.models.UserSignupFormModel;
import itstep.learning.services.db.DbService;
import itstep.learning.services.kdf.KdfService;
import itstep.learning.services.random.RandomService;

import java.sql.*;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

@Singleton
public class UserDao {
    private final Connection connection;
    private final Logger logger;
    private final KdfService kdfService;
    private final RandomService randomService;
    private final DbService dbService;



@Inject
    public UserDao(DbService dbService, Logger logger, KdfService kdfService, RandomService randomService) throws SQLException {
    this.dbService = dbService;
    this.connection = dbService.getConnection();
        this.logger = logger;
        this.kdfService = kdfService;
        this.randomService = randomService;
}

    public User addUser(UserSignupFormModel userModel)
    {
        User user = new User();

        user.setUserId(UUID.randomUUID());
        user.setName(userModel.getName());
        user.setEmail(userModel.getEmail());
        user.setPhone(userModel.getPhoneNumbers().toString());

        String sql = " INSERT INTO users (userId, name, email, phone)"
                + " VALUES (?, ?,?,?) ";

        try(PreparedStatement prep = this.connection.prepareStatement(sql))
        {
            prep.setString(1, user.getUserId().toString());
            prep.setString(2, user.getName().toString());
            prep.setString(3, user.getEmail().toString());
            prep.setString(4, user.getPhone().toString().substring(0,32));
            this.connection.setAutoCommit(false);
            prep.executeUpdate();

        }
        catch (SQLException ex)
        {
           logger.warning("UserDao::addUser" + ex.getMessage());
            try
            {
                this.connection.rollback();
            }
            catch (SQLException e)
            {
                throw new RuntimeException(e);
            }
            return null;
        }

        sql = " INSERT INTO user_access (user_access_id, user_id, role_id, login, salt, dk)"
                + " VALUES (UUID(), ?, 'quest', ?,?,?) ";
        try(PreparedStatement prep = dbService.getConnection().prepareStatement(sql))
        {
            prep.setString(1, user.getUserId().toString());
            prep.setString(2, user.getEmail().toString());
           // String salt = UUID.randomUUID().toString().substring(0, 16);
            String salt = randomService.randomString(16);
            prep.setString(3, salt);
            prep.setString(4, kdfService.dk(userModel.getPassword(), salt));
            prep.executeUpdate();
            this.dbService.getConnection().commit();
        }
        catch (SQLException ex)
        {
            logger.warning("UserDao::addUser" + ex.getMessage());
            try
            {
                this.connection.rollback();
            }
            catch (SQLException e)
            {
                throw new RuntimeException(e);
            }
            return null;
        }

        return user;
    }

    public User authorize(String login, String password)
    {
        String sql = "SELECT * FROM user_access ua " +
                "JOIN users u ON ua.user_id = u.userId" +
                " WHERE ua.login = ?";

        try (PreparedStatement prep =
                     dbService.getConnection().prepareStatement(sql))
        {
            prep.setString(1, login);
            ResultSet rs = prep.executeQuery();
            if (rs.next())
            {
                String dk = kdfService.dk(password, rs.getString("salt"));
                if (Objects.equals(dk, rs.getString("dk")))
                {
                    return User.fromResultSet(rs);
                }
            }
        }
        catch (SQLException ex)
        {
            logger.log( Level.WARNING,"UserDao::authorize {0}", ex.getMessage() );
        }

        return null;
    }

    public boolean installTables(){
        return installUsers() && installUserAccess() && installUserRole();
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

    private boolean installUserRole(){
        String sql = "CREATE TABLE IF NOT EXISTS user_role("
                + "id VARCHAR(16) PRIMARY KEY DEFAULT ( UUID()),"
                + "description VARCHAR(255) NOT NULL,"
                + "canCreate BOOLEAN NOT NULL DEFAULT FALSE,"
                + "canRead BOOLEAN NOT NULL DEFAULT TRUE,"
                + "canUpdate BOOLEAN NOT NULL DEFAULT FALSE,"
                + "canDelete BOOLEAN NOT NULL DEFAULT FALSE"
                + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;";

        try(Statement statement = connection.createStatement())
        {
            statement.executeUpdate(sql);
            logger.info("UserDao::installUserRole OK");
            return true;
        }
        catch (SQLException ex)
        {
            logger.warning("UserDao::installUserRole");
            ex.getMessage();
        }
        return  false;
    }
}
