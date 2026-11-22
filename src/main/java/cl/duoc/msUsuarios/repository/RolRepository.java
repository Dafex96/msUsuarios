package cl.duoc.msUsuarios.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import cl.duoc.msUsuarios.model.Rol;

public interface RolRepository extends JpaRepository<Rol, Long> {

    List<Rol> findAllByOrderByIdAsc();
}
