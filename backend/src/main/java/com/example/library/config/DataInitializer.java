package com.example.library.config;

import com.example.library.model.Book;
import com.example.library.model.Member;
import com.example.library.repository.BookRepository;
import com.example.library.repository.MemberRepository;
import com.example.library.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializer {
    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    @Bean
    CommandLineRunner seedData(BookRepository bookRepository, MemberRepository memberRepository, AuthService authService) {
        return args -> {
            if (bookRepository.count() == 0) {
                bookRepository.save(newBook("Domain-Driven Design", "Eric Evans", 3));
                bookRepository.save(newBook("Clean Code", "Robert C. Martin", 2));
                bookRepository.save(newBook("The Pragmatic Programmer", "Andrew Hunt", 1));
                log.info("Seeded default books");
            }

            if (memberRepository.findByUsername("analyst").isEmpty()) {
                Member member = new Member();
                member.setUsername("analyst");
                member.setRole("ADMIN");
                member.setPasswordHash(authService.encodePassword("password"));
                memberRepository.save(member);
                log.info("Seeded default member 'analyst' with password 'password'");
            }
        };
    }

    private Book newBook(String title, String author, int copies) {
        Book book = new Book();
        book.setTitle(title);
        book.setAuthor(author);
        book.setAvailableCopies(copies);
        return book;
    }
}
