package itstep.learning.services.db;

import java.sql.Connection;
import java.sql.SQLException;

public interface DbService {
    Connection getConnection() throws SQLException;
}
