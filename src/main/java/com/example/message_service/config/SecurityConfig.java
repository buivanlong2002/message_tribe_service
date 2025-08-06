package com.example.message_service.config;


import com.example.message_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    @Autowired
    private final UserRepository userRepository;

    /**
     * Cấu hình UserDetailsService để tìm user bằng số điện thoại.
     * Khi Spring Security cần xác thực user, nó sẽ gọi service này.
     * Nếu không tìm thấy user, ném ra ngoại lệ UsernameNotFoundException.
     */
    @Bean
    public UserDetailsService userDetailsService() {
        return email -> (org.springframework.security.core.userdetails.UserDetails) userRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found" + email));
    }

    /**
     * Mã hóa mật khẩu người dùng bằng BCryptPasswordEncoder.
     * Đây là phương pháp bảo mật mạnh mẽ giúp bảo vệ mật khẩu.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Cấu hình AuthenticationProvider sử dụng DaoAuthenticationProvider.
     * - Lấy thông tin user từ UserDetailsService.
     * - Kiểm tra mật khẩu bằng PasswordEncoder.
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
        daoAuthenticationProvider.setUserDetailsService(userDetailsService());
        daoAuthenticationProvider.setPasswordEncoder(passwordEncoder());
        return daoAuthenticationProvider;
    }

    /**
     * Cấu hình AuthenticationManager để quản lý xác thực.
     * - Lấy từ AuthenticationConfiguration mặc định của Spring Security.
     */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}

