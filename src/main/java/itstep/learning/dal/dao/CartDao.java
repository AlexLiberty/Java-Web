package itstep.learning.dal.dao;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import itstep.learning.dal.dto.Cart;
import itstep.learning.services.db.DbService;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

@Singleton
public class CartDao {
    private final DbService dbService;
    private final Logger logger;
    private final SimpleDateFormat sqlDateFormat = 
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Inject
    public CartDao( DbService dbService, Logger logger ) throws SQLException {
        this.dbService = dbService;
        this.logger    = logger;
    }
    
    public Cart getUserCart( UUID userAccessId, boolean createNew ) {
        // Якщо у користувача є незакритий кошик - повернути його
        // якщо немає - створити новий
        String sql = String.format( Locale.ROOT, 
                "SELECT * FROM carts c WHERE c.cart_closed_at IS NULL "
                + "AND c.user_access_id = '%s'", userAccessId.toString() );
        try( Statement statement = dbService.getConnection().createStatement() ) {
            ResultSet rs = statement.executeQuery( sql ) ;
            if( rs.next() ) {
                return Cart.fromResultSet( rs );
            }
            else if( createNew ) {
                Cart cart = new Cart() ;
                cart.setCartId( UUID.randomUUID() );
                cart.setUserAccessId( userAccessId );
                cart.setCartCreatedAt( new Date() );
                sql = String.format( Locale.ROOT, 
                    "INSERT INTO carts (cart_id, user_access_id,"
                    + " cart_created_at, cart_price) "
                    + "VALUES('%s', '%s', '%s', 0)", 
                    cart.getCartId().toString(),
                    userAccessId.toString(),
                    sqlDateFormat.format( cart.getCartCreatedAt() ) 
                );
                statement.executeUpdate( sql );
                dbService.getConnection().commit();
                return cart;
            }
        }
        catch( SQLException ex ) {
            logger.log( 
                    Level.WARNING, 
                    "CartDao::getUserCart {0} sql: {1}",
                    new Object[] { ex.getMessage(), sql } 
            );
        }
        return null;
    }
    
    public boolean installTables() {
        Future<Boolean> task1 = CompletableFuture
                .supplyAsync( this::installCarts ) ;        
        Future<Boolean> task2 = CompletableFuture
                .supplyAsync( this::intallCartItems ) ;
        try {
            boolean res1 = task1.get() ;   // await task1
            boolean res2 = task2.get() ;   // await task2
            try { dbService.getConnection().commit() ; } catch( SQLException ignore ) { }
            return res1 && res2;
        }
        catch( ExecutionException | InterruptedException ignore ) {
            return false;
        }
    }
    
    private boolean installCarts() {
        String sql = "CREATE TABLE IF NOT EXISTS carts("
                + "cart_id            CHAR(36)      PRIMARY KEY DEFAULT( UUID() ),"
                + "user_access_id     CHAR(36)      NOT NULL,"
                + "cart_is_cancelled  TINYINT           NULL,"
                + "cart_price         DECIMAL(14,2) NOT NULL,"
                + "cart_created_at    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,"
                + "cart_closed_at     DATETIME          NULL "
                + ") Engine = InnoDB, DEFAULT CHARSET = utf8mb4";
        try( Statement statement = dbService.getConnection().createStatement() ) {
            statement.executeUpdate( sql ) ;
            logger.info( "CartDao::installCarts OK" );
            return true;
        }
        catch( SQLException ex ) {
            logger.log( 
                    Level.WARNING, 
                    "CartDao::installCarts {0} sql: {1}",
                    new Object[] { ex.getMessage(), sql } 
            );
        }
        return false;    
    }
    
    private boolean intallCartItems() {
        String sql = "CREATE TABLE IF NOT EXISTS cart_items("
                + "cart_item_id    CHAR(36)      PRIMARY KEY DEFAULT( UUID() ),"
                + "cart_id         CHAR(36)      NOT NULL,"
                + "product_id      CHAR(36)      NOT NULL,"
                + "cart_item_price DECIMAL(12,2) NOT NULL,"
                + "quantity        SMALLINT      NOT NULL DEFAULT 1,"
                + "action_id       CHAR(36)          NULL"
                + ") Engine = InnoDB, DEFAULT CHARSET = utf8mb4";
        try( Statement statement = dbService.getConnection().createStatement() ) {
            statement.executeUpdate( sql ) ;
            logger.info( "CartDao::intallCartItems OK" );
            return true;
        }
        catch( SQLException ex ) {
            logger.log( 
                    Level.WARNING, 
                    "CartDao::intallCartItems {0} sql: {1}",
                    new Object[] { ex.getMessage(), sql } 
            );
        }
        return false;    
    }
}
