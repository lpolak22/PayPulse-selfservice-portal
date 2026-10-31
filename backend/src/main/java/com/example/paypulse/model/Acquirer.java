package com.example.paypulse.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "acquirer")
public class Acquirer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_acquirer")
    private Long idAcquirer;

    @Column(nullable = false, length = 20, unique = true)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    public Acquirer() {
    }

    public Acquirer(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public Long getIdAcquirer() {
        return idAcquirer;
    }

    public void setIdAcquirer(Long idAcquirer) {
        this.idAcquirer = idAcquirer;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
