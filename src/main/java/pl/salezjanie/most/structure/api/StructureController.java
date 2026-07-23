package pl.salezjanie.most.structure.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.salezjanie.most.structure.application.AssignLeaderCommand;
import pl.salezjanie.most.structure.application.CreateUnitCommand;
import pl.salezjanie.most.structure.application.LeadershipAssignmentView;
import pl.salezjanie.most.structure.application.OrganizationUnitNode;
import pl.salezjanie.most.structure.application.StructureService;

import java.util.List;
import java.util.UUID;

/**
 * REST API for the Structure bounded context.
 */
@RestController
@RequestMapping("/api/structure")
class StructureController {

    private final StructureService service;

    StructureController(StructureService service) {
        this.service = service;
    }

    /**
     * Returns the complete organizational tree with active leaders.
     */
    @GetMapping("/tree")
    List<OrganizationUnitNode> getTree() {
        return service.getTree();
    }

    /**
     * Creates a new organizational unit.
     */
    @PostMapping("/units")
    ResponseEntity<OrganizationUnitNode> createUnit(@RequestBody CreateUnitCommand command) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createUnit(command));
    }

    /**
     * Assigns a leader to an organizational unit.
     */
    @PostMapping("/units/{unitId}/leaders")
    ResponseEntity<LeadershipAssignmentView> assignLeader(@PathVariable UUID unitId,
                                                          @RequestBody AssignLeaderCommand command) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.assignLeader(unitId, command));
    }
}
