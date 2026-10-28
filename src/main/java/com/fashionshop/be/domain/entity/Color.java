package com.fashionshop.be.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "colors")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Color extends BaseEntity {

    @Column(nullable = false, unique = true, length = 80)
    private String name;

    @Column(name = "hex_code", nullable = false, length = 20)
    @Builder.Default
    private String hexCode = "#221f20";
}
