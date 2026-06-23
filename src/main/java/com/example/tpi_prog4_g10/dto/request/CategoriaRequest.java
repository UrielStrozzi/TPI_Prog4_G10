@Data
public class CategoriaRequest {

    @NotBlank
    @Size(max = 80)
    private String nombre;

    private String descripcion;
}