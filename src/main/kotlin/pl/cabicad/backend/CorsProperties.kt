package pl.cabicad.backend

import org.springframework.boot.context.properties.ConfigurationProperties

/** `cabicad.cors.*` – frontend origins allowed to call the REST API (see application.yml). */
@ConfigurationProperties(prefix = "cabicad.cors")
data class CorsProperties(
    val allowedOrigins: List<String> = listOf("http://localhost:5173"),
)
