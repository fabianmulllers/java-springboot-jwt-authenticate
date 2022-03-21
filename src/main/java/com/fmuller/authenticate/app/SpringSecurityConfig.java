
package com.fmuller.authenticate.app;

import com.fmuller.authenticate.app.auth.filter.JWTAuthenticationFilter;
import com.fmuller.authenticate.app.auth.filter.JWTAuthorizationFilter;
import com.fmuller.authenticate.app.auth.service.IJWTService;
import com.fmuller.authenticate.app.services.JpaUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@EnableGlobalMethodSecurity(securedEnabled = true, prePostEnabled = true)
@Configuration
public class SpringSecurityConfig extends WebSecurityConfigurerAdapter{
    
    @Autowired
    private JpaUserDetailsService userDetailsService;
    
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
    
    @Autowired
    private IJWTService jwtService;
    
    protected void configure(HttpSecurity http) throws Exception {
    
        http.authorizeRequests().antMatchers().permitAll()
//        http.authorizeRequests()
        .anyRequest().authenticated()
        .and()
	.addFilter(new JWTAuthenticationFilter(authenticationManager(), jwtService))
        .addFilter(new JWTAuthorizationFilter(authenticationManager(),jwtService))
        .csrf().disable()
        .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
    }
    
    @Autowired
    public void configurerGlobal(AuthenticationManagerBuilder builder) throws Exception{
    
        builder.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder);
        
    }
}
