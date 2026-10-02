package tech.oliver.safenotes.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "tb_roles")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "role_id", nullable = false)
    private  Long id;

    @Column(name = "name", nullable = false)
    private String name;
}
