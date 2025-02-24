package itstep.learning.dal.dto;

import java.util.Date;
import java.util.UUID;

public class UserAccess
{
    private UUID userAccessId;
    private UUID userId;
    private String login;
    private String salt;
    private String dk;
    private String roleId;
    private Date deleteMoment;

    public Date getDeleteMoment() {
        return deleteMoment;
    }

    public void setDeleteMoment(Date deleteMoment) {
        this.deleteMoment = deleteMoment;
    }

    public String getRoleId() {
        return roleId;
    }

    public void setRoleId(String roleId) {
        this.roleId = roleId;
    }

    public String getDk() {
        return dk;
    }

    public void setDk(String dk) {
        this.dk = dk;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public UUID getUserAccessId() {
        return userAccessId;
    }

    public void setUserAccessId(UUID userAccessId) {
        this.userAccessId = userAccessId;
    }
}
