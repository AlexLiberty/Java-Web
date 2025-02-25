package itstep.learning.dal.dao;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import itstep.learning.dal.dto.AccessToken;
import itstep.learning.dal.dto.UserAccess;
import itstep.learning.services.db.DbService;

import java.sql.*;
import java.util.Date;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

@Singleton
public class AccessTokenDao
{
    private final Logger logger;
    private final DbService dbService;

    @Inject
    public AccessTokenDao(Logger logger, DbService dbService) throws SQLException  {
        this.logger = logger;
        this.dbService = dbService;
    }

    public AccessToken create(UserAccess userAccess) {
        if (userAccess == null) return null;

        AccessToken activeToken = getActiveToken(userAccess.getUserAccessId());
        if (activeToken != null) {
            return prolong(activeToken) ? activeToken : null;
        }

        AccessToken token = new AccessToken();
        token.setAccessTokenId(UUID.randomUUID());
        token.setUserAccessId(userAccess.getUserAccessId());
        Date date = new Date();
        token.setIssuedAt(date);
        token.setExpiresAt(new Date(date.getTime() + 100 * 1000));

        String sql = "INSERT INTO access_tokens(access_token_id, user_access_id, issued_at, expires_at) VALUES(?,?,?,?)";
        try (PreparedStatement prep = dbService.getConnection().prepareStatement(sql)) {
            prep.setString(1, token.getAccessTokenId().toString());
            prep.setString(2, token.getUserAccessId().toString());
            prep.setTimestamp(3, new Timestamp(token.getIssuedAt().getTime()));
            prep.setTimestamp(4, new Timestamp(token.getExpiresAt().getTime()));
            prep.executeUpdate();
            dbService.getConnection().commit();
        } catch (SQLException ex) {
            logger.log(Level.WARNING, "AccessTokenDao::create {0} sql: '{1}'", new Object[]{ex.getMessage(), sql});
            return null;
        }
        return token;
    }

    public UserAccess getUserAccess(String bearerCredentials )
    {
       UUID accessTokenId;
       try
       {
           accessTokenId = UUID.fromString(bearerCredentials);
       }
       catch (Exception ignore)
       {
           return null;
       }

       String sql = String.format(
               "SELECT * FROM access_tokens a "
                       + "JOIN user_access ua ON a.user_access_id = ua.user_access_id "
                       +  "WHERE a.access_token_id = '%s' "
               + "AND a.expires_at > CURRENT_TIMESTAMP",
               accessTokenId.toString());
       try(Statement statement = dbService.getConnection().createStatement())
       {
           ResultSet rs = statement.executeQuery(sql);
           if(rs.next())
           {
               return UserAccess.fromResultSet(rs);
           }
       }
       catch (SQLException ex)
       {
           logger.log(
                   Level.WARNING,
                   "AccessTokenDao::getUserAccess {0} sql: '{1}'",
                   new Object[] {ex.getMessage(), sql});
           return null;
       }
       return null;
    }

    public boolean cancel(AccessToken token)
    {
        return true;
    }

    public boolean prolong(AccessToken token)
    {
        if (token == null) return false;

        long additionalTime = 100 * 1000;
        Date newExpiresAt = new Date(System.currentTimeMillis() + additionalTime);

        String sql = "UPDATE access_tokens SET expires_at = ? WHERE access_token_id = ?";
        try (PreparedStatement prep = dbService.getConnection().prepareStatement(sql))
        {
            prep.setTimestamp(1, new Timestamp(newExpiresAt.getTime()));
            prep.setString(2, token.getAccessTokenId().toString());
            int affectedRows = prep.executeUpdate();
            dbService.getConnection().commit();
            return affectedRows > 0;
        } catch (SQLException ex)
        {
            logger.log(
                    Level.WARNING, "AccessTokenDao::prolong {0} sql: '{1}'",
                    new Object[]{ex.getMessage(), sql});
        }
        return false;
    }

    public boolean installTables()
    {
        String sql = "CREATE TABLE IF NOT EXISTS access_tokens("
                + "access_token_id  CHAR(36)     PRIMARY KEY DEFAULT( UUID() ),"
                + "user_access_id    CHAR(36) NOT NULL,"
                + "issued_at DATETIME NOT NULL,"
                + "expires_at DATETIME NULL"
                + ") Engine = InnoDB, DEFAULT CHARSET = utf8mb4";

        try(Statement statement = dbService.getConnection().createStatement())
        {
            statement.executeUpdate(sql);
            dbService.getConnection().commit();
            logger.info("AccessTokenDao::installTables OK");
            return true;
        }
        catch (SQLException ex)
        {
            logger.log(Level.WARNING, "AccessTokenDao::installTables {0} sql: '{1}'",
            new Object[] {ex.getMessage(), sql});
        }
        return  false;
    }

    private AccessToken getActiveToken(UUID userAccessId) {
        String sql = "SELECT * FROM access_tokens WHERE user_access_id = ? AND expires_at > CURRENT_TIMESTAMP LIMIT 1";

        try (PreparedStatement prep = dbService.getConnection().prepareStatement(sql))
        {
            prep.setString(1, userAccessId.toString());
            ResultSet rs = prep.executeQuery();
            if (rs.next())
            {
                return AccessToken.fromResultSet(rs);
            }
        } catch (SQLException ex)
        {
            logger.log(
                    Level.WARNING, "AccessTokenDao::getActiveToken {0} sql: '{1}'",
                    new Object[]{ex.getMessage(), sql});
        }
        return null;
    }


}
