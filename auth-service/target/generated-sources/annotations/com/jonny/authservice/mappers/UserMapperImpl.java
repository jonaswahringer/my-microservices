package com.jonny.authservice.mappers;

import com.jonny.authservice.dto.UserCreationDto;
import com.jonny.authservice.dto.UserDto;
import com.jonny.authservice.dto.UserDto.UserDtoBuilder;
import com.jonny.authservice.entities.ShopUser;
import com.jonny.authservice.entities.ShopUser.ShopUserBuilder;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2023-11-07T15:52:37+0100",
    comments = "version: 1.4.2.Final, compiler: javac, environment: Java 17.0.8.1 (Homebrew)"
)
@Component
public class UserMapperImpl implements UserMapper {

    @Override
    public UserDto toUserDto(ShopUser user, String token) {
        if ( user == null && token == null ) {
            return null;
        }

        UserDtoBuilder userDto = UserDto.builder();

        if ( user != null ) {
            if ( user.getId() != null ) {
                userDto.id( user.getId() );
            }
            userDto.login( user.getLogin() );
            if ( user.getRole() != null ) {
                userDto.role( user.getRole().name() );
            }
        }
        if ( token != null ) {
            userDto.token( token );
        }

        return userDto.build();
    }

    @Override
    public ShopUser toShopUser(UserCreationDto userDto, String encodedPassword) {
        if ( userDto == null && encodedPassword == null ) {
            return null;
        }

        ShopUserBuilder shopUser = ShopUser.builder();

        if ( userDto != null ) {
            shopUser.login( userDto.getLogin() );
            shopUser.birthDate( userDto.getBirthDate() );
        }
        if ( encodedPassword != null ) {
            shopUser.password( encodedPassword );
        }

        return shopUser.build();
    }
}
