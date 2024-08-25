package com.project.shopapp.models;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CollectionId;

@Entity
@Table(name="social_accounts")
@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SocialAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "provider",nullable = false, length = 20)
    private String provider;

    @Column(name = "name", length = 150)
    private String name;

    @Column(name = "email", length = 150)
    private String email;

    @Column(name = "provider_id", nullable = false, length = 50)
    private String providerId;
}
