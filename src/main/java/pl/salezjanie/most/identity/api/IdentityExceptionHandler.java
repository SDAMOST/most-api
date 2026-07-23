package pl.salezjanie.most.identity.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import pl.salezjanie.most.identity.application.MemberNotFoundException;

import java.net.URI;

/**
 * Translates Identity module exceptions into RFC 9457 Problem Detail responses.
 */
@RestControllerAdvice(basePackages = "pl.salezjanie.most.identity.api")
class IdentityExceptionHandler {

    @ExceptionHandler(MemberNotFoundException.class)
    ProblemDetail handleNotFound(MemberNotFoundException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setTitle("Member Not Found");
        problem.setType(URI.create("https://most.salezjanie.pl/errors/member-not-found"));
        return problem;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ProblemDetail handleBadRequest(IllegalArgumentException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, ex.getMessage());
        problem.setTitle("Invalid Request");
        problem.setType(URI.create("https://most.salezjanie.pl/errors/invalid-request"));
        return problem;
    }

    @ExceptionHandler(IllegalStateException.class)
    ProblemDetail handleConflict(IllegalStateException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT, ex.getMessage());
        problem.setTitle("Invalid State Transition");
        problem.setType(URI.create("https://most.salezjanie.pl/errors/invalid-state-transition"));
        return problem;
    }
}
