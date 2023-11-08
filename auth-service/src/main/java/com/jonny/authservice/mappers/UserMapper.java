package com.jonny.authservice.mappers;

import com.jonny.authservice.dto.UserCreationDto;
import com.jonny.authservice.dto.UserDto;
import com.jonny.authservice.entities.ShopUser;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(source = "user.id", target = "id")
    @Mapping(source = "user.login", target = "login")
    @Mapping(source = "token", target = "token")
    UserDto toUserDto(ShopUser user, String token);

    @Mapping(source = "encodedPassword", target = "password")
    ShopUser toShopUser(UserCreationDto userDto, String encodedPassword);
}
