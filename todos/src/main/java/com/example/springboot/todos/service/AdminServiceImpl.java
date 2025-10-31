package com.example.springboot.todos.service;

import com.example.springboot.todos.entity.Authority;
import com.example.springboot.todos.entity.User;
import com.example.springboot.todos.repository.UserRepository;
import com.example.springboot.todos.response.UserResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

@Service
public class AdminServiceImpl implements AdminService {
    private final UserRepository userRepository;

    public AdminServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        //Ve bunun yaptığı şey, yinelenebilir sonucu bir akışa dönüştürmektir.
        // Yani bir yineleyici sonucu olacak.
        // Ve bir akışa geri dönüyoruz.
        //Buradaki false parametresi, akışın paralel değil sıralı olacağı anlamına gelir.
        return StreamSupport.stream(userRepository.findAll().spliterator(), false)
                .map(this::convertToUserResponse).toList();//her user entity'i userResponse cast eder
    }

    @Override
    @Transactional
    public UserResponse promoteToAdmin(long userId) {

        Optional<User> user = userRepository.findById(userId);
        //Kullanıcının var olduğundan emin oluyoruz ve bu kullanıcının zaten yönetici olmadığından emin olmak için kontrol ediyoruz
        if (user.isEmpty() || user.get().getAuthorities().stream().anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()))) {
            //Eğer öyleyse, sadece kötü bir istek atıyoruz ve kullanıcının mevcut olmadığını veya zaten bir yönetici olduğunu söylüyoruz.
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User not exist or already an admin");
        }
        //Kullanıcı mevcutsa ve yönetici değilse, o zaman bu kullanıcıya çalışan rolü ve yönetici rolü için yeni roller ve yetkiler atıyoruz.
        List<Authority> authorities = new ArrayList<>();
        authorities.add(new Authority("ROLE_EMPLOYEE"));
        authorities.add(new Authority("ROLE_ADMIN"));
        user.get().setAuthorities(authorities);
        User savedUser = userRepository.save(user.get());
        return convertToUserResponse(savedUser);

    }

    @Override
    @Transactional
    public void deleteNonAdmin(long userId) {
        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty() || user.get().getAuthorities().stream().anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User not exist or already an admin you can't delete admin");
        }
        userRepository.delete(user.get());
    }

    private UserResponse convertToUserResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getFirstName() + " " + user.getLastName(),
                user.getEmail(),
                user.getAuthorities().stream().map(auth -> (Authority) auth).toList()
        );
    }

}
