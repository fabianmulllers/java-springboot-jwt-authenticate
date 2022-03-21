
package com.fmuller.authenticate.app.services;

import com.fmuller.authenticate.app.models.dao.IRoleDao;
import com.fmuller.authenticate.app.models.entity.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RoleService implements IRoleService{

    @Autowired
    private IRoleDao roleDao;
    
    @Override
    public Role getRoleById(Long id) {
       
        return roleDao.findById(id).orElse(null);
        
    }
    
}
