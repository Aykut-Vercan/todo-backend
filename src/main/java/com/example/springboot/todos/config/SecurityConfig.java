package com.example.springboot.todos.config;

import com.example.springboot.todos.repository.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration //bir class'in bir veya daha fazla bean yöntemi bildirdiğini gösteren temel bir spring ek açıklamasıdır.
@EnableWebSecurity
public class SecurityConfig {
    private final UserRepository userRepository;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(UserRepository userRepository, JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.userRepository = userRepository;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    UserDetailsService userDetailsService() {
        return username -> userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }


    @Bean
    public PasswordEncoder passwordEncoder() {
        //spring boot uygulamamıza bir hash olan Bcrypt şifre kodlayıcısını kullanmasını söyler
        return new BCryptPasswordEncoder();
    }

    //istediğimiz şey bir kimlik doğrulama yöneticisi eklemektir, böylece config'in kimlik doğrulama yapılandırmasını aktardıgımız
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        //AuthenticationManager Spring security temel bir arayüzdür.
        //Kimlik doğrulama isteklerini işler.
        //Kullanıcı kimlik bilgilerini doğrular ve kimlik bilgileri geçerliyse kimliği doğrulanmış bir oturum oluşturur.
        //JWT tabanlı kimlik doğrulama kullanacağımız için, kimlik doğrulama yöneticisi oturum açma sırasında kullanıcı kimlik bilgilerini doğrulayacaktır.
        return config.getAuthenticationManager();
    }


    // AuthenticationEntryPoint bir kullanıcının kimliğini doğrulamaya çalıştığımızda reddedilirse ve bir exception atmamız gerekirse, atılacak exception buolur
    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return (request, response, authException) -> {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType("application/json");
            response.setHeader("WWW-Authenticate", "");
            response.getWriter().write("{\"error\": \"Unauthorized access\"}");
        };
    }

    //son şey güvenlik yapılandırma filtre zincirimizi eklemek.
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(configurer ->
                configurer
                        .requestMatchers("/api/auth/**", "/swagger-ui/**", "/v3/api-docs/**",
                                "/swagger-resources/**", "/webjars/**", "/docs").permitAll()
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated());
        //yukarıda ROLE_ADMIN yazmama sebebimiz spring ekliyor

        http.cors(cors -> cors.configurationSource(request -> {
            var corsConfig = new org.springframework.web.cors.CorsConfiguration();
            corsConfig.addAllowedOrigin("http://localhost:5173");
            corsConfig.addAllowedOrigin("https://todo-backend-production-ba85.up.railway.app");
            corsConfig.addAllowedOrigin("https://todo-frontend-topaz-iota.vercel.app");
            corsConfig.addAllowedOrigin("https://todo-frontend-ij87imuxp-aykuts-projects-39460c32.vercel.app");
            corsConfig.addAllowedMethod("*");
            corsConfig.addAllowedHeader("*");
            corsConfig.setAllowCredentials(true);
            return corsConfig;
        }));

        http.csrf(csrf -> csrf.disable());

        http.exceptionHandling(exceptionHandling ->
                exceptionHandling
                        .authenticationEntryPoint(authenticationEntryPoint()));

        http.sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));//her bir isteğin herhangi bir tür çerez veya başka bir şey depolamayacağıdır.

        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);//ilk olarak JWT kimlik doğrulama filtremizi yap

        return http.build();
    }
}
