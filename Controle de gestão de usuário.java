package com.example.usermanagement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import javax.persistence.*;
import java.util.List;

// Classe principal do Spring Boot que inicia a aplicação
@SpringBootApplication
public class UserManagementApplication {
    public static void main(String[] args) {
        SpringApplication.run(UserManagementApplication.class, args);
    }
    
    // Bean para criptografar senhas dos usuários usando BCrypt
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

// Modelo de usuário para persistência no banco de dados
@Entity
class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Geração automática do ID
    private Long id;
    private String username;
    private String password;
    private String role;
    
    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}

// Repositório para operações com banco de dados
interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username); // Busca usuário pelo nome
}

// Serviço para manipulação de usuários e autenticação
@Service
class UserService implements UserDetailsService {
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    // Salva um novo usuário criptografando a senha antes
    public User saveUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    // Retorna a lista de usuários cadastrados
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // Busca usuário pelo nome para autenticação e retorna um UserDetails para Spring Security
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("User not found");
        }
        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPassword())
                .roles(user.getRole())
                .build();
    }
}

// Controlador REST para gerenciar usuários via API
@RestController
@RequestMapping("/users")
class UserController {
    @Autowired
    private UserService userService;
    
    // Endpoint para registrar um novo usuário
    @PostMapping("/register")
    public User registerUser(@RequestBody User user) {
        return userService.saveUser(user);
    }
    
    // Endpoint para listar todos os usuários
    @GetMapping("/list")
    public List<User> listUsers() {
        return userService.getAllUsers();
    }
}

// Configuração de segurança com Spring Security
@EnableWebSecurity
class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Autowired
    private UserService userService;

    // Gerenciador de autenticação, usado para autenticar usuários
    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }
    
    // Configuração de segurança para definir permissões e autenticação
    @Override
    protected void configure(org.springframework.security.config.annotation.web.builders.HttpSecurity http) throws Exception {
        http.csrf().disable() // Desativa proteção CSRF para facilitar testes via API
            .authorizeRequests()
            .antMatchers("/users/register").permitAll() // Permite acesso livre ao cadastro de usuários
            .anyRequest().authenticated() // Exige autenticação para qualquer outro endpoint
            .and()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS); // Configuração para API Stateless
    }
}
