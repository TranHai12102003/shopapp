    package com.project.shopapp.controllers;

    import com.project.shopapp.components.LocalizationUtils;
    import com.project.shopapp.dtos.SliderDTO;
    import com.project.shopapp.models.LinkType;
    import com.project.shopapp.models.Product;
    import com.project.shopapp.models.Slider;
    import com.project.shopapp.responses.DeleteSliderResponse;
    import com.project.shopapp.responses.ProductListResponse;
    import com.project.shopapp.responses.ProductResponse;
    import com.project.shopapp.services.ProductService;
    import com.project.shopapp.services.SliderService;
    import com.project.shopapp.utils.MessageKeys;
    import jakarta.validation.Valid;
    import lombok.RequiredArgsConstructor;
    import org.springframework.core.io.UrlResource;
    import org.springframework.data.domain.Page;
    import org.springframework.data.domain.PageRequest;
    import org.springframework.data.domain.Sort;
    import org.springframework.http.HttpStatus;
    import org.springframework.http.MediaType;
    import org.springframework.http.ResponseEntity;
    import org.springframework.util.StringUtils;
    import org.springframework.validation.BindingResult;
    import org.springframework.validation.FieldError;
    import org.springframework.web.bind.annotation.*;
    import org.springframework.web.multipart.MultipartFile;
    import java.io.IOException;
    import java.nio.file.Files;
    import java.nio.file.Paths;
    import java.nio.file.StandardCopyOption;
    import java.util.List;
    import java.util.Objects;
    import java.util.UUID;

    @RestController
    @RequiredArgsConstructor
    @RequestMapping("${api.prefix}/sliders")
    public class SliderController {
        private final SliderService sliderService;
        private final LocalizationUtils localizationUtils;
        private final ProductService productService;

        @PostMapping("")
        public ResponseEntity<?> createSlider(
                @Valid @RequestBody SliderDTO sliderDTO,
                BindingResult result) {
            try
            {
                if(result.hasErrors()){
                    List<String> errorMessages= result.getFieldErrors()
                            .stream()
                            .map(FieldError::getDefaultMessage)
                            .toList();
                    return ResponseEntity.badRequest().body(errorMessages);
                }
                Slider slider=sliderService.createSlider(sliderDTO);

                return ResponseEntity.ok(slider);
            }catch (Exception e){
                return ResponseEntity.badRequest()
                        .body( localizationUtils
                                .getLocalizedMessage(MessageKeys.INSERT_SLIDER_FAILED,e.getMessage()));
            }
        }

        @GetMapping("")
        public ResponseEntity<List<Slider>> getAllSliders(
                //@RequestParam("page") đây là tham số ở client
                //int page đây là tham số của server
                @RequestParam(defaultValue = "0") int page,
                @RequestParam(defaultValue = "10") int limit){
            List<Slider> sliders=sliderService.getAllSliders();
            return ResponseEntity.ok().body(sliders);

        }

        @GetMapping("/{id}")
        public Slider getSliderById(@PathVariable("id") Long id){
            return sliderService.getSliderById(id);
        }

        @GetMapping("/{id}/action")
        public ResponseEntity<?> handleSliderAction(@PathVariable Long id,
                                                    @RequestParam(defaultValue = "") String keyword,
                                                    @RequestParam(defaultValue = "0") int page,
                                                    @RequestParam(defaultValue = "10") int limit) throws Exception {
            //Lay slider theo id
            Slider slider = sliderService.getSliderById(id);
            if(slider == null){
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(localizationUtils.getLocalizedMessage(MessageKeys.CAN_NOT_FIND_SLIDER));
            }

            //kiem tra kieu lien ket (category hay product)
            if(slider.getLinkType() == LinkType.category){
                //Tao pageRequest voi trang va kich thuoc tu request
                PageRequest pageRequest=PageRequest.of(page,limit,
                        Sort.by("createdAt").descending());
                //tra ve danh sach san pham thuoc danh muc do
                Page<ProductResponse> productPage=productService
                        .getProductsByCategoryId(slider.getCategoryId(), pageRequest);
                //lay so trang
                int totalPage=productPage.getTotalPages();
                List<ProductResponse> products=productPage.getContent();
                return ResponseEntity.ok(ProductListResponse.builder()
                                .products(products)
                                .totalPages(totalPage)
                                .build());
            }else if(slider.getLinkType() == LinkType.product) {
                Product product = productService.getProductById(slider.getProductId());
                return ResponseEntity.ok(ProductResponse.fromProduct(product));
            }else if(slider.getLinkType() == LinkType.all_products) {
                //Tao pageRequest voi trang va kich thuoc tu request
                PageRequest pageRequest=PageRequest.of(page,limit,
                        Sort.by("createdAt").descending());
                //tra ve danh sach san pham thuoc danh muc do
                Page<ProductResponse> productPage=productService
                        .getAllProducts(keyword,slider.getCategoryId(), pageRequest);
                //lay so trang
                int totalPage=productPage.getTotalPages();
                List<ProductResponse> products=productPage.getContent();
                return ResponseEntity.ok(ProductListResponse.builder()
                        .products(products)
                        .totalPages(totalPage)
                        .build());
            }else {
                 return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid link type");
            }
        }

        @GetMapping("/images/{imageName}")
        public ResponseEntity<?> viewImage(@PathVariable String imageName) {
            try {
                java.nio.file.Path imagePath = Paths.get("sliders/"+imageName);
                UrlResource resource = new UrlResource(imagePath.toUri());

                if (resource.exists()) {
                    return ResponseEntity.ok()
                            .contentType(MediaType.IMAGE_JPEG)
                            .body(resource);
                } else {
                    return ResponseEntity.ok()
                            .contentType(MediaType.IMAGE_JPEG)
                            .body(new UrlResource(Paths.get("uploads/notfound.jpg").toUri()));
                    //return ResponseEntity.notFound().build();
                }
            } catch (Exception e) {
                return ResponseEntity.notFound().build();
            }
        }

        @PostMapping(value="uploads/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        public ResponseEntity<?> uploadImages(@PathVariable("id") Long sliderId,
                                              @RequestParam("file") MultipartFile file){
            try {
                Slider existingSlider=sliderService.getSliderById(sliderId);
                    if(file.getSize()>10*1024*1024){//kích thước lớn hơn 10 MB
                        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                                .body(localizationUtils.getLocalizedMessage(MessageKeys.UPLOAD_IMAGES_FILE_LARGE));
                    }
                    String contentType=file.getContentType();
                    if(contentType == null || !contentType.startsWith("image/")){
                        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                                .body(localizationUtils.getLocalizedMessage(MessageKeys.UPLOAD_IMAGES_FILE_MUST_BE_IMAGE));
                    }
                    //Lưu  file và cập nhật thumbnail trong DTO
                    String filename=storeFile(file);
                    Slider updateSlider=sliderService.uploadImages(existingSlider.getId(),
                            SliderDTO.builder()
                                    .title(existingSlider.getTitle())
                                    .linkType(existingSlider.getLinkType())
                                    .imageUrl(filename)
                                    .categoryId(existingSlider.getCategoryId())
                                    .productId(existingSlider.getProductId())
                                    .build());

                return ResponseEntity.ok().body(updateSlider);
            } catch (Exception e) {
                return ResponseEntity.badRequest().body(e.getMessage());
            }
        }

        // Hàm kiểm tra định dạng file có hợp lệ không
        public String storeFile(MultipartFile file) throws IOException{
            if(!isImageFile(file) || file.getOriginalFilename() == null){
                throw new IOException("Invalid image format");
            }
            String filename= StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
            //Thêm UUID vào trước tên file để đảm bảo tên file là duy nhất
            String uniqueFilename = UUID.randomUUID().toString()+"_"+filename;
            //Đường dẫn đến thư mục mà bạn muốn lưu file
            java.nio.file.Path uploadDir = Paths.get("sliders");
            //kiểm tra và tạo thư mục nếu nó chưa tồn tại
            if(!Files.exists(uploadDir)){
                Files.createDirectories(uploadDir);
            }
            //Đường dẫn đầy đủ đến file
            java.nio.file.Path destination = Paths.get(uploadDir.toString(),uniqueFilename);
            //Sao chép file vào thư mục đích
            Files.copy(file.getInputStream(),destination, StandardCopyOption.REPLACE_EXISTING);
            return uniqueFilename;

        }
        private boolean isImageFile(MultipartFile file){
            String contentType=file.getContentType();
            return contentType != null && contentType.startsWith("image/");
        }

        @PutMapping("/update/{id}")
        public ResponseEntity<?> updateSlider(@PathVariable("id") long id,@RequestBody SliderDTO sliderDTO){
            try{
                Slider slider  = sliderService.updateSlider(id,sliderDTO);
                return ResponseEntity.ok().body(slider);
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
            }
        }

        @DeleteMapping("/{id}")
        public ResponseEntity<DeleteSliderResponse> deleteSlider(@PathVariable("id") long id){
            try {
                Slider slider=sliderService.getSliderById(id);
                if(slider != null){
                    sliderService.deleteSlider(slider.getId());
                    return ResponseEntity.ok(DeleteSliderResponse.builder()
                                    .message(localizationUtils.getLocalizedMessage(MessageKeys.DELETE_SLIDER_SUCCESSFULLY,id))
                            .build());
                }
            } catch (Exception e) {
                return ResponseEntity.badRequest().body(DeleteSliderResponse.builder()
                        .message(localizationUtils.getLocalizedMessage(MessageKeys.DELETE_SLIDER_SUCCESSFULLY,id))
                        .build());
            }
            return null;
        }
    }
