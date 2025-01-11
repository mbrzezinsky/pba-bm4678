package lab04.users;

import java.util.List;
import java.util.stream.Collectors;

import org.mapstruct.Mapper;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lab04.users.api.model.User;

@Mapper(componentModel = "spring")
public interface UserMapper
{

    UserDto toModel(@NotNull @Valid User user);

    User toApi(UserDto save);

    default List<User> toApi(List<UserDto> users)
    {
        return users.stream()
                .map(this::toApi)
                .collect(Collectors.toList());
    }
}
