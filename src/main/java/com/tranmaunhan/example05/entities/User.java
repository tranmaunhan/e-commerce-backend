package com.tranmaunhan.example05.entities;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class User {

      @Id
      @GeneratedValue(strategy = GenerationType.IDENTITY)
      private Long userId;

      private String firstName;

      private String lastName;

      private String mobileNumber;

      @Column(unique = true, nullable = false)
      private String email;

      @Column(nullable = false)
      private String password;

      @ManyToMany(fetch = FetchType.EAGER)
      @JoinTable(name = "user_role", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "role_id"))
      private Set<Role> roles = new HashSet<>();

      @ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE }, fetch = FetchType.LAZY)
      @JoinTable(name = "user_address", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "address_id"))
      private List<Address> addresses = new ArrayList<>();

      @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
      private Cart cart;
}
