package tech.oliver.safenotes.entity;

import jakarta.persistence.*;

import java.util.Set;

@Entity
@Table(name = "tb_scopes")
public class Scope {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "scopes_id", nullable = false)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    public Scope() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
