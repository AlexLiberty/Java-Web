package itstep.learning.dal.dto;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.UUID;

public class AccessToken
{
    private UUID accessTokenId;
    private UUID userAccessId;
    private Date issuedAt;
    private Date expiresAt;

    public static AccessToken fromResultSet(ResultSet rs) throws SQLException {
        AccessToken token = new AccessToken();
        token.setAccessTokenId(UUID.fromString(rs.getString("access_token_id")));
        token.setUserAccessId(UUID.fromString(rs.getString("user_access_id")));

        Timestamp timestamp;
        timestamp = rs.getTimestamp("issued_at");

        if(timestamp != null)
        {
            token.setIssuedAt(new Date(timestamp.getTime()));
        }

        timestamp = rs.getTimestamp("expires_at");

        if(timestamp != null)
        {
            token.setIssuedAt(new Date(timestamp.getTime()));
        }

        return token;
    }

    public UUID getAccessTokenId() {
        return accessTokenId;
    }

    public void setAccessTokenId(UUID accessTokenId) {
        this.accessTokenId = accessTokenId;
    }

    public UUID getUserAccessId() {
        return userAccessId;
    }

    public void setUserAccessId(UUID userAccessId) {
        this.userAccessId = userAccessId;
    }

    public Date getIssuedAt() {
        return issuedAt;
    }

    public void setIssuedAt(Date issuedAt) {
        this.issuedAt = issuedAt;
    }

    public Date getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Date expiresAt) {
        this.expiresAt = expiresAt;
    }
}
