package com.example.springboot.todos.util;

import com.example.springboot.todos.entity.User;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

//bir kaç yerde kullanıyorduk kod tekrarı oluyordu buraya utilde refactor edip fonksiyon yazdık

@Component
public class FindAuthenticatedUserImpl implements FindAuthenticatedUser {
//Artık uygulamamızın içinde yeni fonksiyonlar veya yeni sınıflar oluşturduğumuzda
// ve bir kullanıcı getirmemiz gerektiğinde,direkt asagıdaki fonksiyonu cagırıcaz

    @Override
    public User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() ||
                authentication.getPrincipal().equals("anonymousUser")) {
            throw new AccessDeniedException("Authentication required");
        }
       return (User) authentication.getPrincipal();//herhangi bir object dönebilir user'a Type Casting yapıyoruz
    }
}
