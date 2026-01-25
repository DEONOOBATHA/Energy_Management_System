package com.example.demo.dtos.builders;

import com.example.demo.dtos.PersonDTO;
import com.example.demo.dtos.UserDeviceDTO;
import com.example.demo.entities.Device;
import com.example.demo.entities.Person;
import com.example.demo.entities.UserDevice;
import com.example.demo.repositories.DeviceRepository;
import com.example.demo.repositories.PersonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

public class UserDeviceBuilder {

    private UserDeviceBuilder() {
    }

    public static UserDeviceDTO toUserDeviceDTO(UserDevice userDevice) {
        return new UserDeviceDTO(userDevice.getId(), userDevice.getUser().getId(), userDevice.getDevice().getId());
    }

    public static UserDevice toEntity(UserDeviceDTO userDeviceDTO,
                                      PersonRepository personRepository,
                                      DeviceRepository deviceRepository) {

        if (userDeviceDTO.getUserid() == null || userDeviceDTO.getDeviceid() == null) {
            throw new IllegalArgumentException("UserId and DeviceId must not be null");
        }

        Person person = personRepository.findById(userDeviceDTO.getUserid())
                .orElseThrow(() -> new RuntimeException("User not found: " + userDeviceDTO.getUserid()));
        Device device = deviceRepository.findById(userDeviceDTO.getDeviceid())
                .orElseThrow(() -> new RuntimeException("Device not found: " + userDeviceDTO.getDeviceid()));

        return new UserDevice(person, device);
    }
}
