package com.example.paypulse.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "mcc_code")
public class MccCode {
    @Id
    @Column(name = "mcc_code", length = 4, columnDefinition = "VARCHAR(4)")
    private String mccCode;

    @Column(nullable = false, length = 255)
    private String description;

    public MccCode() {
    }

    public MccCode(String mccCode, String description) {
        this.mccCode = mccCode;
        this.description = description;
    }

    public String getMccCode() {
        return mccCode;
    }

    public void setMccCode(String mccCode) {
        this.mccCode = mccCode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
