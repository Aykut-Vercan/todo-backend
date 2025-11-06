package com.example.springboot.todos.config;

import com.example.springboot.todos.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Lazy;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtService jwtService, @Lazy UserDetailsService userDetailsService) {
        //lazy gerçekten ihtiyac duyulana kadar beanveya injection oluşturmayı geçiktirir
        //burada proxy olusturuyor yalnızca ilk kullanıldıgında oluşuyor
        //Böylece hepsini bir kerede oluşturmak yerine yalnızca kullanıldığında oluşturur.
        //amaç esasen bir hizmete ihtiyaç duyulana kadar yaratılmasına gerek olmayan bir bellek verimliliği
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        //Gelen bir talep var, bilirsiniz, JWT'yi talepten çıkarabiliriz.
        //Başarılı bir yanıt döndürmemiz gereken bir yanıt var.
        //Ve tüm kimlik doğrulama sürecinin bir sonraki adımı olan filtre zinciri vardır.
        //ana filtre zinciri ilk olarak bir JWT olup olmadığını doğrulamaktır.
        //sonra gelen her talep için yaptığımız bir sonraki filtreye itmemiz gerekiyor.

        //istediğimiz şey gelen istekten başlığı çekmektir
        final String authHeader = request.getHeader("Authorization");
        //System.out.println("Auth Header: " + authHeader);
        final String jwt;
        final String userEmail;

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        jwt = authHeader.substring(7);
        userEmail = jwtService.extractUsername(jwt);

        //JWT'den geçerli bir e-posta çıkarılmışsa ve SecurityContextHolder'da şu anda herhangi bir kimlik doğrulama ayarlanmamışsa devam

        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            //JWT'deki e-postayı kullanarak veritabanı kullanıcı deposundan kullanıcının ayrıntılarını yüklemektir
            //bunun nedeni e-postayı JWT olarak kullanıyor olmamızdır.

            UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);
            if (jwtService.isTokenValid(jwt, userDetails)) {//İlettiğimiz JWT belirtecinin başarılı olduğunu doğruluyoruz.
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));//Ve sonra spring security tarafından kullanılabilecek bir kullanıcı oluşturuyoruz.
                //kimlik doğrulama ve spring güvenlik bağlamını ayarlama.
                SecurityContextHolder.getContext().setAuthentication(authToken);//Ve burada kullanıcıyı spring security olarak ayarlıyoruz.
            }
        }
        //filterChain'in devam etmesini ve sistem içinde aldığımız request ve response göre bir sonraki filtreyi yapmasını istiyoruz.
        filterChain.doFilter(request,response);

    }
}


