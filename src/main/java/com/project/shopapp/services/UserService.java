package com.project.shopapp.services;

import com.project.shopapp.components.JwtTokenUtils;
import com.project.shopapp.components.LocalizationUtils;
import com.project.shopapp.dtos.UserDTO;
import com.project.shopapp.exceptions.DataNotFoundException;
import com.project.shopapp.exceptions.PermissionDenyException;
import com.project.shopapp.models.Role;
import com.project.shopapp.models.User;
import com.project.shopapp.repositories.RoleRepository;
import com.project.shopapp.repositories.UserRepository;
import com.project.shopapp.utils.MessageKeys;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService implements IUserService{
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private  final JwtTokenUtils jwtTokenUtils;
    private final AuthenticationManager authenticationManager;
    private final LocalizationUtils localizationUtils;


    @Override
    @Transactional
    public User createUser(UserDTO userDTO) throws Exception {
        String phoneNumber=userDTO.getPhoneNumber();
        if(userRepository.existsByPhoneNumber(phoneNumber)){
            throw new DataIntegrityViolationException(localizationUtils
                    .getLocalizedMessage(MessageKeys.PHONE_NUMBER_ALREADY_EXISTS));
        }
        Role role=roleRepository.findById(userDTO.getRoleId())
                .orElseThrow(()->new DataNotFoundException(
                        localizationUtils
                                .getLocalizedMessage(MessageKeys.ROLE_DOES_NOT_EXISTS)));
        if(role.getName().toUpperCase().equals(Role.ADMIN)){
            throw new PermissionDenyException(localizationUtils
                    .getLocalizedMessage(MessageKeys.REGISTER_ROLE_AN_ADMIN_ACCOUNT));
        }
        //Convert từ userDTO sang user
        User newUser=User.builder()
                .fullName(userDTO.getFullName())
                .phoneNumber(userDTO.getPhoneNumber())
                .address(userDTO.getAddress())
                .password(userDTO.getPassword())
                .dateOfBirth(userDTO.getDateOfBirth())
                .facebookAccountId(userDTO.getFacebookAccountId())
                .googleAccountId(userDTO.getGoogleAccountId())
                .build();

        newUser.setRole(role);

        //kiem tra neu co accountId thi khong yeu cau password
        if(userDTO.getFacebookAccountId()==0 && userDTO.getGoogleAccountId()==0){
            String password=userDTO.getPassword();
            String encodePassword=passwordEncoder.encode(password);
            newUser.setPassword(encodePassword);
            // se noi den trong phan spring security
        }
        return userRepository.save(newUser);
    }

    @Override
    public String login(String phoneNumber, String password,Long roleId) throws Exception{
        // trả về kiểu String vì khi đăng nhập nó sẽ trả về token là 1 chuỗi String
        //doan nay lien quan nhieu den phan security (kho)
        Optional<User> optionalUser=userRepository.findByPhoneNumber(phoneNumber);
        if(optionalUser.isEmpty()){
            throw new DataNotFoundException(localizationUtils
                    .getLocalizedMessage(MessageKeys.INVALID_PHONE_NUMBER));
        }
//        return optionalUser.get();//muốn trả về JWT token
        User existingUser= optionalUser.get();
        //kiem tra password
        if(existingUser.getFacebookAccountId()==0 && existingUser.getGoogleAccountId()==0){
            if(!passwordEncoder.matches(password, existingUser.getPassword())){
                throw new BadCredentialsException(localizationUtils
                        .getLocalizedMessage(MessageKeys.WRONG_PASSWORD));
            }
        }
        Optional<Role> optionalRole=roleRepository.findById(roleId);
        if(optionalRole.isEmpty() || !roleId.equals(existingUser.getRole().getId())){
            throw new DataNotFoundException(localizationUtils
                    .getLocalizedMessage(MessageKeys.ROLE_DOES_NOT_EXISTS));
        }
        if(!optionalUser.get().isActive()){
            throw new DataNotFoundException(localizationUtils.getLocalizedMessage(MessageKeys.USER_IS_LOCKED));
        }
        UsernamePasswordAuthenticationToken authenticationToken=new UsernamePasswordAuthenticationToken(
                phoneNumber,password,existingUser.getAuthorities()
        );
        //Authenticate(Xac thuc) voi Java Spring security
        authenticationManager.authenticate(authenticationToken);
        return jwtTokenUtils.generateToken(optionalUser.get());
    }

    @Override
    public User getUserDetailFromToken(String token) throws Exception {
        if(jwtTokenUtils.isTokenExpired(token)){
            throw new Exception("Token is expired");
        }
        String phoneNumber= jwtTokenUtils.extractPhoneNumber(token);
        Optional<User> user=userRepository.findByPhoneNumber(phoneNumber);
        if(user.isPresent()){
            return user.get();
        }
        else {
            throw new Exception("User not found");
        }
    }
}
