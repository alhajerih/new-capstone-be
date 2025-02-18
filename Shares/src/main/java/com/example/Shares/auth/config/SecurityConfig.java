//package com.example.Shares.config;
//
//
//import com.example.Shares.service.auth.CustomUserDetailsService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.authentication.AuthenticationManager;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
//import org.springframework.security.config.http.SessionCreationPolicy;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
//import org.springframework.web.cors.CorsConfiguration;
//
//import java.util.Arrays;
//import java.util.Collections;
//import java.util.List;
//
//@EnableWebSecurity
//@Configuration
//public class SecurityConfig extends WebSecurityConfigurerAdapter {
//
//    /*
//    These lines declare constant strings AUTH_PATH and USER_PATH, representing the paths related to authentication and user-related resources, respectively.
//     */
//    public static final String AUTH_PATH= "/api/v1/auth/**";
//    public static final String ADMIN_PATH="/api/v1/admin-dashboard/**";
//    public static final String USER_PATH = "/api/v1/nice-worlds/**";
//
//    /*
//    These lines declare two lists of strings, ALLOWED_METHODS and ALLOWED_HEADERS. These lists specify the HTTP methods and headers allowed for CORS (Cross-Origin Resource Sharing) configuration.
//     */
//    private static final List<String> ALLOWED_METHODS = Arrays.asList("GET", "PUT", "POST", "DELETE", "OPTIONS", "PATCH");
//    private static final List<String> ALLOWED_HEADERS = Arrays.asList("x-requested-with", "authorization", "Content-Type",
//            "Authorization", "credential", "X-XSRF-TOKEN", "X-Refresh-Token", "X-Client-Id", "x-client-id");
//
//    @Autowired
//    private JWTUtil jwtUtil;
//
//    @Autowired
//    private CustomUserDetailsService userDetailsService;
//
//    /*
//    - **`.csrf().disable()`**: Disables CSRF (Cross-Site Request Forgery) protection.
//    - **`.exceptionHandling()`**: Configures exception handling for security-related exceptions.
//    - **`.httpBasic().disable()`**: Disables HTTP Basic Authentication.
//    - **`.formLogin().disable()`**: Disables form-based login.
//    - **`.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)`**: Configures session management to be stateless, meaning no sessions will be created.
//    - **`.cors().configurationSource(request -> getCorsConfiguration())`**: Configures CORS (Cross-Origin Resource Sharing) using a custom CorsConfiguration source.
//    - **`.authorizeRequests()`**: Begins configuring request authorization rules.
//    - **`.antMatchers(AUTH_PATH).permitAll()`**: Specifies that requests to **`AUTH_PATH`** should be permitted without authentication.
//    - **`.antMatchers(USER_PATH).hasAuthority("user")`**: Specifies that requests to **`USER_PATH`** require the "user" authority.
//    - **`.anyRequest().authenticated()`**: Requires authentication for any other requests.
//    - Finally, a custom **`JwtAuthFilter`** is added before the **`UsernamePasswordAuthenticationFilter`**. This filter is responsible for JWT-based authentication.
//     */
//    @Override
//    protected void configure(HttpSecurity http) throws Exception {
//        http.csrf().disable()
//                .exceptionHandling()
//                .and()
//                .httpBasic().disable()
//                .formLogin().disable()
//                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
//                .and()
//                .cors()
//                .configurationSource(request -> getCorsConfiguration())
//                .and()
//                .authorizeRequests()
//                .antMatchers(AUTH_PATH).permitAll()
//                .antMatchers(USER_PATH).hasAuthority("user")
//                .antMatchers(ADMIN_PATH).hasAuthority("admin")
//                .anyRequest().authenticated();
//        http.addFilterBefore(new JwtAuthFilter(jwtUtil,userDetailsService), UsernamePasswordAuthenticationFilter.class);
//
//    }
//
//    /*
//    creates a CorsConfiguration object with allowed headers, methods, origins, and credentials settings for CORS.
//     */
//    private CorsConfiguration getCorsConfiguration(){
//        CorsConfiguration corsConfiguration = new CorsConfiguration();
//        corsConfiguration.setAllowedHeaders(ALLOWED_HEADERS);
//        corsConfiguration.setAllowedMethods(ALLOWED_METHODS);
//        corsConfiguration.setAllowedOriginPatterns(Collections.singletonList("*"));
//        corsConfiguration.setAllowCredentials(true);
//
//        return corsConfiguration;
//    }
//
//    @Bean
//    @Override
//    protected AuthenticationManager authenticationManager() throws Exception {
//        return super.authenticationManager();
//    }
//
//    @Bean
//    public BCryptPasswordEncoder bCryptPasswordEncoder(){
//        return new BCryptPasswordEncoder();
//    }
//}


// Security Configuration
package com.example.Shares.auth.config;

import com.example.Shares.auth.service.auth.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter {

    public static final String AUTH_PATH = "/api/user";
    @Autowired
    private JWTUtil jwtUtil;
    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable()
                .authorizeRequests()
                .antMatchers(
                        "/swagger-ui.html",
                        "/swagger-ui/**",
                        "/v3/api-docs",
                        "/api-docs/**",
                        "/swagger-resources/**",
                        "/webjars/**",
                        "/ws/**",
                        "/api/setup/**",
                        "/api/user/pay-with-hubcard",
                        "/api/user/qrcode",
                        "/api/user/pay-with-hubcard-unified",
                        "/api/user/pay-with-hubcard-ai"
                ).permitAll()
                .antMatchers(AUTH_PATH + "/login", AUTH_PATH + "/generate-otp", AUTH_PATH + "/validate-otp", AUTH_PATH + "/register").permitAll()
                .anyRequest().authenticated()
                .and()
                .addFilterBefore(new JwtAuthFilter(jwtUtil, userDetailsService), UsernamePasswordAuthenticationFilter.class);
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}