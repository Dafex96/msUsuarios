package cl.duoc.msUsuarios.controller;

import cl.duoc.msUsuarios.dto.RolResponse;
import cl.duoc.msUsuarios.repository.RolRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
@Tag(name = "Roles")
public class RolController {

    private final RolRepository rolRepository;

    @Operation(summary = "Listar roles")
    @GetMapping
    public List<RolResponse> listar() {
        return rolRepository.findAllByOrderByIdAsc().stream()
                .map(r -> new RolResponse(r.getId(), r.getNombre())).toList();
    }
}
