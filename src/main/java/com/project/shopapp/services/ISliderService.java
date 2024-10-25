package com.project.shopapp.services;

import com.project.shopapp.dtos.SliderDTO;
import com.project.shopapp.models.Product;
import com.project.shopapp.models.Slider;

import java.util.List;

public interface ISliderService {
    Slider createSlider(SliderDTO sliderDTO);
    Slider getSliderById(long sliderId) throws Exception;
    Slider uploadImages(long id,SliderDTO sliderDTO) throws Exception;
    Slider updateSlider(long id,SliderDTO sliderDTO) throws Exception;
    void deleteSlider(long id);
    void deleteFile(String filename);
    List<Slider> getAllSliders();
}
