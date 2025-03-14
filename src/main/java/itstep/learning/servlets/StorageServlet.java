package itstep.learning.servlets;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import itstep.learning.services.storage.StorageService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;

@Singleton
public class StorageServlet extends HttpServlet {
    private final StorageService storageService;
    private static final Set<String> BLACKLISTED_EXTENSIONS = Set.of(
            ".exe", ".php", ".py", ".cgi", ".sh", ".bat", ".cmd", ".pl", ".jar"
    );

    @Inject
    public StorageServlet(StorageService storageService) {
        this.storageService = storageService;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String fileId = req.getPathInfo();

        if (fileId == null || fileId.length() <= 1 || !fileId.contains(".")) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("Invalid file ID");
            return;
        }

        fileId = fileId.substring(1);
        int dotPosition = fileId.lastIndexOf('.');
        String ext = fileId.substring(dotPosition);

        if (dotPosition == fileId.length() - 1 || BLACKLISTED_EXTENSIONS.contains(ext.toLowerCase())) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            resp.getWriter().write("Forbidden file type");
            return;
        }

        try (InputStream inputStream = storageService.get(fileId)) {
            resp.setContentType(mimeByExtensions(ext));
            OutputStream writer = resp.getOutputStream();
            byte[] buf = new byte[131072];
            int len;
            while ((len = inputStream.read(buf)) > 0) {
                writer.write(buf, 0, len);
            }
        } catch (IOException ex) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private String mimeByExtensions(String ext) {
        switch (ext) {
            case ".jpg": ext = ".jpeg";
            case ".jpeg":
            case ".gif":
            case ".png":
            case ".webp":
            case ".bmp": return "image/" + ext.substring(1);

            case ".txt": ext = ".plain";
            case ".css":
            case ".csv":
            case ".html": return "text/" + ext.substring(1);

            case ".js":
            case ".mjs": return "text/javascript";

            default: return "application/octet-stream";
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String fileId = req.getPathInfo();

        if (fileId == null || fileId.length() <= 1) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("Invalid file ID");
            return;
        }

        fileId = fileId.substring(1);
        boolean deleted = storageService.delete(fileId);

        if (deleted) {
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write("File deleted successfully");
        } else {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            resp.getWriter().write("File not found");
        }
    }
}


/*
http://localhost:8080/Java-Web-221/storage/123?x=10&y=20
req.getMethod()       GET
req.getRequestURI()   /Java-Web-221/storage/123
req.getContextPath()  /Java-Web-221
req.getServletPath()  /storage
req.getPathInfo()     /123
req.getQueryString()  x=10&y=20
req.getServerName() localhost
req.getScheme()  http
  */
