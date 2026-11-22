package cl.duoc.msUsuarios.service;

import cl.duoc.msUsuarios.dto.MensajeResponse;
import cl.duoc.msUsuarios.dto.RestablecerContrasenaRequest;
import cl.duoc.msUsuarios.dto.SolicitudCodigoResponse;
import cl.duoc.msUsuarios.dto.VerificarCodigoRequest;
import cl.duoc.msUsuarios.exception.SolicitudInvalidaException;
import cl.duoc.msUsuarios.model.CodigoRecuperacion;
import cl.duoc.msUsuarios.model.Usuario;
import cl.duoc.msUsuarios.repository.CodigoRecuperacionRepository;
import cl.duoc.msUsuarios.repository.UsuarioRepository;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecuperacionContrasenaService {

    private static final String CODIGO_INVALIDO = "El código es inválido o ha expirado";
    private static final SecureRandom RANDOM = new SecureRandom();

    private final UsuarioRepository usuarioRepository;
    private final CodigoRecuperacionRepository codigoRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${usuarios.recuperacion.minutos-expiracion:10}")
    private int minutosExpiracion;

    @Value("${usuarios.recuperacion.max-intentos:5}")
    private int maxIntentos;

    @Value("${usuarios.recuperacion.exponer-codigo:false}")
    private boolean exponerCodigo;

    @Transactional
    public SolicitudCodigoResponse solicitar(String correoEntrada) {
        String correo = correoEntrada.trim().toLowerCase();
        String codigoDesarrollo = null;

        if (usuarioRepository.findByCorreo(correo).isPresent()) {
            String codigo = String.format("%06d", RANDOM.nextInt(1_000_000));
            codigoRepository.deleteByCorreo(correo);
            codigoRepository.save(CodigoRecuperacion.builder()
                    .correo(correo)
                    .codigoHash(passwordEncoder.encode(codigo))
                    .expiracion(LocalDateTime.now().plusMinutes(minutosExpiracion))
                    .build());
            if (exponerCodigo) {
                codigoDesarrollo = codigo;
                log.info("[RECUPERACION] Código para {}: {}", correo, codigo);
            }
        }
        return new SolicitudCodigoResponse(
                "Si el correo está registrado, enviamos un código de verificación",
                minutosExpiracion, codigoDesarrollo);
    }

    @Transactional(noRollbackFor = SolicitudInvalidaException.class)
    public MensajeResponse verificar(VerificarCodigoRequest req) {
        validar(req.correo().trim().toLowerCase(), req.codigo().trim());
        return new MensajeResponse("Código verificado");
    }

    @Transactional(noRollbackFor = SolicitudInvalidaException.class)
    public MensajeResponse restablecer(RestablecerContrasenaRequest req) {
        String correo = req.correo().trim().toLowerCase();
        CodigoRecuperacion codigo = validar(correo, req.codigo().trim());
        Usuario usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new SolicitudInvalidaException(CODIGO_INVALIDO));
        usuario.setContrasena(passwordEncoder.encode(req.nuevaContrasena()));
        usuarioRepository.save(usuario);
        codigo.setUsado(true);
        codigoRepository.save(codigo);
        return new MensajeResponse("Contraseña restablecida correctamente");
    }

    private CodigoRecuperacion validar(String correo, String codigoIngresado) {
        Optional<CodigoRecuperacion> vigente = codigoRepository.findTopByCorreoAndUsadoFalseOrderByIdDesc(correo);
        CodigoRecuperacion codigo = vigente.orElseThrow(() -> new SolicitudInvalidaException(CODIGO_INVALIDO));
        if (codigo.getExpiracion().isBefore(LocalDateTime.now()) || codigo.getIntentos() >= maxIntentos) {
            throw new SolicitudInvalidaException(CODIGO_INVALIDO);
        }
        if (!passwordEncoder.matches(codigoIngresado, codigo.getCodigoHash())) {
            codigo.setIntentos(codigo.getIntentos() + 1);
            codigoRepository.save(codigo);
            throw new SolicitudInvalidaException(CODIGO_INVALIDO);
        }
        return codigo;
    }
}
