package itstep.learning.servlets;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import itstep.learning.dal.dao.DataContext;
import itstep.learning.dal.dto.Category;
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
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Singleton
public class ProductServlet extends HttpServlet {
    private final FormParsService formParsService;
    private final StorageService storageService;
    private final RestService restService;
    private final DataContext dataContext;

    @Inject
    public ProductServlet(FormParsService formParsService, StorageService storageService, RestService restService, DataContext dataContext) {
        this.formParsService = formParsService;
        this.storageService = storageService;
        this.restService = restService;
        this.dataContext = dataContext;
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        FormParseResult formParseResult = formParsService.parseRequest(req);
        RestResponse restResponse = new RestResponse()
                .setResourceUrl("POST /product")
                .setMeta(Map.of(
                        "dataType", "object",
                        "read", "GET /product",
                        "update", "PUT /product",
                        "delete", "DELETE /product"
                ));
        Product product = new Product();
        String str;

        str = formParseResult.getFields().get("product-title");
        if (str == null || str.isBlank()) {
            restService.SendResponse(resp, restResponse
                    .setStatus(400)
                    .setDate("Missing or empty 'product-title'"));
            return;
        }
        product.setProductTitle(str);

        str = formParseResult.getFields().get("product-description");
        if (str == null || str.isBlank()) {
            restService.SendResponse(resp, restResponse
                    .setStatus(400)
                    .setDate("Missing or empty 'product-description'"));
            return;
        }
        product.setProductDescription(str);

        str = formParseResult.getFields().get("product-code");
        product.setProductSlug(str);

        str = formParseResult.getFields().get("product-price");
        try {
            product.setPrice(Double.parseDouble(str));
        } catch (NumberFormatException | NullPointerException ex) {
            restService.SendResponse(resp, restResponse
                    .setStatus(400)
                    .setDate("Data parse error 'product-price' " + ex.getMessage()));
            return;
        }

        str = formParseResult.getFields().get("product-stock");
        try {
            product.setStock(Integer.parseInt(str));
        } catch (NumberFormatException | NullPointerException ex) {
            restService.SendResponse(resp, restResponse
                    .setStatus(400)
                    .setDate("Data parse error 'product-stock' " + ex.getMessage()));
            return;
        }

        str = formParseResult.getFields().get("category-id");
        try {
            product.setCategoryId(UUID.fromString(str));
        } catch (IllegalArgumentException | NullPointerException ex) {
            restService.SendResponse(resp, restResponse
                    .setStatus(400)
                    .setDate("Data parse error 'category-id' " + ex.getMessage()));
            return;
        }

        FileItem image = formParseResult.getFiles().get("product-image");
        if (image.getSize() > 0) {
            int dotPosition = image.getName().lastIndexOf('.');
            String ext = image.getName().substring(dotPosition);
            str = storageService.put(image.getInputStream(), ext);
        } else {
            str = null;
        }
        product.setProductImageId(str);

        product = dataContext.getProductDao().addNewProduct(product);

        if (product == null) {
            // Додавання у БД не відбулось, видалити файл зі сховища
            restService.SendResponse(resp, restResponse
                    .setStatus(500)
                    .setDate("Internal error. See logs!"));
            return;
        }

        restService.SendResponse(resp, restResponse
                .setStatus(200).setDate(product));
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String type = req.getParameter("type");
        if ("categories".equals(type)) {
            // .../product?type=categories
            getCategories(req, resp);
        } else if ("category".equals(type)) {
            // .../product?type=category&id=12312
            getCategory(req, resp);
        } else {
            // Якщо тип не заданий або інший тип, отримуємо всі товари
            getProducts(req, resp);
        }
    }

    private void getCategories(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String imgPath = String.format(Locale.ROOT,
                "%s://%s:%d%s/storage/",
                req.getScheme(),
                req.getServerName(),
                req.getServerPort(),
                req.getContextPath());
        List<Category> categories = dataContext.getCategoryDao().getList();
        for (Category c : categories) {
            c.setCategoryImageId(imgPath + c.getCategoryImageId());
        }

        restService.SendResponse(resp,
                new RestResponse()
                        .setResourceUrl("GET /product?type=categories")
                        .setMeta(Map.of(
                                "dataType", "array"
                        ))
                        .setStatus(200)
                        .setCacheTime(86400)
                        .setDate(categories));
    }

    private void getCategory(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String slug = req.getParameter("slug");
        RestResponse restResponse = new RestResponse()
                .setResourceUrl("GET /product?type=category&slug=" + slug)
                .setMeta(Map.of(
                        "dataType", "object"
                ))
                .setCacheTime(86400);

        Category category;
        try {
            category = dataContext.getCategoryDao().getCategoryBySlug(slug);
        } catch (RuntimeException ignore) {
            restService.SendResponse(resp, restResponse
                    .setStatus(500)
                    .setDate("Take a look to the Logs"));
            return;
        }
        if (category == null) {
            restService.SendResponse(resp, restResponse
                    .setStatus(404)
                    .setDate("Category not found"));
            return;
        }

        List<Product> products = dataContext.getProductDao().getProductsByCategory(category.getCategoryId());
        category.setProducts(products);

        restService.SendResponse(resp, restResponse
                .setStatus(200)
                .setDate(category));
    }

    private void getProducts(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        List<Product> products = dataContext.getProductDao().getAllProducts();

        restService.SendResponse(resp, new RestResponse()
                .setResourceUrl("GET /product")
                .setMeta(Map.of("dataType", "array"))
                .setStatus(200)
                .setCacheTime(86400)
                .setDate(products));
    }
}
