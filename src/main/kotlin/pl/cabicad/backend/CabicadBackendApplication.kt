package pl.cabicad.backend

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class CabicadBackendApplication

fun main(args: Array<String>) {
    runApplication<CabicadBackendApplication>(*args)
}
