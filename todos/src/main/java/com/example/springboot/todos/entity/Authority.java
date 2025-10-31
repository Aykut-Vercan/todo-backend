package com.example.springboot.todos.entity;


import jakarta.persistence.Embeddable;
import org.springframework.security.core.GrantedAuthority;


/*
* Embeddable
    Kendi tablosu YOK - başka entity'nin tablosuna gömülür
    Primary KeyYOK
    Lifecycle YOK - sahibi entity ile birlikte oluşur/silinir
    Identity YOK - değerine göre karşılaştırılır
    Basit değer nesnesi - sadece veri tutar
* */
@Embeddable
public class Authority implements GrantedAuthority {

    private String authority;
    public Authority( ){
    }

    public Authority( String authority){
        this.authority=authority;
    }

    @Override
    public String getAuthority() {

        return authority;
    }
}
