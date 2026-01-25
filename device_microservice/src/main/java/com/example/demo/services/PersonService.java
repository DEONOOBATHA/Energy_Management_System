package com.example.demo.services;


import com.example.demo.dtos.PersonDTO;
import com.example.demo.dtos.builders.PersonBuilder;
import com.example.demo.entities.Person;
import com.example.demo.handlers.exceptions.model.ResourceNotFoundException;
import com.example.demo.repositories.PersonRepository;
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
public class PersonService {
    private static final Logger LOGGER = LoggerFactory.getLogger(PersonService.class);
    private final PersonRepository personRepository;

    @Autowired
    public PersonService(PersonRepository personRepository) {
        this.personRepository = personRepository;
    }

    public List<PersonDTO> findPersons() {
        List<Person> personList = personRepository.findAll();
        return personList.stream()
                .map(PersonBuilder::toPersonDTO)
                .collect(Collectors.toList());
    }
    public PersonDTO findPersonById(UUID id) {
        Optional<Person> prosumerOptional = personRepository.findById(id);
        if (!prosumerOptional.isPresent()) {
            LOGGER.error("Person with id {} was not found in db", id);
            throw new ResourceNotFoundException(Person.class.getSimpleName() + " with id: " + id);
        }
        return PersonBuilder.toPersonDTO(prosumerOptional.get());
    }
    public UUID insert(PersonDTO personDTO) {
        Person person = PersonBuilder.toEntity(personDTO);
        if (person.getId() == null) {
            person.setId(UUID.randomUUID());
        } else if (personRepository.existsById(person.getId())) {
            Person existing = personRepository.findById(person.getId()).get();
            existing.setName(person.getName());
            personRepository.save(existing);
            LOGGER.debug("Person with id {} already exists; updated name", existing.getId());
            return existing.getId();
        }
        person = personRepository.save(person);
        LOGGER.debug("Person with id {} was inserted in db", person.getId());
        return person.getId();
    }
    @Transactional
    public void update(UUID id, PersonDTO personDTO) {
        Optional<Person> prosumerOptional = personRepository.findById(id);
        if (!prosumerOptional.isPresent()) {
            LOGGER.error("Person with id {} was not found in db", id);
            throw new ResourceNotFoundException(Person.class.getSimpleName() + " with id: " + id);
        }
        Person prosumer = prosumerOptional.get();
        prosumer.setName(personDTO.getName());
    }
    public void delete(UUID id) {
        Optional<Person> prosumerOptional = personRepository.findById(id);
        if (!prosumerOptional.isPresent()) {
            LOGGER.error("Person with id {} was not found in db", id);
            throw new ResourceNotFoundException(Person.class.getSimpleName() + " with id: " + id);
        }
        personRepository.delete(prosumerOptional.get());
    }
}
