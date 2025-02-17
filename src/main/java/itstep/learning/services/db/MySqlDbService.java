package itstep.learning.services.db;
import com.google.inject.Singleton;
import com.mysql.cj.jdbc.MysqlDataSource;
import java.sql.Connection;
import java.sql.SQLException;

@Singleton
public class MySqlDbService implements DbService
{
    private Connection connection;
    @Override
    public Connection getConnection() throws SQLException {
        if(connection == null)
        {
            //підключення до бази даних
            MysqlDataSource mds = new MysqlDataSource();
            mds.setURL("jdbc:mysql://localhost:3308/javaDb" + "?useUnicode=true&characterEncoding=UTF-8");
            connection = mds.getConnection( "user1", "pass123" );
        }

        return connection;
    }
}
