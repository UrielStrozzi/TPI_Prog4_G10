@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductoResponse {
    private Long id;
    private String nombre;
    private String descripcion;
    private List<String> imagenes;
    private EstadoProducto estado;
    private String categoriaNombre;
    private String vendedorNombre;
    private String vendedorEmail;
    private LocalDateTime fechaCreacion;
}