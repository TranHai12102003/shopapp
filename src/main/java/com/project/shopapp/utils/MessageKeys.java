package com.project.shopapp.utils;

public class MessageKeys {
    public static final String LOGIN_SUCCESSFULLY =  "user.login.login_successfully";
    public static final String REGISTER_SUCCESSFULLY =  "user.register_successfully";
    public static final String LOGIN_FAILED =  "user.login.login_failed";
    public static final String INVALID_PHONE_NUMBER="user.login.invalid_phone_number";
    public static final String PASSWORD_NOT_MATCH =  "user.register.password_not_match";
    public static final String PHONE_NUMBER_ALREADY_EXISTS="user.register.phone_number_already_exists";
    public static final String REGISTER_ROLE_AN_ADMIN_ACCOUNT="user.register.register_role_an_admin_account";
    public static final String USER_IS_LOCKED = "user.login.user_is_locked";
    public static final String WRONG_PASSWORD = "user.login.wrong_password";
    public static final String ROLE_DOES_NOT_EXISTS = "user.login.role_not_exist";

    public static final String INSERT_CATEGORY_SUCCESSFULLY = "category.create_category.create_successfully";
    public static final String DELETE_CATEGORY_SUCCESSFULLY = "category.delete_category.delete_successfully";
    public static final String UPDATE_CATEGORY_SUCCESSFULLY = "category.update_category.update_successfully";
    public static final String INSERT_CATEGORY_FAILED = "category.create_category.create_failed";
    public static final String CATEGORY_NOT_FOUND="category.find_category_failed";

    public static final String DELETE_ORDER_SUCCESSFULLY = "order.delete_order.delete_successfully";
    public static final String DELETE_ORDER_DETAIL_SUCCESSFULLY = "order.delete_order_detail.delete_successfully";

    public static final String CREATE_PRODUCT_FAILED="product.create_product_failed";
    public static final String CAN_NOT_FIND_PRODUCTS="product.find_product_failed";
    public static final String DELETE_PRODUCT_SUCCESSFULLY="product.delete_product.successfully";
    public static final String DELETE_PRODUCT_FAILED="product.delete_product.failed";

    public static final String UPLOAD_IMAGES_MAX_6 = "product.upload_images.error_max_5_images";
    public static final String UPLOAD_IMAGES_FILE_LARGE = "product.upload_images.file_large";
    public static final String UPLOAD_IMAGES_FILE_MUST_BE_IMAGE = "product.upload_images.file_must_be_image";

    public static final String ATTRIBUTE_NOT_FOUND="attribute.find_attribute_failed";
    public static final String INSERT_ATTRIBUTE_SUCCESSFULLY="attribute.insert_attribute_successfully";
    public static final String INSERT_ATTRIBUTE_FAILED="attribute.insert_attribute_failed";

    public static final String INSERT_SLIDER_FAILED="slider.create_slider_failed";
    public static final String CAN_NOT_FIND_SLIDER="slider.find_slider_id_failed";
    public static final String DELETE_SLIDER_SUCCESSFULLY="slider.delete_successfully";
}
