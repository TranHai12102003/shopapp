package com.project.shopapp.services;

import com.project.shopapp.components.LocalizationUtils;
import com.project.shopapp.dtos.AttributeDTO;
import com.project.shopapp.exceptions.DataNotFoundException;
import com.project.shopapp.models.Attribute;
import com.project.shopapp.repositories.AttributeRepository;
import com.project.shopapp.utils.MessageKeys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AttributeService implements IAttributeService {
    private final AttributeRepository attributeRepository;
    private final LocalizationUtils localizationUtils;

    @Override
    public Attribute createAttribute(AttributeDTO attributeDTO) {
        Attribute attribute =Attribute.builder()
                .name(attributeDTO.getName())
                .build();
        return attributeRepository.save(attribute);
    }

    @Override
    public Attribute getAttributeById(long id) {
        return attributeRepository.findById(id)
                .orElseThrow(()-> new DataNotFoundException(localizationUtils.getLocalizedMessage(MessageKeys.ATTRIBUTE_NOT_FOUND)));
    }

    @Override
    public List<Attribute> getAllAttributes() {
        return attributeRepository.findAll();
    }

    @Override
    public Attribute updateAttribute(long attributeId, AttributeDTO attributeDTO) throws DataNotFoundException {
        Attribute existingattribute=getAttributeById(attributeId);
        if(existingattribute==null){
            throw new DataNotFoundException(localizationUtils.getLocalizedMessage(MessageKeys.ATTRIBUTE_NOT_FOUND));
        }
        existingattribute.setName(attributeDTO.getName());
        attributeRepository.save(existingattribute);
        return existingattribute;
    }

    @Override
    public void deleteAttribute(long id) {
        attributeRepository.deleteById(id);
    }
}
