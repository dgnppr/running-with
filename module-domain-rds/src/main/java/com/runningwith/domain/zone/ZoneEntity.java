package com.runningwith.domain.zone;

import jakarta.persistence.*;
import lombok.*;

import static lombok.AccessLevel.PROTECTED;

@EqualsAndHashCode(of = "id")
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = PROTECTED)
@Getter
@Table(name = "zone", uniqueConstraints = @UniqueConstraint(columnNames = {"city", "province"}))
@Entity
public class ZoneEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_zone", nullable = false)
    private Long id;

    @Column(nullable = false)
    private String city;

    @Column(nullable = false, name = "local_name_of_city")
    private String localNameOfCity;

    @Column
    private String province;

    public ZoneEntity(String city, String localNameOfCity, String province) {
        this.city = city;
        this.localNameOfCity = localNameOfCity;
        this.province = province;
    }

    @Override
    public String toString() {
        return String.format("%s(%s)/%s", city, localNameOfCity, province);
    }
}

