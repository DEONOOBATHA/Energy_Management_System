package com.example.demo.services;


import com.example.demo.config.RabbitMQConfig;
import com.example.demo.dto.DeviceAssociationDeletedDTO;
import com.example.demo.dtos.UserDeviceDTO;
import com.example.demo.dtos.builders.UserDeviceBuilder;
import com.example.demo.entities.Device;
import com.example.demo.entities.Person;
import com.example.demo.entities.UserDevice;
import com.example.demo.handlers.exceptions.model.ResourceNotFoundException;
import com.example.demo.repositories.DeviceRepository;
import com.example.demo.repositories.PersonRepository;
import com.example.demo.repositories.UserDeviceRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserDeviceService {
    private static final Logger LOGGER = LoggerFactory.getLogger(PersonService.class);
    private final UserDeviceRepository userDeviceRepository;
    private final PersonRepository personRepository;
    private final DeviceRepository deviceRepository;
    private final RabbitTemplate rabbitTemplate;

    @Autowired
    public UserDeviceService(UserDeviceRepository userDeviceRepository, PersonRepository personRepository, DeviceRepository deviceRepository, RabbitTemplate rabbitTemplate) {
        this.userDeviceRepository = userDeviceRepository;
        this.personRepository = personRepository;
        this.deviceRepository = deviceRepository;
        this.rabbitTemplate = rabbitTemplate;
    }

    public List<UserDeviceDTO> findUserDevices() {
        List<UserDevice> userDeviceList = userDeviceRepository.findAll();
        return userDeviceList.stream()
                .map(UserDeviceBuilder::toUserDeviceDTO)
                .collect(Collectors.toList());
    }
    public UserDeviceDTO findUserDeviceById(Long id) {
        UserDevice userDevice = userDeviceRepository.findById(id);
        if (userDevice == null) {
            LOGGER.error("UserDevice with id {} was not found in db", id);
            throw new ResourceNotFoundException(UserDevice.class.getSimpleName() + " with id: " + id);
        }
        return UserDeviceBuilder.toUserDeviceDTO(userDevice);
    }
    public List<UserDeviceDTO> findUserDevicesByUserId(UUID userid) {
        List<UserDevice> userDevices = userDeviceRepository.findByUserId(userid);
        if (userDevices.isEmpty()) {
            LOGGER.error("No UserDevices found for user id {}", userid);
            throw new ResourceNotFoundException("No devices found for user: " + userid);
        }
        return userDevices.stream()
                .map(UserDeviceBuilder::toUserDeviceDTO)
                .collect(Collectors.toList());
    }
    public UserDeviceDTO findUserDeviceByUserId(UUID userid) {
        Optional<UserDevice> prosumerOptional = userDeviceRepository.findFirstByUserId(userid);
        if (!prosumerOptional.isPresent()) {
            LOGGER.error("UserDevice with id {} was not found in db", userid);
            throw new ResourceNotFoundException(UserDevice.class.getSimpleName() + " with id: " + userid);
        }
        return UserDeviceBuilder.toUserDeviceDTO(prosumerOptional.get());
    }
    public UserDeviceDTO findUserDeviceByDeviceId(UUID deviceid) {
        Optional<UserDevice> prosumerOptional = userDeviceRepository.findByDeviceId(deviceid);
        if (!prosumerOptional.isPresent()) {
            LOGGER.error("UserDevice with id {} was not found in db", deviceid);
            throw new ResourceNotFoundException(UserDevice.class.getSimpleName() + " with id: " + deviceid);
        }
        return UserDeviceBuilder.toUserDeviceDTO(prosumerOptional.get());
    }
    public Long insert(UserDeviceDTO userDeviceDTO) {
        UserDevice userDevice = UserDeviceBuilder.toEntity(userDeviceDTO,personRepository,deviceRepository);
        userDevice = userDeviceRepository.save(userDevice);
        LOGGER.debug("UserDevice with id {} was inserted in db", userDevice.getId());
        return userDevice.getId();
    }
    @Transactional
    public void update(Long id, UserDeviceDTO userDeviceDTO) {
        UserDevice userDevice = userDeviceRepository.findById(id);
        if (userDevice == null) {
            LOGGER.error("UserDevice with id {} was not found in db", id);
            throw new ResourceNotFoundException(UserDevice.class.getSimpleName() + " with id: " + id);
        }
        Optional<Device> device = deviceRepository.findById(userDeviceDTO.getDeviceid());
        if (!device.isPresent()) {
            LOGGER.error("UserDevice with id {} was not found in db", userDeviceDTO.getDeviceid());
            throw new ResourceNotFoundException(UserDevice.class.getSimpleName() + " with id: " + userDeviceDTO.getDeviceid());
        }
        Device deviceEntity = device.get();
        userDevice.setDevice(deviceEntity);
        Optional<Person> user = personRepository.findById(userDeviceDTO.getUserid());
        if (!user.isPresent()) {
            LOGGER.error("UserDevice with id {} was not found in db", userDeviceDTO.getUserid());
            throw new ResourceNotFoundException(UserDevice.class.getSimpleName() + " with id: " + userDeviceDTO.getUserid());
        }
        Person personEntity = user.get();
        userDevice.setUser(personEntity);
    }
    public void delete(Long id) {
        UserDevice userDevice = userDeviceRepository.findById(id);
        if (userDevice == null) {
            LOGGER.error("UserDevice with id {} was not found in db", id);
            throw new ResourceNotFoundException(UserDevice.class.getSimpleName() + " with id: " + id);
        }
        
        // Publish event to notify monitoring service to delete energy consumption data
        DeviceAssociationDeletedDTO event = new DeviceAssociationDeletedDTO(
            userDevice.getDevice().getId(),
            userDevice.getUser().getId(),
            userDevice.getId()
        );
        rabbitTemplate.convertAndSend(
            RabbitMQConfig.DEVICE_EXCHANGE,
            RabbitMQConfig.DEVICE_ASSOCIATION_DELETED_KEY,
            event
        );
        LOGGER.info("Published device association deleted event for device: {}, user: {}", 
            userDevice.getDevice().getId(), userDevice.getUser().getId());
        
        userDeviceRepository.delete(userDevice);
    }
}
