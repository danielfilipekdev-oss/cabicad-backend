package pl.cabicad.backend

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import kotlin.test.assertTrue

@SpringBootTest
@AutoConfigureMockMvc
@Import(BoardCatalogControllerTests.FakeFetcher::class)
class EdgeBandCatalogControllerTests(
    @Autowired private val mockMvc: MockMvc,
    @Autowired private val bands: EdgeBandCatalogRepository,
    @Autowired private val boards: BoardCatalogRepository,
) {
    @Test
    fun `GET api edge-bands catalog returns types and bands`() {
        mockMvc.get("/api/edge-bands/catalog").andExpect {
            status { isOk() }
            jsonPath("$.types[0].id") { value("ABS") }
            jsonPath("$.bands[?(@.id == 'egger-abs-h1145-st10')].matchingBoardIds[0]") { value("egger-h1145-st10") }
            jsonPath("$.bands[?(@.id == 'pcv-black')].thicknesses[0]") { doesNotExist() }
        }
    }

    @Test
    fun `every band has a known type and matches only existing boards`() {
        val types = bands.catalog().types.map { it.id }.toSet()
        val boardIds = boards.catalog().boards.map { it.id }.toSet()
        for (b in bands.catalog().bands) {
            assertTrue(b.type in types, b.id)
            assertTrue(boardIds.containsAll(b.matchingBoardIds), b.id)
            assertTrue(b.widths.isNotEmpty(), b.id)
        }
        // every decor board has a matching band
        val matched = bands.catalog().bands.flatMap { it.matchingBoardIds }.toSet()
        assertTrue(boards.catalog().boards.filter { it.textureUrl != null }.all { it.id in matched })
    }

    @Test
    fun `texture is proxied for a band with an image, 404 otherwise`() {
        mockMvc.get("/api/edge-bands/spander-abs-k003-pw/texture").andExpect {
            status { isOk() }
            content { bytes(byteArrayOf(1, 2, 3)) }
        }
        mockMvc.get("/api/edge-bands/pcv-black/texture").andExpect { status { isNotFound() } }
        mockMvc.get("/api/edge-bands/nope/texture").andExpect { status { isNotFound() } }
    }
}
