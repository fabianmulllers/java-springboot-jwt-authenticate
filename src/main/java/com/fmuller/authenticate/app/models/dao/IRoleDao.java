package com.fmuller.authenticate.app.models.dao;


import com.fmuller.authenticate.app.models.entity.Role;
import org.springframework.data.repository.CrudRepository;

public interface IRoleDao extends CrudRepository<Role, Long>{
    
}
