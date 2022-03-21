package com.fmuller.authenticate.app.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fmuller.authenticate.app.models.entity.Role;
import com.fmuller.authenticate.app.models.entity.Usuario;
import com.fmuller.authenticate.app.models.entity.UsuarioRole;
import com.fmuller.authenticate.app.services.IRoleService;
import com.fmuller.authenticate.app.services.IUsuarioService;
import com.fmuller.authenticate.app.services.UsuarioService;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/usuario")
public class usuarioController {

    private Logger log = LoggerFactory.getLogger(usuarioController.class);

    @Autowired
    private IUsuarioService usuarioService;
    
    @Autowired
    private IRoleService roleService;
    
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @GetMapping("")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    public ResponseEntity<?> index() {

        Map<String, Object> response = new HashMap<>();
        List<Usuario> usuarios = usuarioService.findAll();
        response.put("usuarios", usuarios);
        return new ResponseEntity<Map<String, Object>>(response, HttpStatus.OK);

    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    public ResponseEntity<?> obtenerUsuario(@PathVariable Long id) throws JsonProcessingException {

        Usuario usuario = null;
        Map<String, Object> response = new HashMap<>();
        try {
            usuario = usuarioService.findById(id);

        } catch (DataAccessException e) {
            response.put("mensaje", "Ocurrio un error interno, Favor contactarse con el Administrador ");
            response.put("error", e.getMessage().concat(" :").concat(e.getMostSpecificCause().getMessage()));
            return new ResponseEntity<Map<String, Object>>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        if (usuario == null) {
            response.put("mensaje", "El usuario con el ID:" + id + " no existe en el sistema");
            return new ResponseEntity<Map<String, Object>>(response, HttpStatus.NOT_FOUND);
        }
        
        Map<String,Object> usuarioResponse = new HashMap<>();
        usuarioResponse.put("username", usuario.getUsername());
        usuarioResponse.put("email", usuario.getEmail());
        usuarioResponse.put("roles",usuario.getRoles());
        usuarioResponse.put("createAt",usuario.getCreateAt());
        usuarioResponse.put("enabled",usuario.isEnabled());
        response.put("usuario", usuario);
 
        return new ResponseEntity<Map<String, Object>>(response, HttpStatus.FOUND);
    }

    @PostMapping("")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    public ResponseEntity<?> save(@Valid @RequestBody Usuario usuario, BindingResult result, HttpServletRequest request) {
              
        Map<String, Object> response = new HashMap<String, Object>();
        Usuario usuarioNuevo = null;
        
        if (result.hasErrors()) {

            List<Map<String, String>> errores = new ArrayList<>();
            for (FieldError err : result.getFieldErrors()) {
                Map<String, String> errorMap = new HashMap<String, String>();
                errorMap.put("input", err.getField());
                errorMap.put("value", err.getDefaultMessage());
                errores.add(errorMap);
            }
            response.put("errores", errores);
            response.put("mensaje", "errores en los campos");
            return new ResponseEntity<Map<String, Object>>(response, HttpStatus.BAD_REQUEST);
        }
        
        List<Long> rolesList = usuario.getRolesList();
        if(rolesList == null){
            response.put("mensaje", "el usuario necesita al menos 1 role");
            return new ResponseEntity<Map<String,Object>>(response,HttpStatus.BAD_REQUEST);
        }
                
        rolesList.forEach( rol -> {
            Role role = roleService.getRoleById(rol);
            UsuarioRole usuarioRole = new UsuarioRole();
            usuarioRole.setRole(role);
            usuario.addUsuarioRole(usuarioRole);
        });
        
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));

        try {
            usuarioNuevo = usuarioService.save(usuario);

        } catch (DataAccessException e) {
            response.put("mensaje", "No se logro crear al usuario");
            response.put("error", e.getMessage().concat(": ").concat(e.getMostSpecificCause().getMessage()));
            return new ResponseEntity<Map<String, Object>>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        response.put("mensaje", "Usuario creado con exito");
        response.put("usuario", usuarioNuevo);
        return new ResponseEntity<Map<String, Object>>(response, HttpStatus.CREATED);

    }
    
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@Valid @RequestBody Usuario usuario, BindingResult result,@PathVariable String id){
        
        Map<String,Object> response = new HashMap<String,Object>();
        Usuario usuarioActual = null;
        Usuario usuarioEditado = null;
        usuarioActual = usuarioService.findById(Long.parseLong(id));

        //validar los campos enviados
        
        
        if(usuarioActual == null){
            response.put("mensaje", "No existe el usuario en la base de datos");
            return new ResponseEntity<Map<String,Object>>(response,HttpStatus.BAD_REQUEST);
        }
        
        if(usuario.getRolesList() == null){
            response.put("mensaje", "El usuario debe tener al menos 1 role");
            return new ResponseEntity<Map<String, Object>>(response,HttpStatus.BAD_REQUEST);
        }
        
        List<FieldError> errorsToKeep = result.getFieldErrors().stream()
                .filter(fer -> !fer.getField().equals("password"))
                .collect(Collectors.toList());

        result = new BeanPropertyBindingResult(Usuario.class, "usuario");
        for (FieldError fieldError : errorsToKeep) {
            result.addError(fieldError);
        }
        
        if(result.hasErrors()){
            List<Map<String,Object>> errorList = new ArrayList<>();
                        
            result.getFieldErrors().forEach( err->{
                Map<String,Object> errorMap = new HashMap<String,Object>();
                errorMap.put("input", err.getField());
                errorMap.put("value", err.getDefaultMessage());
                errorList.add(errorMap);
            } );
           
            response.put("errores",errorList);
            response.put("mensaje", "errores en los campos");
            return new ResponseEntity<Map<String,Object>>(response,HttpStatus.BAD_REQUEST);
        }
        
        try {
            usuarioActual.setEmail(usuario.getEmail());
            usuarioActual.setUsername(usuario.getUsername());

            List<Long> rolesList = usuario.getRolesList();
            
            usuarioActual.getRoles().clear();
            for( Long rol: rolesList ){
                Role role = roleService.getRoleById(rol);
                if(role != null){
                    UsuarioRole usuarioRole = new UsuarioRole();
                    usuarioRole.setRole(role);
                    usuarioActual.addUsuarioRole(usuarioRole);
                }
                
            }
            
            if(usuarioActual.getRoles().isEmpty()){
                response.put("mensaje", "Los roles enviados no se encuentran en la BD");
                return new ResponseEntity<Map<String,Object>>(response,HttpStatus.BAD_REQUEST);
            }
                            
            usuarioEditado = usuarioService.save(usuarioActual);        
            
        } catch (DataAccessException e) {
            response.put("mensaje", "No se logro crear al usuario");
            response.put("error", e.getMessage().concat(": ").concat(e.getMostSpecificCause().getMessage()));
            return new ResponseEntity<Map<String, Object>>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        
        response.put("mensaje","usuario actualizado con exito");
        response.put("usuario",usuarioEditado);
        return new ResponseEntity<Map<String,Object>>(response,HttpStatus.CREATED);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Long id){
      
        log.info("ingresaste");
        Usuario usuario = null;
        usuario = usuarioService.findById(id);
        Map<String,Object> response = new HashMap<String,Object>();
        
        if(usuario == null){
            response.put("mensaje", "No existe el usuario con el id "+ id);
            return new ResponseEntity<Map<String,Object>>(response,HttpStatus.BAD_REQUEST);
        }
        
        try {
            usuario.setEliminado(true);
            usuarioService.save(usuario);
            
        } catch (DataAccessException e) {
            response.put("mensaje", "Ocurrio un error en el sistem, favor contactarse con el administrador");
            response.put("error", "El error "+ e.getMessage() + " :"+ e.getMostSpecificCause().getMessage());
            return new ResponseEntity<Map<String,Object>>(response,HttpStatus.INTERNAL_SERVER_ERROR);
        }
        response.put("mensaje","EL usuario "+ usuario.getUsername() + " fue eliminado con exito");
        return new ResponseEntity<Map<String,Object>>(response,HttpStatus.OK);
    }
    
    @PutMapping("/password/{id}")
    public ResponseEntity<?> actualizarUpdate(@RequestBody String password, @PathVariable Long id) throws IOException{
        
        Map<String,Object> response = new HashMap<String,Object>();
        Usuario usuario = null;
        usuario = usuarioService.findById(id);
        Usuario usuarioActualizado = null;
        
        if(usuario == null){
            response.put("mensaje", "el usuario con el id "+ id +" no existe en la base de datos");
            return new ResponseEntity<Map<String,Object>>(response,HttpStatus.BAD_REQUEST);
        }
        Usuario usuarioActualizar = null;
        usuarioActualizar  = new ObjectMapper().readValue(password, Usuario.class);
        
        log.info("aqui"+usuarioActualizar);
        
        if(usuarioActualizar.getPassword() == null){
            response.put("mensaje","Es necesario enviar nuevo password del usuario");
            return new ResponseEntity<Map<String,Object>>(response,HttpStatus.BAD_REQUEST);
        }
        
        try {
        
            usuario.setPassword(passwordEncoder.encode(usuarioActualizar.getPassword()));
            usuarioActualizado = usuarioService.save(usuario);
        } catch (DataAccessException e) {
            response.put("mensaje","Ocurrio un error en el sistema, favor conectarse co el administrador");
            response.put("error","Error: "+ e.getMessage() + " :" + e.getMostSpecificCause().getMessage());
            return new ResponseEntity<Map<String,Object>>(response,HttpStatus.INTERNAL_SERVER_ERROR);
        }
        
        response.put("usuario", usuarioActualizado);
        response.put("mensaje","El password fue actualizado con exito");
        
        return new ResponseEntity<Map<String,Object>>(response,HttpStatus.OK);
    }
    
}
