package com.fmuller.authenticate.app.models.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import com.fasterxml.jackson.annotation.*;
import org.hibernate.annotations.Where;

@Entity
@Table(name = "usuario")
@Where(clause = "eliminado = 0")
@Valid
@JsonIgnoreProperties(ignoreUnknown = true)
public class Usuario implements Serializable{
    
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true,nullable = false)
    @NotNull
    private String username;
    
    @Size(min = 4)
    @Column(nullable = false)
    @NotNull
    @JsonProperty( value = "password", access = JsonProperty.Access.WRITE_ONLY)
    private String password;
    
    @Column(unique = true,nullable = false)
    @NotNull
    private String email;
    
    private boolean enabled;
    
    private boolean eliminado = false;

    @Column(name = "create_at")
    @Temporal(TemporalType.DATE)
    private Date CreateAt;
    
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL,orphanRemoval = true)
    @JoinColumn(name = "usuario_id")
    private List<UsuarioRole> roles;
    
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Transient
    private List<Long> rolesList;
    
    public Usuario(){
        this.roles = new ArrayList<UsuarioRole>();
    }
    
    @PrePersist
    public void prePersist(){
        this.CreateAt = new Date();
        this.enabled = true;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Date getCreateAt() {
        return CreateAt;
    }

    public void setCreateAt(Date CreateAt) {
        this.CreateAt = CreateAt;
    }

    public List<UsuarioRole> getRoles() {
        return roles;
    }

    public void setRoles(List<UsuarioRole> roles) {
        this.roles = roles;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public List<Long> getRolesList() {
        return rolesList;
    }

    public void addUsuarioRole(UsuarioRole usuarioRole){
        this.roles.add(usuarioRole);
    }

    public boolean isEliminado() {
        return eliminado;
    }

    public void setEliminado(boolean eliminado) {
        this.eliminado = eliminado;
    }
    
    public void clearRoles(){
        this.roles = new ArrayList<UsuarioRole>();
    }
    
}

