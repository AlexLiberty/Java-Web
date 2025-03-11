package itstep.learning.servlets;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import itstep.learning.dal.dao.DataContext;
import itstep.learning.dal.dto.Product;
import itstep.learning.rest.RestResponse;
import itstep.learning.rest.RestService;
import itstep.learning.services.form_pars.FormParsService;
import itstep.learning.services.form_pars.FormParseResult;
import itstep.learning.services.storage.StorageService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.fileupload2.core.FileItem;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Singleton
public class ProductServlet extends HttpServlet
{
    private final FormParsService formParsService;
    private final StorageService storageService;
    private final RestService restService;
    private final DataContext  dataContext;

    @Inject
    public ProductServlet(FormParsService formParsService, StorageService storageService, RestService restService, DataContext dataContext)
    {
        this.formParsService = formParsService;
        this.storageService = storageService;
        this.restService = restService;
        this.dataContext = dataContext;
    }

    @Override
    protected void doPost( HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        FormParseResult formParseResult = formParsService.parseRequest(req);
        RestResponse restResponse = new RestResponse()
                .setResourceUrl( "POST /product" )
                .setMeta( Map.of(
                        "dataType", "object",
                        "read", "GET /product",
                        "update", "PUT /product",
                        "delete", "DELETE /product"
                ) );
        Product product = new Product();
        String str;

        str = formParseResult.getFields().get( "product-title" );
        if( str == null || str.isBlank() ) {
            restService.SendResponse( resp, restResponse
                    .setStatus( 400 )
                    .setDate( "Missing or empty 'product-title'" ) );
            return;
        }
        product.setProductTitle( str );

        str = formParseResult.getFields().get( "product-description" );
        if( str == null || str.isBlank() ) {
            restService.SendResponse( resp, restResponse
                    .setStatus( 400 )
                    .setDate( "Missing or empty 'product-description'" ) );
            return;
        }
        product.setProductDescription( str );

        str = formParseResult.getFields().get( "product-code" );
        product.setProductSlug( str );

        str = formParseResult.getFields().get( "product-price" );
        try { product.setPrice( Double.parseDouble( str ) ); }
        catch( NumberFormatException | NullPointerException ex ) {
            restService.SendResponse( resp, restResponse
                    .setStatus( 400 )
                    .setDate( "Data parse error 'product-price' " + ex.getMessage() ) );
            return;
        }

        str = formParseResult.getFields().get( "product-stock" );
        try { product.setStock( Integer.parseInt( str ) ); }
        catch( NumberFormatException | NullPointerException ex ) {
            restService.SendResponse( resp, restResponse
                    .setStatus( 400 )
                    .setDate( "Data parse error 'product-stock' " + ex.getMessage() ) );
            return;
        }

        str = formParseResult.getFields().get( "category-id" );
        try { product.setCategoryId( UUID.fromString( str ) ); }
        catch( IllegalArgumentException | NullPointerException ex ) {
            restService.SendResponse( resp, restResponse
                    .setStatus( 400 )
                    .setDate( "Data parse error 'category-id' " + ex.getMessage() ) );
            return;
        }

        FileItem image = formParseResult.getFiles().get( "product-image" );
        if( image.getSize() > 0 ) {
            int dotPosition = image.getName().lastIndexOf( '.' );
            String ext = image.getName().substring( dotPosition ) ;
            str = storageService.put( image.getInputStream(), ext ) ;
        }
        else {
            str = null;
        }
        product.setProductImageId( str );

        product = dataContext.getProductDao().addNewProduct(product);

        if(product == null)
        {
            //Додавання у БД не відбулось, видалити файл зі сховища
            restService.SendResponse( resp, restResponse
                    .setStatus( 500 )
                    .setDate( "Internal error. See logs!" ) );
            return;
        }

        restService.SendResponse( resp, restResponse
                .setStatus( 200 ).setDate( product ) );
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String type = req.getParameter("type");
        if("categories".equals(type))
        {//.../product?type=categories
            getCategories(req, resp);
        }
        else
        {//.../product?type=category$id=12312
            getCategory(req, resp);
        }
    }

    private void getCategories(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        restService.SendResponse(resp,
                new RestResponse()
                        .setResourceUrl("GET /product?type=categories")
                        .setMeta(Map.of(
                                "dataType", "array"
                        ))
                        .setStatus(200)
                        .setCacheTime(86400)
                        .setDate(dataContext.getCategoryDao().getList()));
    }

    private void getCategory(HttpServletRequest req, HttpServletResponse resp) throws ServletException
    {

    }

    private void getProducts(HttpServletRequest req, HttpServletResponse resp) throws ServletException
    {

    }
}
