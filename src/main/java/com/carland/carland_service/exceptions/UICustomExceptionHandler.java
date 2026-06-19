package com.carland.carland_service.exceptions;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class UICustomExceptionHandler {
    @ExceptionHandler(InviteException.class)
    public String handleInviteException(InviteException ex, Model model) {
        model.addAttribute("error", ex.getMessage());
        return "invite-error";
    }
}
