package com.todo.controller

import com.todo.dto.auth.MessageResponse


import com.todo.dto.auth.UserResponse
import com.todo.dto.profile.ProfileResponse
import com.todo.dto.profile.UpdateProfileRequest
import com.todo.service.ProfileService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/profile")
@Validated
@CrossOrigin(origins = ["*"])
class ProfileController(
    private val profileService: ProfileService
) {

    @GetMapping
    fun getProfile(@AuthenticationPrincipal userDetails: UserDetails): ResponseEntity<ProfileResponse> {
        val response = profileService.getProfile(userDetails.username)
        return ResponseEntity.ok(response)
    }

    @PutMapping
    fun updateProfile(
        @AuthenticationPrincipal userDetails: UserDetails,
        @Valid @RequestBody request: UpdateProfileRequest
    ): ResponseEntity<MessageResponse> {
        val response = profileService.updateProfile(userDetails.username, request)
        return ResponseEntity.ok(response)
    }

    @DeleteMapping
    fun deleteAccount(@AuthenticationPrincipal userDetails: UserDetails): ResponseEntity<MessageResponse> {
        val response = profileService.deleteAccount(userDetails.username)
        return ResponseEntity.ok(response)
    }
}
