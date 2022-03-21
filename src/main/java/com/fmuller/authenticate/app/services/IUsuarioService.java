
package com.fmuller.authenticate.app.services;

import com.fmuller.authenticate.app.models.entity.Usuario;
import java.util.List;

public interface IUsuarioService {
    
    public List<Usuario> findAll();
    
    public Usuario findById(Long id);
    
    public Usuario findByUsername(String username);
    
    public Usuario save(Usuario usuario);
    
    public void deleteById(Long id);
    
}
