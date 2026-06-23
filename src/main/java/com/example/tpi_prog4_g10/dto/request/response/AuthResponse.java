@Data @AllArgsConstructor
public class AuthResponse {
    private String token;
    private String tipo = "Bearer";
    private String email;
    private List<String> roles;
}