package com.chatbot.unla;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

	@Autowired
	private DataSource dataSource;

	@Autowired
	private BCryptPasswordEncoder passEncoder;

	@Autowired
	public void configurerSecurityGlobal(AuthenticationManagerBuilder builder) throws Exception {
		builder.jdbcAuthentication().dataSource(dataSource).passwordEncoder(passEncoder)
				.usersByUsernameQuery(
						"SELECT nombre_de_usuario, contrasena, deshabilitado from usuario where nombre_de_usuario=?")
				.authoritiesByUsernameQuery(
						"SELECT u.nombre_de_usuario, p.rol from perfiles p inner join usuario u on p.id=u.perfiles_id where u.nombre_de_usuario=?");

	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
	    http.csrf().disable();
	    http.authorizeRequests()
	        // Rutas públicas
	        .antMatchers("/index", "/", "/css/**", "/images/**", "/js/**", "/feedback/**", "/vendor/**").permitAll()

	        // Usuarios: solo Administrador
	        .antMatchers("/usuarios/").hasAuthority("Administrador")
	        .antMatchers("/usuarios/lista").hasAuthority("Administrador")
	        .antMatchers("/usuarios/lista/edit/**").hasAuthority("Administrador")
	        .antMatchers("/usuarios/lista/delete/**").hasAuthority("Administrador")
	        .antMatchers("/usuarios/lista/cambiar-contrasena/**").hasAnyAuthority("Administrador", "Operador")

	        // Preguntas: Administrador y Operador
	        .antMatchers("/preguntas/").hasAnyAuthority("Administrador", "Operador")
	        .antMatchers("/preguntas/lista").hasAnyAuthority("Administrador", "Operador")
	        .antMatchers("/preguntas/lista/edit/**").hasAnyAuthority("Administrador", "Operador")
	        .antMatchers("/preguntas/lista/delete/**").hasAnyAuthority("Administrador", "Operador")
	        .antMatchers("/preguntas/lista/**").hasAnyAuthority("Administrador", "Operador")

	        // Base de conocimiento: Administrador y Operador
	        .antMatchers("/baseDeConocimiento/").hasAnyAuthority("Administrador", "Operador")
	        .antMatchers("/baseDeConocimiento/lista").hasAnyAuthority("Administrador", "Operador")
	        .antMatchers("/baseDeConocimiento/lista/edit/**").hasAnyAuthority("Administrador", "Operador")
	        .antMatchers("/baseDeConocimiento/lista/delete/**").hasAnyAuthority("Administrador", "Operador")
	        .antMatchers("/baseDeConocimiento/lista/**").hasAnyAuthority("Administrador", "Operador")
	        .antMatchers("/baseDeConocimiento/lista/restore/**").hasAnyAuthority("Administrador", "Operador")
	        .antMatchers("/baseDeConocimiento/buscar/**").hasAnyAuthority("Administrador", "Operador")

	        // Cualquier otra solicitud autenticada
	        .anyRequest().authenticated()
	    .and()
	        .formLogin()
	            .loginPage("/login")
	            .defaultSuccessUrl("/index", true)
	            .failureUrl("/login?error=true")
	            .permitAll()
	    .and()
	        .logout()
	            .logoutUrl("/logout")
	            .logoutSuccessUrl("/index?logout")
	            .invalidateHttpSession(true)
	            .deleteCookies("JSESSIONID")
	            .permitAll();
	}

}
