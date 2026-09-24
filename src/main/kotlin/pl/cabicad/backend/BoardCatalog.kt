package pl.cabicad.backend

import org.springframework.stereotype.Component

/**
 * Catalog of furniture boards (płyty meblowe) the frontend offers in its board picker
 * (tree: type → brand → concrete board). A board decides the look of the board faces (texture),
 * whether the grain direction matters and which thicknesses can be chosen.
 *
 * For now the catalog is a hard-coded mockup ([MockBoardCatalogRepository]); later it will be read
 * from Firebase – only a new [BoardCatalogRepository] implementation is needed then.
 */

/** Board type = 1st level of the picker tree, e.g. chipboard, MDF, HDF. */
data class BoardType(
    /** Stable id, referenced by [BoardModel.type]. */
    val id: String,
    /** Display name. */
    val name: String,
)

/** Real size [mm] of the area shown by the texture image (width × height of the image). */
data class TextureSize(val width: Double, val height: Double)

/** One concrete board of a manufacturer = a leaf of the picker tree. */
data class BoardModel(
    /** Stable id stored on the boards of a project. */
    val id: String,
    /** Brand / manufacturer (2nd level of the tree), e.g. "Egger", or [RAW_BRAND] for raw boards. */
    val brand: String,
    /** Id of the [BoardType] (1st level of the tree). */
    val type: String,
    /** Series (decor) code of the manufacturer, e.g. "H1145 ST10" (3rd level of the tree). */
    val seriesCode: String,
    /** Decor name, e.g. "Dąb Bardolino naturalny". */
    val name: String,
    /**
     * Link to the image the texture of the board faces is generated from, or null (raw board – the
     * frontend draws its own procedural texture / plain colour). The grain / pattern of the image runs
     * along its HEIGHT (vertical). Served to the browser through `GET /api/boards/{id}/texture`.
     */
    val textureUrl: String?,
    /** Real size of the image [mm] (texture scale), or null when there is no image. */
    val textureSize: TextureSize?,
    /** Does the grain direction (kierunek usłojenia) matter – false = the direction cannot be changed. */
    val grainMatters: Boolean,
    /** Available thicknesses [mm]; empty = any thickness, entered by hand. */
    val thicknesses: List<Double>,
    /** Average colour of the decor (CSS hex) – swatch / fallback while the texture loads. */
    val color: String,
)

data class BoardCatalog(
    val types: List<BoardType>,
    /** Name of the brand grouping raw boards; every type has it (with the default board inside). */
    val rawBrand: String,
    /** Board used when nothing else is chosen (raw chipboard) – selected by default in the picker. */
    val defaultBoardId: String,
    val boards: List<BoardModel>,
)

const val RAW_BRAND = "Surowe (sklejki, płyty wiórowe...)"
const val DEFAULT_BOARD_ID = "raw-chipboard"

/** Source of the board catalog (mockup now, Firebase later). */
interface BoardCatalogRepository {
    fun catalog(): BoardCatalog

    fun findBoard(id: String): BoardModel? = catalog().boards.find { it.id == id }
}

/**
 * Hard-coded catalog: the raw boards + 3 Kronospan and 3 Egger decors (data from the manufacturers'
 * decor pages: kronospan.com, egger.com and Polish distributors' thickness lists, 2800×2070 mm format).
 */
@Component
class MockBoardCatalogRepository : BoardCatalogRepository {

    private val catalog = BoardCatalog(
        types = listOf(
            BoardType("CHIPBOARD", "Płyta wiórowa laminowana"),
            BoardType("MDF", "MDF"),
            BoardType("HDF", "HDF"),
        ),
        rawBrand = RAW_BRAND,
        defaultBoardId = DEFAULT_BOARD_ID,
        boards = listOf(
            // ---- raw boards ----------------------------------------------------------------------
            BoardModel(
                id = DEFAULT_BOARD_ID,
                brand = RAW_BRAND,
                type = "CHIPBOARD",
                seriesCode = "PW",
                name = "Płyta wiórowa surowa",
                textureUrl = null,
                textureSize = null,
                grainMatters = false,
                thicknesses = listOf(8.0, 10.0, 12.0, 16.0, 18.0, 22.0, 25.0, 28.0, 38.0),
                color = "#d8c29a",
            ),
            BoardModel(
                id = "raw-mdf",
                brand = RAW_BRAND,
                type = "MDF",
                seriesCode = "MDF",
                name = "Płyta MDF surowa",
                textureUrl = null,
                textureSize = null,
                grainMatters = false,
                // no fixed variants – the thickness is entered by hand
                thicknesses = emptyList(),
                color = "#b8966a",
            ),
            BoardModel(
                id = "raw-hdf",
                brand = RAW_BRAND,
                type = "HDF",
                seriesCode = "HDF",
                name = "Płyta HDF surowa",
                textureUrl = null,
                textureSize = null,
                grainMatters = false,
                thicknesses = listOf(2.5, 3.0, 4.0, 6.0),
                color = "#8f6a45",
            ),
            // ---- Kronospan (Kronodesign) ---------------------------------------------------------------
            BoardModel(
                id = "kronospan-k003-pw",
                brand = "Kronospan",
                type = "CHIPBOARD",
                seriesCode = "K003 PW",
                name = "Dąb Craft złoty (Gold Craft Oak)",
                textureUrl = "https://kronospan.com/public/files/decors/kronodesign/K/K003.jpg",
                textureSize = TextureSize(900.0, 1350.0),
                grainMatters = true,
                thicknesses = listOf(8.0, 10.0, 12.0, 16.0, 18.0, 19.0, 22.0, 25.0, 28.0, 38.0),
                color = "#c39a73",
            ),
            BoardModel(
                id = "kronospan-k350-rt",
                brand = "Kronospan",
                type = "CHIPBOARD",
                seriesCode = "K350 RT",
                name = "Beton Flow (Concrete Flow)",
                textureUrl = "https://kronospan.com/public/files/decors/kronodesign/K/K350.jpg",
                textureSize = TextureSize(900.0, 1350.0),
                // the pattern has a flow direction – it matters how it is laid
                grainMatters = true,
                thicknesses = listOf(8.0, 10.0, 12.0, 16.0, 18.0, 19.0, 22.0, 25.0, 28.0, 38.0),
                color = "#bdbab4",
            ),
            BoardModel(
                id = "kronospan-k520-su",
                brand = "Kronospan",
                type = "MDF",
                seriesCode = "K520 SU",
                name = "Szmaragd ciemny (Dark Emerald)",
                textureUrl = "https://kronospan.com/public/files/decors/kronodesign/K/K520.jpg",
                textureSize = TextureSize(900.0, 1350.0),
                grainMatters = false,
                thicknesses = listOf(8.0, 10.0, 12.0, 16.0, 18.0, 22.0, 25.0, 28.0, 30.0, 38.0),
                color = "#3f5c5a",
            ),
            // ---- Egger (Eurodekor) -------------------------------------------------------------------
            BoardModel(
                id = "egger-h1145-st10",
                brand = "Egger",
                type = "CHIPBOARD",
                seriesCode = "H1145 ST10",
                name = "Dąb Bardolino naturalny",
                textureUrl = "https://cdn.egger.com/img/pim/8854363537438/8854522462238/original.jpg?width=1024&srcext=png",
                textureSize = TextureSize(1300.0, 2800.0),
                grainMatters = true,
                thicknesses = listOf(8.0, 10.0, 16.0, 18.0, 25.0, 28.0, 38.0),
                color = "#c1a385",
            ),
            BoardModel(
                id = "egger-w1000-st9",
                brand = "Egger",
                type = "CHIPBOARD",
                seriesCode = "W1000 ST9",
                name = "Biały Premium",
                textureUrl = "https://cdn.egger.com/img/pim/8854478520350/8854478553118/original.jpg?width=1024&srcext=png",
                textureSize = TextureSize(1300.0, 2800.0),
                grainMatters = false,
                thicknesses = listOf(8.0, 16.0, 18.0, 25.0),
                color = "#f9faf2",
            ),
            BoardModel(
                id = "egger-f186-st9",
                brand = "Egger",
                type = "CHIPBOARD",
                seriesCode = "F186 ST9",
                name = "Beton Chicago jasnoszary",
                textureUrl = "https://cdn.egger.com/img/pim/8854324805662/8854505521182/original.jpg?width=1024&srcext=png",
                textureSize = TextureSize(1300.0, 2800.0),
                grainMatters = false,
                thicknesses = listOf(8.0, 10.0, 16.0, 18.0, 25.0, 38.0),
                color = "#9a9693",
            ),
        ),
    )

    override fun catalog(): BoardCatalog = catalog
}
