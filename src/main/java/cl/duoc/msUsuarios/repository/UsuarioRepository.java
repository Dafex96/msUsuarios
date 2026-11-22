package cl.duoc.msUsuarios.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import cl.duoc.msUsuarios.model.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    List<Usuario> findAllByOrderByNombreAsc();

    Optional<Usuario> findByCorreo(String correo);

    Optional<Usuario> findByRut(String rut);

    boolean existsByCorreo(String correo);

    boolean existsByRut(String rut);

    boolean existsByCorreoAndIdNot(String correo, Long id);

    boolean existsByRutAndIdNot(String rut, Long id);

    List<Usuario> findByRolIdOrderByNombreAsc(Long rolId);

    @Query("""
            SELECT u FROM Usuario u
            WHERE LOWER(u.nombre) LIKE LOWER(CONCAT('%', :texto, '%'))
               OR LOWER(u.correo) LIKE LOWER(CONCAT('%', :texto, '%'))
               OR LOWER(u.rut) LIKE LOWER(CONCAT('%', :texto, '%'))
            ORDER BY u.nombre
            """)
    List<Usuario> buscar(@Param("texto") String texto);
}
