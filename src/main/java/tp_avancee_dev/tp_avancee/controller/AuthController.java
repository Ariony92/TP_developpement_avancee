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
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tp_avancee_dev.tp_avancee.dto.LoginRequestDto;
import tp_avancee_dev.tp_avancee.dto.LoginResponseDto;
import tp_avancee_dev.tp_avancee.dto.RefreshTokenRequestDto;
import tp_avancee_dev.tp_avancee.exception.ApiErrorResponse;
import tp_avancee_dev.tp_avancee.security.CustomUserDetailsService;
import tp_avancee_dev.tp_avancee.security.CustomUserPrincipal;
import tp_avancee_dev.tp_avancee.security.JwtService;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentification", description = "Endpoints d'authentification JWT")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    public AuthController(AuthenticationManager authenticationManager,
                          JwtService jwtService,
                          CustomUserDetailsService userDetailsService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @PostMapping("/login")
    @Operation(
            summary = "Authentification",
            description = "Authentifie un utilisateur et retourne un access token + un refresh token JWT.",
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
        String role = principal.getAuthorities().iterator().next().getAuthority();

        String accessToken = jwtService.generateToken(principal.getUserId(), principal.getUsername(), role);
        String refreshToken = jwtService.generateRefreshToken(principal.getUserId(), principal.getUsername(), role);

        return ResponseEntity.ok(new LoginResponseDto(accessToken, refreshToken, jwtService.getExpirationMs()));
    }

    @PostMapping("/refresh")
    @Operation(
            summary = "Rafraîchir le token",
            description = "Échange un refresh token valide contre un nouveau couple access token + refresh token.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(examples = @ExampleObject(
                            name = "Exemple refresh",
                            value = """
                                    {"refreshToken": "eyJhbGciOi..."}
                                    """
                    ))
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Tokens rafraîchis",
                            content = @Content(schema = @Schema(implementation = LoginResponseDto.class))),
                    @ApiResponse(responseCode = "401", description = "Refresh token invalide ou expiré",
                            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
            }
    )
    public ResponseEntity<LoginResponseDto> refresh(@Valid @RequestBody RefreshTokenRequestDto request) {
        try {
            String refreshToken = request.getRefreshToken();
            String username = jwtService.extractUsername(refreshToken);
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            if (!jwtService.isTokenValid(refreshToken, userDetails) || !jwtService.isRefreshToken(refreshToken)) {
                return ResponseEntity.status(401).build();
            }

            CustomUserPrincipal principal = (CustomUserPrincipal) userDetails;
            String role = principal.getAuthorities().iterator().next().getAuthority();

            String newAccessToken = jwtService.generateToken(principal.getUserId(), principal.getUsername(), role);
            String newRefreshToken = jwtService.generateRefreshToken(principal.getUserId(), principal.getUsername(), role);

            return ResponseEntity.ok(new LoginResponseDto(newAccessToken, newRefreshToken, jwtService.getExpirationMs()));
        } catch (Exception e) {
            return ResponseEntity.status(401).build();
        }
    }
}
