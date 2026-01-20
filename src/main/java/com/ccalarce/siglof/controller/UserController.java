package com.ccalarce.siglof.controller;

import com.ccalarce.siglof.dto.RegisterRequest;
import com.ccalarce.siglof.dto.UpdateUserRequest;
import com.ccalarce.siglof.dto.UserDto;
import com.ccalarce.siglof.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.MediaType;
import java.io.IOException;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService service;

    @GetMapping("/me")
    public ResponseEntity<UserDto> getMe() {
        return ResponseEntity.ok(service.getCurrentUser());
    }

    @PostMapping("/me/picture")
    public ResponseEntity<Void> uploadProfilePicture(@RequestParam("file") MultipartFile file) throws IOException {
        UserDto currentUser = service.getCurrentUser();
        service.uploadProfilePicture(currentUser.getId(), file);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/picture")
    public ResponseEntity<byte[]> getProfilePicture(@PathVariable Long id) {
        com.ccalarce.siglof.model.entity.User user = service.getProfilePictureWorker(id);
        if (user.getProfilePicture() == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(user.getProfilePictureContentType()))
                .body(user.getProfilePicture());
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<List<UserDto>> getAllUsers() {
        return ResponseEntity.ok(service.getAllUsers());
    }

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<UserDto> createUser(@RequestBody RegisterRequest request) {
        return ResponseEntity.ok(service.createUser(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<UserDto> updateUser(@PathVariable Long id, @RequestBody UpdateUserRequest request) {
        return ResponseEntity.ok(service.updateUser(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        service.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
