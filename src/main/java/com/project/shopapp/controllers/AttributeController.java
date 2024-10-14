package com.project.shopapp.controllers;

import com.project.shopapp.components.LocalizationUtils;
import com.project.shopapp.dtos.AttributeDTO;
import com.project.shopapp.dtos.CategoryDTO;
import com.project.shopapp.responses.AttributeResponse;
import com.project.shopapp.responses.CategoryResponse;
import com.project.shopapp.services.AttributeService;
import com.project.shopapp.utils.MessageKeys;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/attributes")
public class AttributeController {
    private final LocalizationUtils localizationUtils;
    private final AttributeService attributeService;
    @PostMapping("")
    public ResponseEntity<AttributeResponse> createAttribute(
            @Valid @RequestBody AttributeDTO attributeDTO,
            BindingResult result){
        //kiểm tra xem có lỗi hay không nếu có sẽ trả về cho client lỗi
        if(result.hasErrors()){
            //nếu tìm thấy lỗi stream() sẽ duyệt qua các lỗi đó
            //và lấy ra lỗi cần thiết sau đó "map()" ánh xạ sang 1 mảng khác và trả về lỗi đó
            List<String> errorMessages= result.getFieldErrors()
                    .stream()
                    .map(FieldError::getDefaultMessage)
                    .toList();
            return ResponseEntity.badRequest().body(AttributeResponse.builder()
                    .message(localizationUtils.getLocalizedMessage(MessageKeys.INSERT_ATTRIBUTE_FAILED))
                    .build());
        }
        attributeService.createAttribute(attributeDTO);
        return ResponseEntity.ok(AttributeResponse.builder()
                .message(localizationUtils.getLocalizedMessage(MessageKeys.INSERT_ATTRIBUTE_SUCCESSFULLY))
                .build());
    }
}
