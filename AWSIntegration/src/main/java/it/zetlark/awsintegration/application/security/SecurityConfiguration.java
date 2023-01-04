package it.zetlark.awsintegration.application.security;

import it.zetlark.awsintegration.application.security.cognito.AwsCognitoJwtAuthFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

import java.util.List;


@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(securedEnabled = true, prePostEnabled = true) // To configure method-level security
public class SecurityConfiguration {
	@Autowired
	private AwsCognitoJwtAuthFilter awsCognitoJwtAuthenticationFilter;


	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

		CorsConfiguration corsConfiguration = new CorsConfiguration();
		corsConfiguration.setAllowedHeaders(List.of("*"));
		corsConfiguration.setAllowedOrigins(List.of("*"));
		corsConfiguration.setAllowedMethods(List.of("*"));
		//corsConfiguration.setAllowCredentials(true);
		corsConfiguration.setExposedHeaders(List.of("*"));

		http
				.cors().configurationSource(request -> corsConfiguration)
				.and()
				.csrf().disable()
				.authorizeRequests()
				.anyRequest().authenticated()
				.and()
				.addFilterBefore(awsCognitoJwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
				.headers().cacheControl();

		return http.build();
	}

	@Bean
	public WebSecurityCustomizer webSecurityCustomizer() {
		return (web) -> web.ignoring()
				.antMatchers("/api/auth/**")
				.antMatchers("/v3/api-docs/**")
				.antMatchers("configuration/**")
				.antMatchers("/swagger*/**")
				.antMatchers("/webjars/**")
				.antMatchers("/swagger-ui/**")
				.antMatchers("/unity-customizer/**");
	}

}
