package itstep.learning.dal.dto;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.UUID;

public class Product
{
    private UUID productId;
    private UUID categoryId;
    private String productTitle;
    private String productSlug;
    private String productDescription;
    private String productImageId;
    private double price;
    private int stock;
    private Date deleteMoment;

    public UUID getProductId() {
        return productId;
    }

    public void setProductId(UUID productId) {
        this.productId = productId;
    }

    public UUID getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(UUID categoryId) {
        this.categoryId = categoryId;
    }

    public String getProductTitle() {
        return productTitle;
    }

    public void setProductTitle(String productTitle) {
        this.productTitle = productTitle;
    }

    public String getProductSlug() {
        return productSlug;
    }

    public void setProductSlug(String productSlug) {
        this.productSlug = productSlug;
    }

    public String getProductDescription() {
        return productDescription;
    }

    public void setProductDescription(String productDescription) {
        this.productDescription = productDescription;
    }

    public String getProductImageId() {
        return productImageId;
    }

    public void setProductImageId(String productImageId) {
        this.productImageId = productImageId;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public Date getDeleteMoment() {
        return deleteMoment;
    }

    public void setDeleteMoment(Date deleteMoment) {
        this.deleteMoment = deleteMoment;
    }

    public static Product fromResultSet(ResultSet rs) throws SQLException
    {
        Product product = new Product() ;
        product.setProductId( UUID.fromString( rs.getString( "product_id" ) ) );
        product.setCategoryId( UUID.fromString( rs.getString( "category_id" ) ) );
        product.setProductTitle( rs.getString( "product_title"  ) );
        product.setProductDescription( rs.getString( "product_description" ) );
        product.setProductSlug( rs.getString( "product_slug" ) );
        product.setProductImageId( rs.getString( "product_imageId" ) );
        product.setPrice( rs.getDouble( "product_price" ) );
        product.setStock( rs.getInt( "product_stock" ) );
        java.sql.Timestamp timestamp = rs.getTimestamp( "product_delete_moment" ) ;
        product.setDeleteMoment(
                timestamp == null ? null : new Date( timestamp.getTime() ) ) ;
        return product;
    }
}
