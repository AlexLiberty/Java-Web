package itstep.learning.dal.dto;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.UUID;

public class Cart {    
    private UUID   cartId;
    private UUID   userAccessId;
    private byte   isCartCancelled;
    private double cartPrice;
    private Date   cartCreatedAt;
    private Date   cartClosedAt;
    
    public static Cart fromResultSet( ResultSet rs ) throws SQLException {
        Cart cart = new Cart() ;
        cart.setCartId( UUID.fromString( rs.getString( "cart_id" ) ) );
        cart.setUserAccessId( UUID.fromString( rs.getString( "product_id" ) ) );
        java.sql.Timestamp timestamp = rs.getTimestamp( "cart_created_at" ) ;        
        cart.setCartCreatedAt( new Date( timestamp.getTime() ) ) ;
        timestamp = rs.getTimestamp( "cart_closed_at" ) ;
        cart.setCartClosedAt(
                timestamp == null ? null : new Date( timestamp.getTime() ) ) ;
        cart.setCartPrice( rs.getDouble( "cart_price" ) );
        cart.setIsCartCancelled( rs.getByte( "cart_is_cancelled" ) );
        return cart;
    }

    public UUID getCartId() {
        return cartId;
    }

    public void setCartId(UUID cartId) {
        this.cartId = cartId;
    }

    public UUID getUserAccessId() {
        return userAccessId;
    }

    public void setUserAccessId(UUID userAccessId) {
        this.userAccessId = userAccessId;
    }

    public byte getIsCartCancelled() {
        return isCartCancelled;
    }

    public void setIsCartCancelled(byte isCartCancelled) {
        this.isCartCancelled = isCartCancelled;
    }

    public double getCartPrice() {
        return cartPrice;
    }

    public void setCartPrice(double cartPrice) {
        this.cartPrice = cartPrice;
    }

    public Date getCartCreatedAt() {
        return cartCreatedAt;
    }

    public void setCartCreatedAt(Date cartCreatedAt) {
        this.cartCreatedAt = cartCreatedAt;
    }

    public Date getCartClosedAt() {
        return cartClosedAt;
    }

    public void setCartClosedAt(Date cartClosedAt) {
        this.cartClosedAt = cartClosedAt;
    }
    
    
}
/*
"CREATE TABLE IF NOT EXISTS carts("
+ "cart_id            CHAR(36)      PRIMARY KEY DEFAULT( UUID() ),"
+ "user_access_id     CHAR(36)      NOT NULL,"
+ "cart_is_cancelled  TINYINT           NULL,"
+ "cart_price         DECIMAL(14,2) NOT NULL,"
+ "cart_created_at    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,"
+ "cart_closed_at     DATETIME          NULL "
+ ") Engine = InnoDB, DEFAULT CHARSET = utf8mb4";
*/