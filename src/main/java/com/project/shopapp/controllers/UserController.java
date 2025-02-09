package com.project.shopapp.controllers;

import com.project.shopapp.dtos.UpdateUserDTO;
import com.project.shopapp.dtos.UserDTO;
import com.project.shopapp.dtos.UserLoginDTO;
import com.project.shopapp.exceptions.DataNotFoundException;
import com.project.shopapp.models.Role;
import com.project.shopapp.models.User;
import com.project.shopapp.responses.*;
import com.project.shopapp.services.UserService;
import com.project.shopapp.components.LocalizationUtils;
import com.project.shopapp.utils.MessageKeys;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final LocalizationUtils localizationUtils;

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> createUser(@Valid @RequestBody UserDTO userDTO, BindingResult result){
        RegisterResponse registerResponse=new RegisterResponse();
        try {
            if (result.hasErrors())
            {
                List<String> errorMessages = result.getFieldErrors()
                        .stream()
                        .map(FieldError::getDefaultMessage)
                        .toList();
                registerResponse.setMessage(errorMessages.toString());
                return ResponseEntity.badRequest().body(registerResponse);
            }
            if(!userDTO.getPassword().equals(userDTO.getRetypePassword())){
                registerResponse.setMessage(localizationUtils.getLocalizedMessage(MessageKeys.PASSWORD_NOT_MATCH));
                return ResponseEntity.badRequest().body(registerResponse);
            }
            if(userDTO.getRoleId()==null){
                userDTO.setRoleId(1L);
            }
            User user=userService.createUser(userDTO);
//                return ResponseEntity.ok("Register successfully");
                registerResponse.setMessage(localizationUtils.getLocalizedMessage(MessageKeys.REGISTER_SUCCESSFULLY));
                return ResponseEntity.ok(RegisterResponse.builder()
                                .user(user)
                        .build());
            }catch (Exception e){
                registerResponse.setMessage(e.getMessage());
                return  ResponseEntity.badRequest().body(registerResponse);
        }
    }

//        "phone_number":"0123654343",
//         "password":"1234567"
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody UserLoginDTO userLoginDTO)  {
        //kiem tra thong tin dang nhap va sinh token
        try {
            String token = userService.login(
                    userLoginDTO.getPhoneNumber(),
                    userLoginDTO.getPassword(),
                    userLoginDTO.getRoleId()==null ? 1 : userLoginDTO.getRoleId());
            //tra ve token trong response
            return ResponseEntity.ok(LoginResponse.builder()
                            .message(localizationUtils.getLocalizedMessage(MessageKeys.LOGIN_SUCCESSFULLY))
                            .token(token)
                    .build());
            //Tra ve token trong response
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(LoginResponse.builder()
                            .message(localizationUtils.getLocalizedMessage(MessageKeys.LOGIN_FAILED,e.getMessage()))
                    .build());
        }
    }

    @GetMapping("")
    public ResponseEntity<UserListResponse> getAllUsers(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int limit){
        PageRequest pageRequest=PageRequest.of(
                page,limit,
                Sort.by("id").ascending());
        Page<UserResponse> userPage=userService
                .findAll(keyword,pageRequest)
                .map(UserResponse::fromUser);
        //lay tong so trang
        int totalPages=userPage.getTotalPages();
        List<UserResponse> userResponses=userPage.getContent();
        return ResponseEntity.ok(UserListResponse.builder()
                .users(userResponses)
                .totalPages(totalPages)
                .build());
    }

    @PostMapping("/details")
    public ResponseEntity<UserResponse> getUserDetails(@RequestHeader("Authorization") String token){
        try {
            String extractedToken=token.substring(7);//Loai bo "Bearer" tu chuoi token
            User user=userService.getUserDetailFromToken(extractedToken);
            return ResponseEntity.ok(UserResponse.fromUser(user));
        }
        catch (Exception e){
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/details/{userId}")
    public ResponseEntity<UserResponse> updateUserDetails(
            @PathVariable Long userId,
            @RequestBody UpdateUserDTO updatedUserDTO,
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        try {
            String extractedToken = authorizationHeader.substring(7);
            User user = userService.getUserDetailFromToken(extractedToken);
            // Ensure that the user making the request matches the user being updated
            if (user.getId() != userId) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            User updatedUser = userService.updateUser(userId, updatedUserDTO);
            return ResponseEntity.ok(UserResponse.fromUser(updatedUser));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{userId}/status")
    public ResponseEntity<UserBlockOrEnableResponse> blockOrEnableUser(
            @PathVariable("userId") Long userId,
            @RequestParam("active") Boolean active)
    {
        try{
            // Gọi service để xử lý
            userService.blockOrEnable(userId,active);
            String message = active ? "tài khoản đã được kích hoạt." : "tài khoản đã bị khóa.";
            return ResponseEntity.ok(UserBlockOrEnableResponse.builder()
                            .message(message)
                    .build());
        }catch (DataNotFoundException ex){
            // Nếu không tìm thấy user, trả về lỗi 404
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(UserBlockOrEnableResponse.builder()
                            .message(ex.getMessage())
                    .build());
        }
    }
}
