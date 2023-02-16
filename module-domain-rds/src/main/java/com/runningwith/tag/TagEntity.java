package com.runningwith.tag;

import jakarta.persistence.*;
import lombok.*;

@EqualsAndHashCode(of = "id")
@AllArgsConstructor
@Builder
@NoArgsConstructor
@Entity
@Table(name = "tag")
@Getter
public class TagEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tag", nullable = false)
    private Long id;

    @Column(unique = true, nullable = false)
    private String title;
}
