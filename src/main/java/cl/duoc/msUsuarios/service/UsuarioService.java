package cl.duoc.msUsuarios.service;

import cl.duoc.msUsuarios.dto.ActualizarUsuarioRequest;
import cl.duoc.msUsuarios.dto.CambiarContrasenaRequest;
import cl.duoc.msUsuarios.dto.LoginRequest;
import cl.duoc.msUsuarios.dto.RegistroUsuarioRequest;
import cl.duoc.msUsuarios.dto.UsuarioResponse;
import cl.duoc.msUsuarios.exception.ConflictoException;
import cl.duoc.msUsuarios.exception.CredencialesInvalidasException;
import cl.duoc.msUsuarios.exception.RecursoNoEncontradoException;
import cl.duoc.msUsuarios.exception.SolicitudInvalidaException;
import cl.duoc.msUsuarios.model.Rol;
import cl.duoc.msUsuarios.model.Usuario;
import cl.duoc.msUsuarios.repository.RolRepository;
import cl.duoc.msUsuarios.repository.UsuarioRepository;
import cl.duoc.msUsuarios.util.RutUtil;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private static final Long ROL_CLIENTE = 1L;

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UsuarioResponse registrar(RegistroUsuarioRequest req) {
        String correo = normalizarCorreo(req.correo());
        String rut = RutUtil.formatear(req.rut());
        if (usuarioRepository.existsByCorreo(correo)) {
            throw new ConflictoException("Ya existe una cuenta con ese correo");
        }
        if (usuarioRepository.existsByRut(rut)) {
            throw new ConflictoException("Ya existe una cuenta con ese RUT");
        }
        Long rolId = req.rolId() != null ? req.rolId() : ROL_CLIENTE;
        exigirRol(rolId);

        Usuario usuario = Usuario.builder()
                .rut(rut)
                .nombre(req.nombre().trim())
                .correo(correo)
                .contrasena(passwordEncoder.encode(req.contrasena()))
                .rolId(rolId)
                .telefono(texto(req.telefono()))
                .direccion(texto(req.direccion()))
                .ciudad(texto(req.ciudad()))
                .build();
        return aRespuesta(usuarioRepository.save(usuario));
    }

    @Transactional(readOnly = true)
    public UsuarioResponse login(LoginRequest req) {
        String id = req.identificador().trim();
        Optional<Usuario> encontrado = id.contains("@")
                ? usuarioRepository.findByCorreo(id.toLowerCase())
                : usuarioRepository.findByRut(RutUtil.formatear(id));
        Usuario usuario = encontrado
                .filter(u -> passwordEncoder.matches(req.contrasena(), u.getContrasena()))
                .orElseThrow(() -> new CredencialesInvalidasException("Correo, RUT o contraseña incorrectos"));
        return aRespuesta(usuario);
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponse> listar() {
        return aRespuestas(usuarioRepository.findAllByOrderByNombreAsc());
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponse> buscar(String texto) {
        return aRespuestas(usuarioRepository.buscar(texto.trim()));
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponse> listarPorRol(Long rolId) {
        return aRespuestas(usuarioRepository.findByRolIdOrderByNombreAsc(rolId));
    }

    @Transactional(readOnly = true)
    public UsuarioResponse obtener(Long id) {
        return aRespuesta(buscarEntidad(id));
    }

    @Transactional
    public UsuarioResponse actualizar(Long id, ActualizarUsuarioRequest req) {
        Usuario usuario = buscarEntidad(id);
        String correo = normalizarCorreo(req.correo());
        String rut = RutUtil.formatear(req.rut());

        boolean cambiaRut = !RutUtil.normalizar(usuario.getRut()).equals(RutUtil.normalizar(rut));
        if (cambiaRut && !RutUtil.esValido(rut)) {
            throw new SolicitudInvalidaException("El RUT no es válido");
        }
        if (usuarioRepository.existsByCorreoAndIdNot(correo, id)) {
            throw new ConflictoException("Ya existe otra cuenta con ese correo");
        }
        if (usuarioRepository.existsByRutAndIdNot(rut, id)) {
            throw new ConflictoException("Ya existe otra cuenta con ese RUT");
        }
        if (req.rolId() != null) {
            exigirRol(req.rolId());
            usuario.setRolId(req.rolId());
        }
        usuario.setRut(rut);
        usuario.setNombre(req.nombre().trim());
        usuario.setCorreo(correo);
        usuario.setTelefono(texto(req.telefono()));
        usuario.setDireccion(texto(req.direccion()));
        usuario.setCiudad(texto(req.ciudad()));
        return aRespuesta(usuarioRepository.save(usuario));
    }

    @Transactional
    public UsuarioResponse actualizarAvatar(Long id, String avatarBase64) {
        Usuario usuario = buscarEntidad(id);
        usuario.setAvatar(avatarBase64);
        return aRespuesta(usuarioRepository.save(usuario));
    }

    @Transactional
    public void cambiarContrasena(Long id, CambiarContrasenaRequest req) {
        Usuario usuario = buscarEntidad(id);
        if (!passwordEncoder.matches(req.actual(), usuario.getContrasena())) {
            throw new SolicitudInvalidaException("La contraseña actual no es correcta");
        }
        usuario.setContrasena(passwordEncoder.encode(req.nueva()));
        usuarioRepository.save(usuario);
    }

    @Transactional
    public void eliminar(Long id) {
        usuarioRepository.delete(buscarEntidad(id));
    }

    private Usuario buscarEntidad(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el usuario con id " + id));
    }

    private void exigirRol(Long rolId) {
        if (!rolRepository.existsById(rolId)) {
            throw new SolicitudInvalidaException("El rol indicado no existe");
        }
    }

    private String normalizarCorreo(String correo) {
        return correo.trim().toLowerCase();
    }

    private String texto(String valor) {
        return valor == null ? "" : valor.trim();
    }

    private UsuarioResponse aRespuesta(Usuario u) {
        String rol = rolRepository.findById(u.getRolId()).map(Rol::getNombre).orElse("");
        return construir(u, rol);
    }

    private List<UsuarioResponse> aRespuestas(List<Usuario> usuarios) {
        Map<Long, String> roles = new HashMap<>();
        rolRepository.findAll().forEach(r -> roles.put(r.getId(), r.getNombre()));
        return usuarios.stream().map(u -> construir(u, roles.getOrDefault(u.getRolId(), ""))).toList();
    }

    private UsuarioResponse construir(Usuario u, String rolNombre) {
        return new UsuarioResponse(u.getId(), u.getRut(), u.getNombre(), u.getCorreo(), u.getRolId(),
                rolNombre, u.getTelefono(), u.getDireccion(), u.getCiudad(), u.getAvatar());
    }
}
