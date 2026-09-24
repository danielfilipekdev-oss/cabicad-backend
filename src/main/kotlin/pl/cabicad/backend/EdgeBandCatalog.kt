package pl.cabicad.backend

import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Catalog of edge bands (okleiny krawędzi / obrzeża) for the frontend's band picker – the same tree as the
 * boards: type (ABS, PCV, melamina) → brand → concrete band. A band decides the look of the banded edges
 * (texture), the thicknesses it comes in and the widths of the roll (the band must be wider than the
 * board is thick). `matchingBoardIds` links a band to the boards of the same decor – the picker offers
 * them first ("pasujące do płyty").
 *
 * Hard-coded mockup for now ([MockEdgeBandCatalogRepository]); later Firebase.
 */

/** One concrete edge band = a leaf of the picker tree. */
data class EdgeBandModel(
    val id: String,
    /** Brand / manufacturer of the band (Egger, Spander, Schilsner …). */
    val brand: String,
    /** Id of the band type ([EdgeBandCatalog.types]). */
    val type: String,
    /** Decor code, e.g. "H1145 ST10". */
    val seriesCode: String,
    val name: String,
    /** Image of the decor (its pattern / grain runs along the image height), or null = plain colour. */
    val textureUrl: String?,
    /** Real size [mm] of the image. */
    val textureSize: TextureSize?,
    /** The decor has a grain / pattern direction – it is laid along the edge. */
    val grainMatters: Boolean,
    /** Available thicknesses [mm]; empty = any thickness entered by hand. */
    val thicknesses: List<Double>,
    /** Widths [mm] of the rolls. */
    val widths: List<Double>,
    /** Average colour (CSS hex). */
    val color: String,
    /** Ids of the boards ([BoardModel.id]) the band matches (same decor). */
    val matchingBoardIds: List<String>,
)

data class EdgeBandCatalog(
    /** Band types – 1st level of the tree ([BoardType] has the same shape: id + name). */
    val types: List<BoardType>,
    val bands: List<EdgeBandModel>,
)

interface EdgeBandCatalogRepository {
    fun catalog(): EdgeBandCatalog

    fun findBand(id: String): EdgeBandModel? = catalog().bands.find { it.id == id }
}

private const val EGGER_H1145 = "https://cdn.egger.com/img/pim/8854363537438/8854522462238/original.jpg?width=1024&srcext=png"
private const val EGGER_W1000 = "https://cdn.egger.com/img/pim/8854478520350/8854478553118/original.jpg?width=1024&srcext=png"
private const val EGGER_F186 = "https://cdn.egger.com/img/pim/8854324805662/8854505521182/original.jpg?width=1024&srcext=png"
private const val KRONO = "https://kronospan.com/public/files/decors/kronodesign/K"

/**
 * Mockup: Egger ABS edging (Egger's own sizes: 0.4 / 0.8 / 2 mm, rolls 22–43 mm) for the three Egger
 * decors; ABS bands of Spander / Schilsner – the Polish producers of edges matched to Kronospan decors
 * (e.g. Spander K003 PW: 22 and 43 mm × 0.8 / 1 / 2 mm); plus plain PCV / melamine bands. Decor images
 * are the manufacturers' board decor images (the band carries the same print).
 */
@Component
class MockEdgeBandCatalogRepository : EdgeBandCatalogRepository {
    private val eggerThicknesses = listOf(0.4, 0.8, 2.0)
    private val eggerWidths = listOf(22.0, 23.0, 28.0, 33.0, 43.0)
    private val eggerSize = TextureSize(1300.0, 2800.0)
    private val kronoSize = TextureSize(900.0, 1350.0)

    private val catalog = EdgeBandCatalog(
        types = listOf(BoardType("ABS", "ABS"), BoardType("PCV", "PCV"), BoardType("MELAMINE", "Melamina")),
        bands = listOf(
            // ---- Egger ABS ------------------------------------------------------------------------
            EdgeBandModel("egger-abs-h1145-st10", "Egger", "ABS", "H1145 ST10", "Dąb Bardolino naturalny",
                EGGER_H1145, eggerSize, true, eggerThicknesses, eggerWidths, "#c1a385", listOf("egger-h1145-st10")),
            EdgeBandModel("egger-abs-w1000-st9", "Egger", "ABS", "W1000 ST9", "Biały Premium",
                EGGER_W1000, eggerSize, false, eggerThicknesses, eggerWidths, "#f9faf2", listOf("egger-w1000-st9")),
            EdgeBandModel("egger-abs-f186-st9", "Egger", "ABS", "F186 ST9", "Beton Chicago jasnoszary",
                EGGER_F186, eggerSize, false, eggerThicknesses, eggerWidths, "#9a9693", listOf("egger-f186-st9")),
            // ---- ABS matched to Kronospan decors --------------------------------------------------------
            EdgeBandModel("spander-abs-k003-pw", "Spander", "ABS", "K003 PW", "Dąb Craft złoty",
                "$KRONO/K003.jpg", kronoSize, true, listOf(0.8, 1.0, 2.0), listOf(22.0, 43.0), "#c39a73", listOf("kronospan-k003-pw")),
            EdgeBandModel("spander-abs-k520-su", "Spander", "ABS", "K520 SU", "Szmaragd ciemny",
                "$KRONO/K520.jpg", kronoSize, false, listOf(0.8, 1.0, 2.0), listOf(22.0, 43.0), "#3f5c5a", listOf("kronospan-k520-su")),
            EdgeBandModel("schilsner-abs-k350-rt", "Schilsner", "ABS", "K350 RT", "Beton Flow",
                "$KRONO/K350.jpg", kronoSize, true, listOf(0.8, 2.0), listOf(22.0), "#bdbab4", listOf("kronospan-k350-rt")),
            // ---- plain colours ---------------------------------------------------------------------------
            EdgeBandModel("pcv-black", "Uniwersalne", "PCV", "CZ-M", "Czarne mat",
                null, null, false, emptyList(), listOf(22.0, 43.0), "#1f1f1f", emptyList()),
            EdgeBandModel("melamine-white", "Uniwersalne", "MELAMINE", "BI-MEL", "Biała melamina (z klejem)",
                null, null, false, listOf(0.3, 0.4), listOf(19.0, 22.0, 24.0), "#f4f3ee", emptyList()),
        ),
    )

    override fun catalog(): EdgeBandCatalog = catalog
}

/**
 * Edge band catalog API: `GET /api/edge-bands/catalog`, `GET /api/edge-bands/{id}/texture` (image proxy,
 * see [TextureProxy]).
 */
@RestController
@RequestMapping("/api/edge-bands")
class EdgeBandCatalogController(
    private val repository: EdgeBandCatalogRepository,
    private val textures: TextureProxy,
) {
    @GetMapping("/catalog")
    fun catalog(): EdgeBandCatalog = repository.catalog()

    @GetMapping("/{id}/texture")
    fun texture(@PathVariable id: String): ResponseEntity<ByteArray> = textures.serve(repository.findBand(id)?.textureUrl)
}
