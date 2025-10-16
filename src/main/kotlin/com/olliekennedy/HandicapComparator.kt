package com.olliekennedy

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.http4k.core.HttpHandler
import org.http4k.core.Method
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.then
import org.http4k.filter.DebuggingFilters.PrintRequest
import org.http4k.routing.bind
import org.http4k.routing.path
import org.http4k.routing.routes
import org.http4k.server.Jetty
import org.http4k.server.asServer
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

val jobs = ConcurrentHashMap<String, Pair<Boolean, ByteArray?>>()

fun startJob(): String {
    val jobId = UUID.randomUUID().toString()
    jobs[jobId] = false to null
    GlobalScope.launch {
        // Simulate long job
        delay(3000)
        val fileContent = "Generated file content".toByteArray()
        jobs[jobId] = true to fileContent
    }
    return jobId
}

val app = routes(
    "/start" bind Method.GET to {
        val jobId = startJob()
        Response(Status.OK).body(jobId)
    },
    "/status/{jobId}" bind Method.GET to { req ->
        val jobId = req.path("jobId") ?: return@to Response(Status.BAD_REQUEST)
        val (done, _) = jobs[jobId] ?: return@to Response(Status.NOT_FOUND)
        Response(Status.OK).body(if (done) "done" else "pending")
    },
    "/download/{jobId}" bind Method.GET to { req ->
        val jobId = req.path("jobId") ?: return@to Response(Status.BAD_REQUEST)
        val (done, file) = jobs[jobId] ?: return@to Response(Status.NOT_FOUND)
        if (!done || file == null) return@to Response(Status.ACCEPTED)
        Response(Status.OK)
            .header("Content-Disposition", "attachment; filename=\"result.txt\"")
            .body(file.inputStream(), file.size.toLong())
    }
)

fun buildApp(debug: Boolean = false): HttpHandler {
    val core = SecurityHeaders.Add.then(app)
    return if (debug) PrintRequest().then(core) else core
}

fun main() {
    val port = System.getenv("PORT")?.toIntOrNull() ?: 9000
    buildApp(debug = true).asServer(Jetty(port)).start()
    println("Server started on $port")
}
