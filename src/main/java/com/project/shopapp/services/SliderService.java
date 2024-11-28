package com.project.shopapp.services;

import com.project.shopapp.components.LocalizationUtils;
import com.project.shopapp.dtos.SliderDTO;
import com.project.shopapp.exceptions.DataNotFoundException;
import com.project.shopapp.models.LinkType;
import com.project.shopapp.models.Slider;
import com.project.shopapp.repositories.SliderRepository;
import com.project.shopapp.utils.MessageKeys;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class SliderService implements ISliderService{
    private final SliderRepository sliderRepository;
    private final ModelMapper modelMapper;
    private final LocalizationUtils localizationUtils;

    @Override
    public Slider createSlider(SliderDTO sliderDTO) {
        Slider slider=Slider.builder()
                .title(sliderDTO.getTitle())
                .imageUrl(sliderDTO.getImageUrl())
                .linkType(sliderDTO.getLinkType())
                .categoryId(sliderDTO.getCategoryId())
                .productId(sliderDTO.getProductId())
                .build();
        return sliderRepository.save(slider);
    }

    @Override
    public Slider getSliderById(long id)  {
        return sliderRepository.findById(id).orElseThrow(
                ()->new DataNotFoundException("Slider not found")
        );
    }

    @Override
    public Slider uploadImages(long id, SliderDTO sliderDTO) throws Exception {
        // Tìm slider hiện tại từ database
        Slider existingSlider = getSliderById(id);

        existingSlider.setImageUrl(sliderDTO.getImageUrl());
//        existingSlider.setTitle(sliderDTO.getTitle());
//        existingSlider.setLinkType(sliderDTO.getLinkType());
//        existingSlider.setCategoryId(sliderDTO.getCategoryId());
//        existingSlider.setProductId(sliderDTO.getProductId());
        // Lưu lại đối tượng đã được cập nhật
        return sliderRepository.save(existingSlider);
    }

    @Override
    public Slider updateSlider(long id, SliderDTO sliderDTO) throws Exception {
        Slider existingSlider = getSliderById(id);
        if(existingSlider !=null){
            existingSlider.setTitle(sliderDTO.getTitle());
            existingSlider.setLinkType(sliderDTO.getLinkType());
            existingSlider.setCategoryId(sliderDTO.getCategoryId());
            existingSlider.setProductId(sliderDTO.getProductId());
            return sliderRepository.save(existingSlider);
        }
        return null;
    }

    @Override
    public void deleteSlider(long id) {
        Slider slider=sliderRepository.findById(id).orElseThrow(
                ()->new DataNotFoundException(localizationUtils.getLocalizedMessage(MessageKeys.CAN_NOT_FIND_SLIDER))
        );
        deleteFile(slider.getImageUrl());
        sliderRepository.delete(slider);
    }

    @Override
    public void deleteFile(String filename) {
        try {
            Path filePath = Paths.get("sliders").resolve(filename);
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public List<Slider> getAllSliders() {
        return sliderRepository.findAll();
    }
}
