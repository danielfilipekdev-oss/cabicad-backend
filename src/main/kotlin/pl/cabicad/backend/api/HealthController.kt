package pl.cabicad.backend.api

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

data class PingResponse(val status: String, val service: String)

@RestController
@RequestMapping("/api")
class HealthController {

    @GetMapping("/ping")
    fun ping(): PingResponse = PingResponse(status = "ok", service = "cabicad-backend")
}
