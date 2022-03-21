/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.fmuller.authenticate.app.models.dao;

import com.fmuller.authenticate.app.models.entity.Usuario;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

/**
 *
 * @author fmuller
 */
public interface IUsuarioDao extends CrudRepository<Usuario, Long>{
    
    public Usuario findByUsername(String username);
    
}
