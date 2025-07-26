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
		http.authorizeRequests().antMatchers("/index", "/", "/css/**", "/images/**", "/js/**", "/feedback/**", "/vendor/**").permitAll()
				.antMatchers("/usuarios/").hasAuthority("Administrador").antMatchers("/usuarios/lista")
				.hasAuthority("Administrador").antMatchers("/usuarios/lista/edit/**")
				.hasAuthority("Administrador").antMatchers("/usuarios/lista/delete/**").hasAuthority("Administrador")
				.antMatchers("/usuarios/lista/**").hasAuthority("Administrador")
				
				.antMatchers("/preguntas/").hasAuthority("Administrador").antMatchers("/preguntas/lista")
				.hasAuthority("Administrador").antMatchers("/preguntas/lista/edit/**")
				.hasAuthority("Administrador").antMatchers("/preguntas/lista/delete/**").hasAuthority("Administrador")
				.antMatchers("/preguntas/lista/**").hasAuthority("Administrador")
				
				.antMatchers("/baseDeConocimiento/").hasAuthority("Administrador").antMatchers("/baseDeConocimiento/lista")
				.hasAuthority("Administrador").antMatchers("/baseDeConocimiento/lista/edit/**")
				.hasAuthority("Administrador").antMatchers("/baseDeConocimiento/lista/delete/**").hasAuthority("Administrador")
				.antMatchers("/baseDeConocimiento/lista/**").hasAuthority("Administrador").antMatchers("/baseDeConocimiento/lista/restore/**").hasAuthority("Administrador")
				.antMatchers("/baseDeConocimiento/buscar/**").hasAuthority("Administrador")
				
				.anyRequest().authenticated().and()
				.formLogin()
				.loginPage("/login") // Tu página de login personalizada
				.defaultSuccessUrl("/index", true) // Después del login exitoso
				.failureUrl("/login?error=true") // Si falla el login
				.permitAll()
		.and()
			.logout()
				.logoutUrl("/logout") // Por defecto ya es POST /logout
				.logoutSuccessUrl("/index?logout") // Redirige al home tras logout
				.invalidateHttpSession(true)
				.deleteCookies("JSESSIONID")
				.permitAll();
	}

}
