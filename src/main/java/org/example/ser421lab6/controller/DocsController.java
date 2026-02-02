package org.example.ser421lab6.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Redirect Controller for the API Documentation
 */
@Controller
public class DocsController {

    @GetMapping("/apidoc")
    public String apidocNoSlash() {
        return "redirect:/apidoc/index.html";
    }

    @GetMapping("/apidoc/")
    public String apidocWithSlash() {
        return "redirect:/apidoc/index.html";
    }
    
}
