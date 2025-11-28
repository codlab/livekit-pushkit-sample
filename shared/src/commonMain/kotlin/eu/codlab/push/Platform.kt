package eu.codlab.push

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform