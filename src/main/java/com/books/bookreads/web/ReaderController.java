package com.books.bookreads.web;

import com.books.bookreads.config.JWTService;
import com.books.bookreads.model.Reader;
import com.books.bookreads.service.ReaderService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = {"http://localhost:8080"}, allowCredentials = "true", maxAge = 3600)
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reader")
public class ReaderController {
    private final ReaderService readerService;
    private final JWTService jwtService;

    @GetMapping("/profile")
    public ResponseEntity<Reader> getReaderProfileInfo (@RequestParam String email, HttpServletRequest request){
        String jwtToken = request.getHeader("Authorization");
        if (jwtToken != null && jwtToken.startsWith("Bearer ")) {
            jwtToken = jwtToken.substring(7);
            if (jwtService.isTokenValid(jwtToken) && jwtService.extractUsername(jwtToken).equals(email)) {
                Reader reader = readerService.findByEmail(email);
                return ResponseEntity.ok(reader);
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}
