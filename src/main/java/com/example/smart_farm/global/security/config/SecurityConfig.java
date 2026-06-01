package com.example.smart_farm.global.security.config;

import com.example.smart_farm.global.security.jwt.JwtAuthenticationFilter;
import com.example.smart_farm.global.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource())) // CORS 설정 연결
                .csrf(AbstractHttpConfigurer::disable) // REST API이므로 CSRF 비활성화
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // 세션 미사용

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/auth/**").permitAll() // 로그인, 회원가입은 모두 허용
                        .requestMatchers("/error").permitAll()
                        .anyRequest().authenticated() // 나머지는 인증 필요
                )

                // JwtFilter를 UsernamePasswordAuthenticationFilter 전에 추가
                .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
    // CORS 상세 설정 Bean
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.addAllowedOrigin("http://localhost:5173");

        // 2. 버셀 대표(운영) 도메인 허용 (맨 끝에 슬래시 '/' 반드시 제거!)
        configuration.addAllowedOrigin("https://front-end-fawn-nu.vercel.app");

        // 3. 깃허브 푸시할 때마다 매번 바뀌는 버셀의 모든 프리뷰 주소를 한 번에 허용 (추천)
        configuration.addAllowedOriginPattern("https://*-wonkyu-song-s-projects.vercel.app");
        configuration.addAllowedMethod("*"); // 모든 HTTP 메서드 허용 (GET, POST, PUT 등)
        configuration.addAllowedHeader("*"); // 모든 헤더 허용
        configuration.setAllowCredentials(true); // 자격 증명(쿠키, 인증 헤더 등) 허용

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration); // 모든 경로에 대해 적용
        return source;
    }
}