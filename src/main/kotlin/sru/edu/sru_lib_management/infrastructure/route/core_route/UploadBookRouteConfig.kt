package sru.edu.sru_lib_management.infrastructure.route.core_route

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.coRouter
import sru.edu.sru_lib_management.core.handler.BookHandler

@Configuration
class UploadBookRouteConfig {
    @Bean
    fun uploadRoute(bookHandler: BookHandler): RouterFunction<ServerResponse> = coRouter {
        (accept(MediaType.MULTIPART_FORM_DATA) and "/api/v1/upload").nest {
            /**
             * Uploads books from an Excel file (.xlsx).
             *
             * Content-Type: multipart/form-data
             * Form field name: "book_file"
             *
             * Accepts Excel file (.xlsx) with the following columns:
             * - Column 0: bookId (String)
             * - Column 1: bookTitle (String)
             * - Column 2: bookQuan (Int) - Book quantity
             * - Column 3: languageId (String)
             * - Column 4: collegeId (String)
             * - Column 5: author (String, optional)
             * - Column 6: publicationYear (Int, optional)
             * - Column 7: genre (String)
             * - Column 8: receiveDate (LocalDate, optional)
             *
             * Process flow:
             * 1. Receives multipart file upload
             * 2. Creates a temporary file for processing
             * 3. Parses Excel using [BookHandler.parseExcelFile]
             * 4. Converts rows to a list of [sru.edu.sru_lib_management.core.domain.dto.BookDto]
             * 5. Saves books via [sru.edu.sru_lib_management.core.domain.service.BookService.saveBook]
             * 6. Deletes the temporary file
             *
             * Returns list of saved [sru.edu.sru_lib_management.core.domain.model.Books] on success.
             * Returns error message on failure (missing file, invalid format, save error).
             *
             * Requires the ADMIN or SUPER_ADMIN role.
             *
             * @see BookHandler.uploadBook
             * @see BookHandler.parseExcelFile
             * @see sru.edu.sru_lib_management.core.domain.service.BookService.saveBook
             * @see sru.edu.sru_lib_management.core.domain.dto.BookDto
             * @see sru.edu.sru_lib_management.core.domain.model.Books
             * @see sru.edu.sru_lib_management.common.CoreResult
             */
            POST("/book", bookHandler::uploadBook)
        }
    }
}