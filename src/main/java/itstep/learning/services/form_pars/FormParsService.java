package itstep.learning.services.form_pars;

import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;

public interface FormParsService
{
    FormParseResult parseRequest(HttpServletRequest req) throws IOException;
}

// Завантаження файлів, розбір даних форм.
// При передачі файлів, форми використовують спеціальний тип запитів - multipart
// При прийомі фалів необхідно забезпечити:
// а) їх тимчасове зберігання.
// б) та постійне зберігання.
