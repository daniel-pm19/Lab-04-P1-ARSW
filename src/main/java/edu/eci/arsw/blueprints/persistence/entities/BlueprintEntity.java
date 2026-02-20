package edu.eci.arsw.blueprints.persistence.entities;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "blueprints", uniqueConstraints = @UniqueConstraint(columnNames = {"author", "name"}))
public class BlueprintEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String author;
    private String name;

    @OneToMany(mappedBy = "blueprint", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PointEntity> points = new ArrayList<>();

    public BlueprintEntity() { }

    public BlueprintEntity(String author, String name) {
        this.author = author;
        this.name = name;
    }

    public Long getId() { return id; }
    public String getAuthor() { return author; }
    public String getName() { return name; }
    public List<PointEntity> getPoints() { return points; }

    public void addPoint(PointEntity p) {
        points.add(p);
        p.setBlueprint(this);
    }

    public void setPoints(List<PointEntity> pts) {
        points.clear();
        for (PointEntity p : pts) addPoint(p);
    }
}
