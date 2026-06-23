@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CategoriaResponse {
    private Long id;
    private String nombre;
    private String descripcion;
    private int totalProductos;
}