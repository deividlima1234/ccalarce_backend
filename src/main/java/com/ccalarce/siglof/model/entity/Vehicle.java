package com.ccalarce.siglof.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "vehicles")
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    @jakarta.validation.constraints.NotNull(message = "Plate is required")
    @jakarta.validation.constraints.NotBlank(message = "Plate cannot be empty")
    private String plate;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "gps_device_id")
    private String gpsDeviceId;

    private String brand;
    private String model;

    @Column(nullable = false)
    @jakarta.validation.constraints.NotNull(message = "Capacity is required")
    @jakarta.validation.constraints.Min(value = 1, message = "Capacity must be greater than 0")
    private Integer capacity; // Max cylinders capacity

    @Column(nullable = false)
    private Boolean active;

    @PrePersist
    protected void onCreate() {
        if (this.active == null)
            this.active = true;
    }
}
