package pl.cabicad.backend

import org.springframework.http.CacheControl
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

/** Image bytes + their media type. */
class TextureImage(val bytes: ByteArray, val contentType: String)

/** Downloads a texture image from the manufacturer's server (null = not available). */
fun interface TextureFetcher {
    fun fetch(url: String): TextureImage?
}

/** [TextureFetcher] using the JDK HTTP client (follows redirects, 20 s timeout). */
@Component
class HttpTextureFetcher : TextureFetcher {
    private val client = HttpClient.newBuilder()
        .followRedirects(HttpClient.Redirect.NORMAL)
        .connectTimeout(Duration.ofSeconds(10))
        .build()

    override fun fetch(url: String): TextureImage? {
        val request = HttpRequest.newBuilder(URI.create(url))
            .timeout(Duration.ofSeconds(20))
            .header("User-Agent", "Mozilla/5.0 (CabiCAD texture proxy)")
            .header("Accept", "image/*")
            .GET()
            .build()
        val response = client.send(request, HttpResponse.BodyHandlers.ofByteArray())
        val type = response.headers().firstValue("Content-Type").orElse("")
        if (response.statusCode() != 200 || !type.startsWith("image/")) return null
        return TextureImage(response.body(), type)
    }
}

/**
 * Proxy of the texture images: downloads an image of a catalog entry from the manufacturer once and
 * keeps it in memory (browsers need the images from our origin – WebGL textures require CORS, which the
 * manufacturers' servers do not always send). Used by the board and the edge band catalogs.
 */
@Component
class TextureProxy(private val fetcher: TextureFetcher) {
    private val cache = ConcurrentHashMap<String, TextureImage>()

    /** 404 when there is no URL (unknown entry / no image), 502 when the download fails. */
    fun serve(url: String?): ResponseEntity<ByteArray> {
        if (url == null) return ResponseEntity.notFound().build()
        val image = cache[url] ?: runCatching { fetcher.fetch(url) }.getOrNull()?.also { cache[url] = it }
            ?: return ResponseEntity.status(HttpStatus.BAD_GATEWAY).build()
        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(image.contentType))
            .cacheControl(CacheControl.maxAge(1, TimeUnit.DAYS))
            .body(image.bytes)
    }
}

/**
 * Board catalog API:
 *  - `GET /api/boards/catalog` – types, brands and concrete boards for the board picker,
 *  - `GET /api/boards/{id}/texture` – the board's texture image, proxied from the manufacturer
 *    ([TextureProxy]). Only URLs of the catalog are fetched.
 */
@RestController
@RequestMapping("/api/boards")
class BoardCatalogController(
    private val repository: BoardCatalogRepository,
    private val textures: TextureProxy,
) {
    @GetMapping("/catalog")
    fun catalog(): BoardCatalog = repository.catalog()

    @GetMapping("/{id}/texture")
    fun texture(@PathVariable id: String): ResponseEntity<ByteArray> = textures.serve(repository.findBoard(id)?.textureUrl)
}
