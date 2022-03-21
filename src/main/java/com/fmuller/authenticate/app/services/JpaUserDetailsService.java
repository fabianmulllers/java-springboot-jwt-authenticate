package com.fmuller.authenticate.app.services;


import com.fmuller.authenticate.app.models.entity.Usuario;
import com.fmuller.authenticate.app.models.entity.UsuarioRole;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("jpaUserDetailsService")
public class JpaUserDetailsService implements UserDetailsService {

    private Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    private IUsuarioService usuarioService;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        Usuario usuario = usuarioService.findByUsername(username);

        if (usuario == null) {
            log.error("No existe el usuario : ".concat(username));
            throw new UsernameNotFoundException("No existe el usuario : ".concat(username));
        }
        
        List <GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
        for(UsuarioRole roles: usuario.getRoles()){
           log.info("Role: ".concat(roles.getRole().getNombre()));
           authorities.add(new SimpleGrantedAuthority(roles.getRole().getNombre()));
        }

        if(authorities.isEmpty()){
            log.error("Error login: El usuario ".concat(username).concat(" no tiene roles asignados"));
            throw new UsernameNotFoundException("Error login: El usuario ".concat(username).concat(" no tiene roles asignados"));
        }
        
        return new User(usuario.getUsername(), usuario.getPassword(), usuario.isEnabled(), true, true, true, authorities);
    }

}
