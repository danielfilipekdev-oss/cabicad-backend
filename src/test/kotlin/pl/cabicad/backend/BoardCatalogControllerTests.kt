package pl.cabicad.backend

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Primary
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@SpringBootTest
@AutoConfigureMockMvc
@Import(BoardCatalogControllerTests.FakeFetcher::class)
class BoardCatalogControllerTests(
    @Autowired private val mockMvc: MockMvc,
    @Autowired private val repository: BoardCatalogRepository,
) {
    @TestConfiguration
    class FakeFetcher {
        /** No network in tests: every texture is a 3-byte "PNG". */
        @Bean
        @Primary
        fun fakeTextureFetcher() = TextureFetcher { TextureImage(byteArrayOf(1, 2, 3), "image/png") }
    }

    @Test
    fun `GET api boards catalog returns types, the default raw board and the decors`() {
        mockMvc.get("/api/boards/catalog").andExpect {
            status { isOk() }
            jsonPath("$.defaultBoardId") { value(DEFAULT_BOARD_ID) }
            jsonPath("$.rawBrand") { value(RAW_BRAND) }
            jsonPath("$.types[0].id") { value("CHIPBOARD") }
            jsonPath("$.boards[?(@.id == 'egger-h1145-st10')].seriesCode") { value("H1145 ST10") }
            jsonPath("$.boards[?(@.id == 'egger-h1145-st10')].grainMatters") { value(true) }
            jsonPath("$.boards[?(@.id == 'raw-mdf')].thicknesses[0]") { doesNotExist() }
        }
    }

    @Test
    fun `mock catalog has 3 Kronospan and 3 Egger boards, at least one wood-like of each`() {
        val boards = repository.catalog().boards
        for (brand in listOf("Kronospan", "Egger")) {
            val ofBrand = boards.filter { it.brand == brand }
            assertEquals(3, ofBrand.size, brand)
            assertTrue(ofBrand.any { it.grainMatters }, brand)
            assertTrue(ofBrand.all { it.textureUrl != null && it.textureSize != null }, brand)
        }
        val types = repository.catalog().types.map { it.id }.toSet()
        assertTrue(boards.all { it.type in types })
        assertTrue(boards.any { it.id == DEFAULT_BOARD_ID && it.brand == RAW_BRAND })
    }

    @Test
    fun `texture is proxied for a catalog board and 404 for unknown or raw boards`() {
        mockMvc.get("/api/boards/egger-h1145-st10/texture").andExpect {
            status { isOk() }
            content { contentType("image/png") }
            content { bytes(byteArrayOf(1, 2, 3)) }
        }
        mockMvc.get("/api/boards/unknown/texture").andExpect { status { isNotFound() } }
        mockMvc.get("/api/boards/$DEFAULT_BOARD_ID/texture").andExpect { status { isNotFound() } }
    }
}
