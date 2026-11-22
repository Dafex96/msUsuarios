package cl.duoc.msUsuarios.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "usuarios")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 12)
    private String rut;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, unique = true, length = 100)
    private String correo;

    @Column(nullable = false, length = 100)
    private String contrasena;

    @Column(name = "rol_id", nullable = false)
    private Long rolId;

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String telefono = "";

    @Column(nullable = false, length = 150)
    @Builder.Default
    private String direccion = "";

    @Column(nullable = false, length = 80)
    @Builder.Default
    private String ciudad = "";

    @Lob
    @Column(name = "avatar")
    private String avatar;
}
