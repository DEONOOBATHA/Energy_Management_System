package com.example.demo.dtos;

import java.util.Objects;
import java.util.UUID;

public class DeviceDTO {
    private UUID id;
    private String name;
    private int maxConsValue;

    public DeviceDTO() {}
    public DeviceDTO(UUID id, String name, int maxConsValue) {
        this.id = id; this.name = name; this.maxConsValue = maxConsValue;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getMaxConsValue() { return maxConsValue; }
    public void setMaxConsValue(int maxConsValue) { this.maxConsValue = maxConsValue; }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DeviceDTO that = (DeviceDTO) o;
        return maxConsValue == that.maxConsValue && Objects.equals(name, that.name);
    }
    @Override public int hashCode() { return Objects.hash(name, maxConsValue); }
}
