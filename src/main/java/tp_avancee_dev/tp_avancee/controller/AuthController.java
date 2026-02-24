package tp_avancee_dev.tp_avancee.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tp_avancee_dev.tp_avancee.dto.LoginRequestDto;
import tp_avancee_dev.tp_avancee.dto.LoginResponseDto;
import tp_avancee_dev.tp_avancee.exception.ApiErrorResponse;
import tp_avancee_dev.tp_avancee.security.CustomUserPrincipal;
import tp_avancee_dev.tp_avancee.security.JwtService;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentification", description = "Endpoints d'authentification JWT")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthController(AuthenticationManager authenticationManager, JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    @Operation(
            summary = "Authentification",
            description = "Authentifie un utilisateur et retourne un token JWT signé contenant l'ID, le username et le rôle.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(examples = @ExampleObject(
                            name = "Exemple login",
                            value = """
                                    {"username": "admin", "password": "admin123"}
                                    """
                    ))
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Authentification réussie",
                            content = @Content(schema = @Schema(implementation = LoginResponseDto.class))),
                    @ApiResponse(responseCode = "401", description = "Identifiants invalides",
                            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Requête invalide (champs manquants)",
                            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
            }
    )
    public ResponseEntity<LoginResponseDto> login(@Valid @RequestBody LoginRequestDto request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

        CustomUserPrincipal principal = (CustomUserPrincipal) authentication.getPrincipal();

        String token = jwtService.generateToken(
                principal.getUserId(),
                principal.getUsername(),
                principal.getAuthorities().iterator().next().getAuthority());

        return ResponseEntity.ok(new LoginResponseDto(token, jwtService.getExpirationMs()));
    }
}
