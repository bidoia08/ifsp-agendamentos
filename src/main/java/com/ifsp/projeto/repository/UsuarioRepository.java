package com.ifsp.projeto.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.ifsp.projeto.model.Usuario;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByNomeUsuarioAndSenha(String nomeUsuario, String senha);

    Optional<Usuario> findByNomeUsuario(String nomeUsuario); // 👈 adicione esta linha
    
    Usuario findByNomeUsuarioAndPalavraSeguranca(String nomeUsuario, String palavraSeguranca);
}