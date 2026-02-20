package edu.eci.arsw.blueprints.persistence;

import edu.eci.arsw.blueprints.model.Blueprint;
import edu.eci.arsw.blueprints.model.Point;
import edu.eci.arsw.blueprints.persistence.entities.BlueprintEntity;
import edu.eci.arsw.blueprints.persistence.entities.PointEntity;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
@Profile("postgres")
public class PostgresBlueprintPersistence implements BlueprintPersistence {

    private final BlueprintRepository repo;

    public PostgresBlueprintPersistence(BlueprintRepository repo) { this.repo = repo; }

    @Override
    public void saveBlueprint(Blueprint bp) throws BlueprintPersistenceException {
        if (repo.findByAuthorAndName(bp.getAuthor(), bp.getName()).isPresent()) {
            throw new BlueprintPersistenceException("Blueprint already exists: %s:%s".formatted(bp.getAuthor(), bp.getName()));
        }
        BlueprintEntity ent = new BlueprintEntity(bp.getAuthor(), bp.getName());
        List<PointEntity> pts = bp.getPoints().stream().map(p -> new PointEntity(p.x(), p.y())).toList();
        ent.setPoints(pts);
        repo.save(ent);
    }

    @Override
    public Blueprint getBlueprint(String author, String name) throws BlueprintNotFoundException {
        return repo.findByAuthorAndName(author, name)
                .map(this::toModel)
                .orElseThrow(() -> new BlueprintNotFoundException("Blueprint not found: %s/%s".formatted(author, name)));
    }

    @Override
    public Set<Blueprint> getBlueprintsByAuthor(String author) throws BlueprintNotFoundException {
        List<BlueprintEntity> list = repo.findByAuthor(author);
        if (list.isEmpty()) throw new BlueprintNotFoundException("No blueprints for author: " + author);
        return list.stream().map(this::toModel).collect(Collectors.toSet());
    }

    @Override
    public Set<Blueprint> getAllBlueprints() {
        return repo.findAll().stream().map(this::toModel).collect(Collectors.toSet());
    }

    @Override
    public void addPoint(String author, String name, int x, int y) throws BlueprintNotFoundException {
        BlueprintEntity entity = repo.findByAuthorAndName(author, name)
                .orElseThrow(() -> new BlueprintNotFoundException("Blueprint not found: %s/%s".formatted(author, name)));
        PointEntity p = new PointEntity(x, y);
        entity.addPoint(p);
        repo.save(entity);
    }

    private Blueprint toModel(BlueprintEntity e) {
        List<Point> pts = e.getPoints().stream().map(pe -> new Point(pe.getX(), pe.getY())).collect(Collectors.toList());
        return new Blueprint(e.getAuthor(), e.getName(), pts);
    }
}
