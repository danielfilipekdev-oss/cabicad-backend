package pl.cabicad.backend

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpHeaders
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.options

@SpringBootTest
@AutoConfigureMockMvc
class WebConfigTests(@Autowired private val mockMvc: MockMvc) {

    @Test
    fun `CORS preflight from Vite dev server is allowed`() {
        mockMvc.options("/api/boards/catalog") {
            header(HttpHeaders.ORIGIN, "http://localhost:5173")
            header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET")
        }.andExpect {
            status { isOk() }
            header { string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:5173") }
        }
    }
}
