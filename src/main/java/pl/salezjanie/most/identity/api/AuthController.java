package pl.salezjanie.most.identity.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.salezjanie.most.identity.application.AuthService;
import pl.salezjanie.most.identity.application.CommunityMemberService;
import pl.salezjanie.most.identity.application.RegisterMemberCommand;

/**
 * Public REST API for authentication and registration.
 */
@RestController
@RequestMapping("/api/auth")
class AuthController {

    private final AuthService authService;
    private final CommunityMemberService memberService;

    AuthController(AuthService authService, CommunityMemberService memberService) {
        this.authService = authService;
        this.memberService = memberService;
    }

    @PostMapping("/login")
    AuthService.TokenResponse login(@RequestBody AuthService.LoginCommand command) {
        return authService.login(command);
    }

    @PostMapping("/register")
    ResponseEntity<Void> register(@RequestBody RegisterMemberCommand command) {
        memberService.register(command);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
