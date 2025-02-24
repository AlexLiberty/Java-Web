package itstep.learning.models;

import itstep.learning.dal.dto.AccessToken;
import itstep.learning.dal.dto.User;
import itstep.learning.dal.dto.UserAccess;

public class UserAuthModel
{
    private User user;
    private UserAccess userAccess;
    private AccessToken accessToken;

    public UserAuthModel(User user,UserAccess userAccess, AccessToken accessToken) {
        this.user = user;
        this.userAccess = userAccess;
        this.accessToken = accessToken;
    }

    public UserAccess getUserAccess() {
        return userAccess;
    }

    public void setUserAccess(UserAccess userAccess) {
        this.userAccess = userAccess;
    }

    public UserAuthModel() {

    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public AccessToken getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(AccessToken accessToken) {
        this.accessToken = accessToken;
    }
}
