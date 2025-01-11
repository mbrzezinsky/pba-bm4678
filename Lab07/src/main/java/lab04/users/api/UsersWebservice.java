package lab04.users.api;

import java.util.List;
import java.util.UUID;
import jakarta.validation.constraints.NotNull;
import lab04.users.UserDto;
import lab04.users.UserRepository;
import lab04.users.api.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lab04.users.UserMapper;

@Slf4j
@RestController
@RequiredArgsConstructor
public class UsersWebservice implements UsersApi
{

    private final UserMapper mapper;
    private final UserRepository repository;

    @Override
    @PostMapping("/api/users")
    public ResponseEntity<UserResponse> createUser(
            @NotNull @Parameter(name = "X-HMAC-SIGNATURE", description = "", required = true, in = ParameterIn.HEADER) @RequestHeader(value = "X-HMAC-SIGNATURE", required = true) String X_HMAC_SIGNATURE,
            @Parameter(name = "body", description = "User object that has to be added", required = true) @Valid @RequestBody CreateRequest body
    ) {

        UserDto userDto = mapper.toModel(body.getUser());
        UserDto savedUserDto = repository.save(userDto);
        User userResponse = mapper.toApi(savedUserDto);

        return ResponseEntity.ok(new UserResponse(body.getRequestHeader(), userResponse));
    }

    @Override
    @DeleteMapping("/api/users/{id}")
    public ResponseEntity<Void> deleteUser(@Parameter(name = "id", description = "", required = true, in = ParameterIn.PATH) @PathVariable("id") UUID id)
    {
        repository.deleteById(id);

        return ResponseEntity.noContent().build();
    }

    @Override
    @GetMapping("/api/users")
    public ResponseEntity<UserListResponse> getAllUsers()
    {
        List<User> api = mapper.toApi(repository.findAll());
        return ResponseEntity.ok(new UserListResponse(null, api));
    }

    @Override
    @GetMapping("/api/users/{id}")
    public ResponseEntity<UserResponse> getUserById(@Parameter(name = "id", description = "", required = true, in = ParameterIn.PATH) @PathVariable("id") UUID id)
    {
        return ResponseEntity.ok(new UserResponse(null, mapper.toApi(repository.getReferenceById(id))));
    }

    @Override
    @PutMapping("/api/users/{id}")
    public ResponseEntity<UserResponse> updateUser(
        @Parameter(name = "id", description = "", required = true, in = ParameterIn.PATH) @PathVariable("id") UUID id,
        @NotNull @Parameter(name = "X-JWS-SIGNATURE", description = "", required = true, in = ParameterIn.HEADER) @RequestHeader(value = "X-JWS-SIGNATURE", required = true) String X_JWS_SIGNATURE,
        @Parameter(name = "body", description = "", required = true) @Valid @RequestBody UpdateRequest body
    )
    {
        UserDto userDto = mapper.toModel(body.getUser());
        userDto.setId(id);
        User user = mapper.toApi(repository.save(userDto));

        return ResponseEntity.ok(new UserResponse(body.getRequestHeader(), user));
    }
}
