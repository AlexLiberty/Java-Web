package itstep.learning.dal.dto;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.UUID;


public class CartItem {
    private UUID   cartItemId;
    private UUID   cartId;
    private UUID   productId;
    private UUID   actionId;
    private double cartItemPrice;
    private short  quantity;
    
    
    public static CartItem fromResultSet( ResultSet rs ) throws SQLException {
        CartItem cartItem = new CartItem() ;
        cartItem.setCartItemId( UUID.fromString( rs.getString( "cart_item_id" ) ) );
        cartItem.setCartId( UUID.fromString( rs.getString( "cart_id" ) ) );
        cartItem.setProductId( UUID.fromString( rs.getString( "product_id" ) ) );
        String actionId = rs.getString( "action_id" ) ;
        if( actionId != null ) {
            cartItem.setActionId( UUID.fromString( actionId ) );
        }
        cartItem.setCartItemPrice( rs.getDouble( "cart_item_price" ));
        cartItem.setQuantity( rs.getShort( "quantity" ) );
        return cartItem;
    }

    public UUID getCartItemId() {
        return cartItemId;
    }

    public void setCartItemId(UUID cartItemId) {
        this.cartItemId = cartItemId;
    }

    public UUID getCartId() {
        return cartId;
    }

    public void setCartId(UUID cartId) {
        this.cartId = cartId;
    }

    public UUID getProductId() {
        return productId;
    }

    public void setProductId(UUID productId) {
        this.productId = productId;
    }

    public UUID getActionId() {
        return actionId;
    }

    public void setActionId(UUID actionId) {
        this.actionId = actionId;
    }

    public double getCartItemPrice() {
        return cartItemPrice;
    }

    public void setCartItemPrice(double cartItemPrice) {
        this.cartItemPrice = cartItemPrice;
    }

    public short getQuantity() {
        return quantity;
    }

    public void setQuantity(short quantity) {
        this.quantity = quantity;
    }
    
}
/*
"CREATE TABLE IF NOT EXISTS cart_items("
+ "cart_item_id    CHAR(36)      PRIMARY KEY DEFAULT( UUID() ),"
+ "cart_id         CHAR(36)      NOT NULL,"
+ "product_id      CHAR(36)      NOT NULL,"
+ "cart_item_price DECIMAL(12,2) NOT NULL,"
+ "quantity        SMALLINT      NOT NULL DEFAULT 1,"
+ "action_id       CHAR(36)          NULL"
+ ") Engine = InnoDB, DEFAULT CHARSET = utf8mb4";
*/