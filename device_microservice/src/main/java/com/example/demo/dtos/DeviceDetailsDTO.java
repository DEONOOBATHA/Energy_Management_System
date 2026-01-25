package com.example.demo.dtos;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Objects;
import java.util.UUID;

public class DeviceDetailsDTO {

    private UUID id;

    @NotBlank(message = "name is required")
    private String name;

    @NotNull
    private Integer maxConsValue;

    public DeviceDetailsDTO() {
    }

    public DeviceDetailsDTO(String name, Integer maxConsValue) {
        this.name = name;
        this.maxConsValue = maxConsValue;
    }

    public DeviceDetailsDTO(UUID id, String name, Integer maxConsValue) {
        this.id = id;
        this.name = name;
        this.maxConsValue = maxConsValue;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getMaxConsValue() {
        return maxConsValue;
    }

    public void setMaxConsValue(int maxConsValue) {
        this.maxConsValue = maxConsValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DeviceDetailsDTO that = (DeviceDetailsDTO) o;
        return Objects.equals(maxConsValue, that.maxConsValue) &&
                Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, maxConsValue);
    }
}
