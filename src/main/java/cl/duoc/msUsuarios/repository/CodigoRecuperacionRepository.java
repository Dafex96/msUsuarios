package cl.duoc.msUsuarios.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import cl.duoc.msUsuarios.model.CodigoRecuperacion;

public interface CodigoRecuperacionRepository extends JpaRepository<CodigoRecuperacion, Long> {

    Optional<CodigoRecuperacion> findTopByCorreoAndUsadoFalseOrderByIdDesc(String correo);

    void deleteByCorreo(String correo);
}
