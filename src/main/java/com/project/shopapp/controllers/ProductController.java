package com.project.shopapp.controllers;

import com.github.javafaker.Faker;
import com.project.shopapp.components.LocalizationUtils;
import com.project.shopapp.dtos.ProductDTO;
import com.project.shopapp.dtos.ProductImageDTO;
import com.project.shopapp.models.Product;
import com.project.shopapp.models.ProductImage;
import com.project.shopapp.responses.DeleteProductResponse;
import com.project.shopapp.responses.ProductListResponse;
import com.project.shopapp.responses.ProductResponse;
import com.project.shopapp.services.IProductService;
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
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("${api.prefix}/products")
@RequiredArgsConstructor
public class ProductController {
    private final IProductService productService;
    private final LocalizationUtils localizationUtils;

    @PostMapping("")
    //POST http://localhost:8080/api/v1/products
    public ResponseEntity<?> createProduct(
            @Valid @RequestBody ProductDTO productDTO,
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
            Product newProduct=productService.createProduct(productDTO);

            return ResponseEntity.ok(newProduct);
        }catch (Exception e){
            return ResponseEntity.badRequest()
                    .body( localizationUtils
                            .getLocalizedMessage(MessageKeys.CREATE_PRODUCT_FAILED,e.getMessage()));
        }
    }

    @PostMapping(value="uploads/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadImages(@PathVariable("id") Long productId,
            @ModelAttribute("files") List<MultipartFile> files){
        try {
            Product existingProduct=productService.getProductById(productId);
            files =files == null ? new ArrayList<>() : files;
            if(files.size() > ProductImage.MAXIMUM_IMAGES_PER_PRODUCT){
                return ResponseEntity.badRequest()
                        .body(localizationUtils.getLocalizedMessage(MessageKeys.UPLOAD_IMAGES_MAX_6));
            }
            List<ProductImage> productImages=new ArrayList<>();
            // kiểm tra xem nó null không nếu có thì tạo ra mảng rỗng
            // ngược lại lấy các phần tử có trong files để duyệt
            // nếu không kiểm tra khi null nó sẽ bị NullPointerException sẽ không cho thêm sản phẩm khi không có ảnh
            //duyệt qua danh sách files và tiến hành kiểm tra từng phần tử
            for (MultipartFile file:files){
                //nếu như biến files bên postman ta để nhưng không upload ảnh
                //thi nó vẫn nhận và file đó có kích thước là 0
                //ta bỏ qua file đó không thực hiện tiếp
                if(file.getSize() == 0){
                    continue;
                }
                //kiểm tra kích thước và định dạng
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
                //lưu vào đối tượng product trong DB
                ProductImage productImage= productService.createProductImage(
                        existingProduct.getId(),
                        ProductImageDTO.builder()
                                .imageUrl(filename)
                                .build());
                productImages.add(productImage);
                //Lưu vào bảng product_images
            }
            return ResponseEntity.ok().body(productImages);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/images/{imageName}")
    public ResponseEntity<?> viewImage(@PathVariable String imageName) {
        try {
            java.nio.file.Path imagePath = Paths.get("uploads/"+imageName);
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

    public String storeFile(MultipartFile file) throws IOException{
        if(!isImageFile(file) || file.getOriginalFilename() == null){
            throw new IOException("Invalid image format");
        }
        String filename= StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        //Thêm UUID vào trước tên file để đảm bảo tên file là duy nhất
        String uniqueFilename = UUID.randomUUID().toString()+"_"+filename;
        //Đường dẫn đến thư mục mà bạn muốn lưu file
        java.nio.file.Path uploadDir = Paths.get("uploads");
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

    @GetMapping("")
    public ResponseEntity<ProductListResponse> getProducts(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0",name = "category_id") Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit

    ){
        //Tao pageable tu thong tin trang va gioi han
        //PageRequest.of(page, size) tạo ra một đối tượng PageRequest
        // với số trang page (bắt đầu từ 0) và limit (số bản ghi mỗi trang).
        PageRequest pageRequest=PageRequest.of(
                page,limit,
                Sort.by("createdAt").descending());
//                Sort.by("id").ascending());
        Page<ProductResponse> productPage=productService.getAllProducts(keyword,categoryId,pageRequest);
        //lay tong so trang
        int totalPages=productPage.getTotalPages();
        List<ProductResponse> products=productPage.getContent();
        return ResponseEntity.ok(ProductListResponse.builder()
                        .products(products)
                        .totalPages(totalPages)
                        .build());
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<ProductListResponse> getProductsByCategoryId
            (@PathVariable Long categoryId,
             @RequestParam(defaultValue = "0") int page,
             @RequestParam(defaultValue = "10") int limit){
        PageRequest pageRequest=PageRequest.of(page,limit,Sort.by("createdAt").descending());
        Page<ProductResponse> productPage=productService.getProductsByCategoryId(categoryId,pageRequest);
        int totalPages=productPage.getTotalPages();
        List<ProductResponse> products=productPage.getContent();
        return ResponseEntity.ok(ProductListResponse.builder()
                .products(products)
                .totalPages(totalPages)
                .build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getProductId(@PathVariable("id") Long productId){
        try {
           Product existingProduct= productService.getProductById(productId);
            return ResponseEntity.ok(ProductResponse.fromProduct(existingProduct));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/latest")
    public ResponseEntity<List<Product>> getLatestProducts() {
        List<Product> latestProducts = productService.getLatestProducts();
        return ResponseEntity.ok(latestProducts);
    }

    @GetMapping("/by-ids")
    public ResponseEntity<?> getProductsByIds(@RequestParam("ids") String ids){
        try {
            //Tach chuoi ids thanh mot mang cac so nguyen
            //split sẽ tách chỗi ids thành 1 mảng các chuổi con dựa vào "," VD "1,2,3"=>["1","2","3"]
            //Sau khi tách chuỗi, mảng kết quả sẽ được chuyển thành một Stream (luồng dữ liệu) để có thể xử lý dễ dàng.
            //map(Long::parseLong) dùng de chuyen chuoi con trong mang thanh so kieu Long
            //Chuỗi ids sẽ được chuyển đổi thành một danh sách List<Long> gồm các ID sản phẩm.
            //va goi xuong service goi toi repository de lay cac sp dua va danh sach ID do
            List<Long> productIds= Arrays.stream(ids.split(","))
                    .map(Long::parseLong)
                    .collect(Collectors.toList());
            List<Product> products=productService.findProductsByIds(productIds);
            return ResponseEntity.ok(products);
        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<DeleteProductResponse> deleteProduct(@PathVariable("id") long id) {
        try {
           Product existingProduct = productService.getProductById(id);
            if(existingProduct!= null){
                productService.deleteProduct(existingProduct.getId());
                return ResponseEntity.ok().body(DeleteProductResponse.builder()
                                .message(localizationUtils.getLocalizedMessage(MessageKeys.DELETE_PRODUCT_SUCCESSFULLY,id))
                        .build());
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(DeleteProductResponse.builder()
                    .message(localizationUtils.getLocalizedMessage(MessageKeys.DELETE_PRODUCT_FAILED,id))
                    .build());
        }
        return null;
    }

 //   @PostMapping("/generateFakerProducts")
    public ResponseEntity<String> generateFakerProducts(){
        Faker faker=new Faker();
        for (int i=0;i<=100;i++){
            String productName=faker.commerce().productName();
            if(productService.existsByName(productName)){
                continue;
            }
            ProductDTO productDTO=ProductDTO.builder()
                    .name(productName)
                    .price((float)faker.number().numberBetween(10,90000000))
                    .description(faker.lorem().sentence())
                    .thumbnail("")
                    .categoryId((long)faker.number().numberBetween(2,5))
                    .build();
            try {
                productService.createProduct(productDTO);
            }catch (Exception e){
                return ResponseEntity.badRequest().body(e.getMessage());
            }
        }
        return ResponseEntity.ok("Fake Products created successfully");
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateProduct( @PathVariable long id, @RequestBody ProductDTO productDTO){
        System.out.println("Payload nhận được: " + productDTO);
        try {
            Product updateProduct=productService.updateProduct(id,productDTO);
            return ResponseEntity.ok(ProductResponse.fromProduct(updateProduct));
        } catch (Exception e) {
           return  ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
