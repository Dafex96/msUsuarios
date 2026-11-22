package cl.duoc.msUsuarios.controller;

import cl.duoc.msUsuarios.dto.MensajeResponse;
import cl.duoc.msUsuarios.dto.RestablecerContrasenaRequest;
import cl.duoc.msUsuarios.dto.SolicitarCodigoRequest;
import cl.duoc.msUsuarios.dto.SolicitudCodigoResponse;
import cl.duoc.msUsuarios.dto.VerificarCodigoRequest;
import cl.duoc.msUsuarios.service.RecuperacionContrasenaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/usuarios/recuperar")
@RequiredArgsConstructor
@Tag(name = "Recuperar contraseña", description = "Pasos: solicitar código, verificar código, restablecer")
public class RecuperacionController {

    private final RecuperacionContrasenaService service;

    @Operation(summary = "Paso 1: solicitar el código de verificación")
    @PostMapping("/solicitar")
    public SolicitudCodigoResponse solicitar(@Valid @RequestBody SolicitarCodigoRequest req) {
        return service.solicitar(req.correo());
    }

    @Operation(summary = "Paso 2: verificar el código")
    @PostMapping("/verificar")
    public MensajeResponse verificar(@Valid @RequestBody VerificarCodigoRequest req) {
        return service.verificar(req);
    }

    @Operation(summary = "Paso 3: definir la nueva contraseña")
    @PostMapping("/restablecer")
    public MensajeResponse restablecer(@Valid @RequestBody RestablecerContrasenaRequest req) {
        return service.restablecer(req);
    }
}
