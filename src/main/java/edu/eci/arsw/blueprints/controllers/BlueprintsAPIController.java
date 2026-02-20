package edu.eci.arsw.blueprints.controllers;

import edu.eci.arsw.blueprints.model.Blueprint;
import edu.eci.arsw.blueprints.model.Point;
import edu.eci.arsw.blueprints.persistence.BlueprintNotFoundException;
import edu.eci.arsw.blueprints.persistence.BlueprintPersistenceException;
import edu.eci.arsw.blueprints.services.BlueprintsServices;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/api/v1/blueprints")
public class BlueprintsAPIController {

    private final BlueprintsServices services;

    public BlueprintsAPIController(BlueprintsServices services) { this.services = services; }

    // GET /api/v1/blueprints
    @io.swagger.v3.oas.annotations.Operation(summary = "Get all blueprints")
    @GetMapping
    public ResponseEntity<ApiResponse<Set<Blueprint>>> getAll() {
        Set<Blueprint> data = services.getAllBlueprints();
        return ResponseEntity.ok(new ApiResponse<>(200, "execute ok", data));
    }

    // GET /api/v1/blueprints/{author}
    @io.swagger.v3.oas.annotations.Operation(summary = "Get blueprints by author")
    @GetMapping("/{author}")
    public ResponseEntity<?> byAuthor(@PathVariable String author) {
        try {
            Set<Blueprint> data = services.getBlueprintsByAuthor(author);
            return ResponseEntity.ok(new ApiResponse<>(200, "execute ok", data));
        } catch (BlueprintNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(404, e.getMessage(), null));
        }
    }

    // GET /api/v1/blueprints/{author}/{bpname}
    @io.swagger.v3.oas.annotations.Operation(summary = "Get a blueprint by author and name")
    @GetMapping("/{author}/{bpname}")
    public ResponseEntity<?> byAuthorAndName(@PathVariable String author, @PathVariable String bpname) {
        try {
            Blueprint data = services.getBlueprint(author, bpname);
            return ResponseEntity.ok(new ApiResponse<>(200, "execute ok", data));
        } catch (BlueprintNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(404, e.getMessage(), null));
        }
    }

    // POST /api/v1/blueprints
    @io.swagger.v3.oas.annotations.Operation(summary = "Create a new blueprint")
    @PostMapping
    public ResponseEntity<ApiResponse<Void>> add(@Valid @RequestBody NewBlueprintRequest req) {
        try {
            Blueprint bp = new Blueprint(req.author(), req.name(), req.points());
            services.addNewBlueprint(bp);
            return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(201, "created", null));
        } catch (BlueprintPersistenceException e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(400, e.getMessage(), null));
        }
    }

    // PUT /api/v1/blueprints/{author}/{bpname}/points
    @io.swagger.v3.oas.annotations.Operation(summary = "Add a point to an existing blueprint")
    @PutMapping("/{author}/{bpname}/points")
    public ResponseEntity<ApiResponse<Void>> addPoint(@PathVariable String author, @PathVariable String bpname,
                                      @RequestBody Point p) {
        try {
            services.addPoint(author, bpname, p.x(), p.y());
            return ResponseEntity.accepted().body(new ApiResponse<>(202, "accepted", null));
        } catch (BlueprintNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(404, e.getMessage(), null));
        }
    }

    public record NewBlueprintRequest(
            @NotBlank String author,
            @NotBlank String name,
            @Valid java.util.List<Point> points
    ) { }
}
