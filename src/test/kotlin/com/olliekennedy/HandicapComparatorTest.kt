import com.olliekennedy.app
import com.olliekennedy.jobs
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Status
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class HandicapComparatorTest {

    @Test
    fun `start endpoint returns jobId and job is registered`() {
        val response = app(Request(Method.GET, "/start"))
        assertEquals(Status.OK, response.status)
        val jobId = response.bodyString()
        assertTrue(jobId.isNotBlank())
        assertTrue(jobs.containsKey(jobId))
    }

    @Test
    fun `status endpoint returns pending then done`() = runBlocking {
        val response = app(Request(Method.GET, "/start"))
        val jobId = response.bodyString()
        // Immediately after start, should be pending
        val statusPending = app(Request(Method.GET, "/status/$jobId"))
        assertEquals(Status.OK, statusPending.status)
        assertEquals("pending", statusPending.bodyString())
        // Wait for job to complete
        delay(4000)
        val statusDone = app(Request(Method.GET, "/status/$jobId"))
        assertEquals(Status.OK, statusDone.status)
        assertEquals("done", statusDone.bodyString())
    }

    @Test
    fun `status endpoint returns 404 for unknown job`() {
        val response = app(Request(Method.GET, "/status/unknown"))
        assertEquals(Status.NOT_FOUND, response.status)
    }

    @Test
    fun `status endpoint returns 404 for missing jobId`() {
        val response = app(Request(Method.GET, "/status/"))
        assertEquals(Status.NOT_FOUND, response.status)
    }

    @Test
    fun `download endpoint returns 202 if job not done`() {
        val response = app(Request(Method.GET, "/start"))
        val jobId = response.bodyString()
        val download = app(Request(Method.GET, "/result/$jobId"))
        assertEquals(Status.ACCEPTED, download.status)
    }

    @Test
    fun `download endpoint shows file content when job done`() = runBlocking {
        val response = app(Request(Method.GET, "/start"))
        val jobId = response.bodyString()
        delay(4000)
        val result = app(Request(Method.GET, "/result/$jobId"))
        assertEquals(Status.OK, result.status)
        assertEquals("amazing results", result.bodyString())
    }

    @Test
    fun `download endpoint returns 404 for unknown job`() {
        val response = app(Request(Method.GET, "/result/unknown"))
        assertEquals(Status.NOT_FOUND, response.status)
    }

    @Test
    fun `download endpoint returns 404 for missing jobId`() {
        val response = app(Request(Method.GET, "/result/"))
        assertEquals(Status.NOT_FOUND, response.status)
    }
}
