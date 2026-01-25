package com.example.demo.services;


import com.example.demo.dto.DeviceEventDTO;
import com.example.demo.dtos.DeviceDTO;
import com.example.demo.dtos.DeviceDetailsDTO;
import com.example.demo.dtos.builders.DeviceBuilder;
import com.example.demo.entities.Device;
import com.example.demo.handlers.exceptions.model.ResourceNotFoundException;
import com.example.demo.publisher.DeviceEventPublisher;
import com.example.demo.repositories.DeviceRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DeviceService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DeviceService.class);
    private final DeviceRepository deviceRepository;
    private final DeviceEventPublisher deviceEventPublisher;

    @Autowired
    public DeviceService(DeviceRepository deviceRepository, DeviceEventPublisher deviceEventPublisher) {
        this.deviceRepository = deviceRepository;
        this.deviceEventPublisher = deviceEventPublisher;
    }

    public List<DeviceDTO> findDevices() {
        List<Device> personList = deviceRepository.findAll();
        return personList.stream()
                .map(DeviceBuilder::toDeviceDTO)
                .collect(Collectors.toList());
    }
    public DeviceDetailsDTO findDeviceById(UUID id) {
        Optional<Device> prosumerOptional = deviceRepository.findById(id);
        if (!prosumerOptional.isPresent()) {
            LOGGER.error("Device with id {} was not found in db", id);
            throw new ResourceNotFoundException(Device.class.getSimpleName() + " with id: " + id);
        }
        return DeviceBuilder.toDeviceDetailsDTO(prosumerOptional.get());
    }
    public UUID insert(DeviceDetailsDTO deviceDTO) {
        Device device = DeviceBuilder.toEntity(deviceDTO);
        device = deviceRepository.save(device);
        LOGGER.debug("Device with id {} was inserted in db", device.getId());
        
        // Publish device created event
        DeviceEventDTO event = new DeviceEventDTO("CREATED", device.getId(), device.getName(), device.getMaxConsValue());
        deviceEventPublisher.publishDeviceCreated(event);
        
        return device.getId();
    }
    @Transactional
    public void update(UUID id, DeviceDetailsDTO deviceDTO) {
        Optional<Device> prosumerOptional = deviceRepository.findById(id);
        if (!prosumerOptional.isPresent()) {
            LOGGER.error("Device with id {} was not found in db", id);
            throw new ResourceNotFoundException(Device.class.getSimpleName() + " with id: " + id);
        }
        Device prosumer = prosumerOptional.get();
        prosumer.setName(deviceDTO.getName());
        prosumer.setMaxConsValue(deviceDTO.getMaxConsValue());
        
        // Publish device updated event
        DeviceEventDTO event = new DeviceEventDTO("UPDATED", id, prosumer.getName(), prosumer.getMaxConsValue());
        deviceEventPublisher.publishDeviceUpdated(event);
    }
    public void delete(UUID id) {
        Optional<Device> prosumerOptional = deviceRepository.findById(id);
        if (!prosumerOptional.isPresent()) {
            LOGGER.error("Device with id {} was not found in db", id);
            throw new ResourceNotFoundException(Device.class.getSimpleName() + " with id: " + id);
        }
        Device device = prosumerOptional.get();
        deviceRepository.delete(device);
        
        // Publish device deleted event
        DeviceEventDTO event = new DeviceEventDTO("DELETED", id, null, null);
        deviceEventPublisher.publishDeviceDeleted(event);
    }
}
