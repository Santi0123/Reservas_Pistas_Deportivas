package com.iesvdc.acceso.pistasdeportivas.repos;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.iesvdc.acceso.pistasdeportivas.modelos.Rol;
import com.iesvdc.acceso.pistasdeportivas.modelos.Usuario;
import java.util.List;


public interface RepoUsuario extends JpaRepository <Usuario, Long>{
    List<Usuario> findByUsername(String username);
    Page<Usuario> findByTipo(Rol tipo, Pageable pageable);
}
