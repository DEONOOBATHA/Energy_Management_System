package com.example.demo.dtos.builders;

import com.example.demo.dtos.DeviceDetailsDTO;
import com.example.demo.dtos.PersonDTO;
import com.example.demo.entities.Device;
import com.example.demo.entities.Person;

public class PersonBuilder {

    private PersonBuilder() {
    }

    public static PersonDTO toPersonDTO(Person person) {
        return new PersonDTO(person.getId(), person.getName());
    }
    public static Person toEntity(PersonDTO personDTO) {
        Person person = new Person(personDTO.getName());
        if (personDTO.getId() != null) {
            person.setId(personDTO.getId());
        }
        return person;
    }
}
