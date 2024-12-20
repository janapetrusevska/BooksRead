package com.books.bookreads.web;

import com.books.bookreads.model.dtos.BookDto;
import com.books.bookreads.model.dtos.BookDtoRequest;
import com.books.bookreads.model.dtos.UpdateStatusRequest;
import com.books.bookreads.model.enums.BookStatus;
import com.books.bookreads.model.enums.Genre;
import com.books.bookreads.service.BookService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/books")
public class BookController {

    private final BookService bookService;
    private final ObjectMapper objectMapper;

    @GetMapping
    public ResponseEntity<List<BookDto>> getAllBooks(@RequestHeader("Authorization") String token){
        String jwtToken = token.substring(7).trim();
        List<BookDto> books = bookService.findAllByReader(jwtToken);
        return ResponseEntity.ok(books);
    }

    @PostMapping("/byIds")
    public ResponseEntity<List<BookDto>> getAllBooksByIds(@RequestHeader("Authorization") String token,
                                                          @RequestBody List<Long> ids) {
        List<BookDto> allBooks = bookService.findByIds(ids);
        return ResponseEntity.ok(allBooks);
    }

    @GetMapping("/{bookId}")
    public ResponseEntity<BookDto> findBookById(@PathVariable Long bookId) {
        BookDto bookDto = bookService.findById(bookId);
        return ResponseEntity.ok(bookDto);
    }

    @PostMapping("/add")
    public ResponseEntity<Void> addBook(
            @RequestPart("bookDtoRequest") String bookJson,
            @RequestPart(value = "cover", required = false) MultipartFile cover,
            @RequestHeader("Authorization") String token) {

        String jwtToken = token.substring(7).trim();

        try {
            BookDtoRequest request = objectMapper.readValue(bookJson, BookDtoRequest.class);

            if (cover != null && !cover.isEmpty()) {
                String coverPath = bookService.saveCoverImage(cover);
                bookService.addBook(request, coverPath, jwtToken);
            } else {
                bookService.addBook(request, request.getCover(), jwtToken);
            }

            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/update/{bookId}")
    public ResponseEntity<BookDto> updateBook(@PathVariable Long bookId,
                                           @RequestPart("bookDtoRequest") String bookJson,
                                           @RequestPart(value = "cover", required = false) MultipartFile cover,
                                           @RequestHeader("Authorization") String token){
        String jwtToken = token.substring(7).trim();
        try {
            BookDtoRequest request = objectMapper.readValue(bookJson, BookDtoRequest.class);
            BookDto updatedBook;
            if(cover !=null && !cover.isEmpty()){
                String coverPath = bookService.saveCoverImage(cover);
                updatedBook = bookService.updateBook(bookId,request, coverPath,jwtToken);
            } else {
                updatedBook = bookService.updateBook(bookId,request, request.getCover(), jwtToken);
            }

            return ResponseEntity.ok(updatedBook);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @PutMapping("/updateStatus/{bookId}")
    public ResponseEntity<BookDto> updateBookStatus(@PathVariable Long bookId,
                                                    @RequestHeader("Authorization") String token,
                                                    @RequestBody UpdateStatusRequest statusRequest){
        try{
            String jwtToken = token.substring(7).trim();
            String newStatus = statusRequest.getStatus();
            BookDto updatedBookStatus = bookService.updateBookStatus(bookId,newStatus,jwtToken);
            return ResponseEntity.ok(updatedBookStatus);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @DeleteMapping("/delete/{bookId}")
    public ResponseEntity<Void> deleteBook(@PathVariable Long bookId, @RequestHeader("Authorization") String token){
        String jwtToken = token.substring(7).trim();
        bookService.deleteBook(bookId,jwtToken);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/bestRated")
    public ResponseEntity<List<BookDto>> getBestRatedBooks(){
        List<BookDto> bestRatedBooks = bookService.getBestRated();
        return ResponseEntity.ok(bestRatedBooks);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<BookDto>> getBooksByStatus(@PathVariable BookStatus status,
                                                          @RequestHeader("Authorization") String token){
        String jwtToken = token.substring(7).trim();
        List<BookDto> wishlist = bookService.getBooksByStatusAndReader(status,jwtToken);
        return ResponseEntity.ok(wishlist);
    }

    @GetMapping("/status/{status}/{readerId}")
    public ResponseEntity<List<BookDto>> getBooksByReaderByStatus(@PathVariable BookStatus status,
                                                          @PathVariable Long readerId,
                                                          @RequestHeader("Authorization") String token){
        String jwtToken = token.substring(7).trim();
        List<BookDto> wishlist = bookService.getBooksByStatusAndReader(status,readerId,jwtToken);
        return ResponseEntity.ok(wishlist);
    }

    @GetMapping("/genres")
    public ResponseEntity<List<Genre>> getAllBookGenres(){
        List<Genre> genres = Arrays.asList(Genre.values());
        return ResponseEntity.ok(genres);
    }
}
