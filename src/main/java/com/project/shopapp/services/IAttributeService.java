package com.project.shopapp.services;

import com.project.shopapp.dtos.AttributeDTO;
import com.project.shopapp.dtos.CategoryDTO;
import com.project.shopapp.exceptions.DataNotFoundException;
import com.project.shopapp.models.Attribute;
import com.project.shopapp.models.Category;

import java.util.List;

public interface IAttributeService {
    Attribute createAttribute(AttributeDTO attributeDTO);

    Attribute getAttributeById(long id);

    List<Attribute> getAllAttributes();

    Attribute updateAttribute(long attributeId, AttributeDTO attributeDTO) throws DataNotFoundException;

    void deleteAttribute(long id);
}
