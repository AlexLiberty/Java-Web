package itstep.learning.dal.dao;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import itstep.learning.dal.dto.User;
import itstep.learning.dal.dto.UserAccess;
import itstep.learning.models.UserSignupFormModel;
import itstep.learning.services.db.DbService;
import itstep.learning.services.kdf.KdfService;
import itstep.learning.services.random.RandomService;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
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

public User getUserById(String id)
{
    UUID uuid;

    try
    {
        uuid = UUID.fromString(id);

    }
    catch (Exception ex)
    {
        logger.log(Level.WARNING, "UserDao::getUserById Parse error: {0}", id);
        return null;
    }
    return getUserById(uuid);
}

public User getUserById(UUID uuid){
    String sql = String.format("SELECT * FROM users u WHERE u.userId = '%s'",
            uuid.toString());
    try(Statement stmt = dbService.getConnection().createStatement())
    {
        ResultSet rs = stmt.executeQuery(sql);
        if(rs.next())
        {
            return User.fromResultSet(rs);
        }
    }
    catch (Exception ex)
    {
        logger.log(Level.WARNING,
                "UserDao::getUserById Parse error: {0}, {1}",
                new Object[] {ex.getMessage(), sql});
    }
    return null;
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

    public UserAccess authorize(String login, String password)
    {
        String sql = "SELECT * FROM user_access ua " +
                //"JOIN users u ON ua.user_id = u.userId" +
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
                    return UserAccess.fromResultSet(rs);
                }
            }
        }
        catch (SQLException ex)
        {
            logger.log( Level.WARNING,"UserDao::authorize {0}", ex.getMessage() );
        }

        return null;
    }

    public boolean installTables()
    {
        Future<Boolean> task1 = CompletableFuture.supplyAsync(this::installUserAccess);
               // .thenApply((b)->{return 1;})
              //  .thenApply(i -> true);
        Future<Boolean> task2 = CompletableFuture.supplyAsync(this::installUsers);
        try {
            boolean res1 = task1.get(); //await task1
            boolean res2 = task2.get(); //await task2
            try
            {
                dbService.getConnection().commit();
            }
            catch (SQLException ignore){}
            return res1 && res2;
        }
        catch (ExecutionException | InterruptedException ex)
        {
            logger.log(Level.WARNING, "UserDao::installTables {0}", ex.getMessage());
            return false;
        }
        //return installUsers() && installUserAccess() && installUserRole();
    }

    private boolean installUsers(){
        String sql = "CREATE TABLE IF NOT EXISTS users("
                + "userId  CHAR(36)     PRIMARY KEY DEFAULT( UUID() ),"
                + "name    VARCHAR(128) NOT NULL,"
                + "email   VARCHAR(256)     NULL,"
                + "phone   VARCHAR(32)      NULL,"
                + "delete_moment DATETIME NULL "
                + ") Engine = InnoDB, DEFAULT CHARSET = utf8mb4";
        try(Statement statement = connection.createStatement())
        {
            statement.executeUpdate(sql);
            logger.info("UserDao::installUsers OK");
            return true;
        }
        catch (SQLException ex)
        {
            logger.log(Level.WARNING, "UserDao::installUsers {0} sql: '{1}'",
                    new Object[] {ex.getMessage(), sql});
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
                + "ua_delete_dt DATETIME NULL,"
                + "UNIQUE(login)"
                + ") Engine = InnoDB, DEFAULT CHARSET = utf8mb4";
        try(Statement statement = dbService.getConnection().createStatement())
        {
            statement.executeUpdate(sql);
            logger.info("UserDao::installUserAccess OK");
            return true;
        }
        catch (SQLException ex)
        {
            logger.log(Level.WARNING, "UserDao::installUserAccess {0} sql: '{1}'",
                    new Object[] {ex.getMessage(), sql});
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

    public Future<Boolean> updateAsync(User user) {
        return CompletableFuture.supplyAsync(() -> {
            Map<String, Object> data = new HashMap<>();

            if (user.getName() != null) {
                data.put("name", user.getName());
            }
            if (user.getPhone() != null) {
                data.put("phone", user.getPhone());
            }
            if (data.isEmpty()) return true;

            StringBuilder sql = new StringBuilder("UPDATE users SET ");
            boolean isFirst = true;
            for (String key : data.keySet()) {
                if (!isFirst) {
                    sql.append(", ");
                }
                sql.append(key).append(" = ?");
                isFirst = false;
            }
            sql.append(" WHERE userId = ?");

            try (PreparedStatement prep = dbService.getConnection().prepareStatement(sql.toString())) {
                int param = 1;
                for (Object value : data.values()) {
                    prep.setObject(param++, value);
                }
                prep.setString(param, user.getUserId().toString());
                prep.execute();
                dbService.getConnection().commit();
                return true;
            } catch (SQLException e) {
                logger.log(Level.WARNING, "UserDao::updateAsync {0}", e.getMessage());
                try { dbService.getConnection().rollback(); } catch (SQLException ignore) {}
                return false;
            }
        });
    }

    public Future deleteAsync(User user)
    {
        String sql =  String.format("UPDATE users SET delete_moment = CURRENT_TIMESTAMP," +
                "name = '', email = NULL, phone = NULL WHERE userId = '%s'",
                user.getUserId().toString());

        String sql2 = "UPDATE user_access SET ua_delete_dt = CURRENT_TIMESTAMP," +
                "login = UUID() WHERE userId = ?";

        Future task1 = CompletableFuture.runAsync(() ->
        {
            try (Statement stmt = dbService.getConnection().createStatement()) {
                stmt.executeUpdate(sql);
            } catch (SQLException ex) {
                logger.log(Level.WARNING, "UserDao::delete1 {0}", ex.getMessage());
                try{dbService.getConnection().rollback();}
                catch (SQLException ignore){}
            }
        });

        Future task2 = CompletableFuture.runAsync(() ->
        {
            try (Statement stmt = dbService.getConnection().createStatement()) {
                stmt.executeUpdate(sql2);
            } catch (SQLException ex) {
                logger.log(Level.WARNING, "UserDao::delete2 {0}", ex.getMessage());
                try{dbService.getConnection().rollback();}
                catch (SQLException ignore){}
            }
        });

        return CompletableFuture.allOf((CompletableFuture<?>) task1, (CompletableFuture<?>) task2)
                .thenRun(() ->
                {
                    try
                    {
                        dbService.getConnection().commit();
                    }
                catch(SQLException ignore){}});
    }
}
