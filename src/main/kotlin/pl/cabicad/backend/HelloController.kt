package pl.cabicad.backend

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

data class HelloResponse(val message: String)

/** Test endpoint used by the frontend to verify backend integration: GET /api/hello. */
@RestController
@RequestMapping("/api")
class HelloController {

    @GetMapping("/hello")
    fun hello(): HelloResponse = HelloResponse(message = "Hello World")
}
