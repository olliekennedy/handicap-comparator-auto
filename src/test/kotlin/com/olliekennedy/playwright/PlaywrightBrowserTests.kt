package com.olliekennedy.playwright

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.contains
import com.olliekennedy.app
import org.http4k.playwright.Http4kBrowser
import org.http4k.playwright.LaunchPlaywrightBrowser
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

@Disabled("leaving here as a template")
class PlaywrightBrowserTests {

    @RegisterExtension
    val playwright = LaunchPlaywrightBrowser(app)

    @Test
    fun `can browse app`(browser: Http4kBrowser) {
        with(browser.newPage()) {
            assertThat(navigateHome().text(), contains("Hello, my name is Ollie and this is my website. Enjoy.".toRegex()))
        }
    }
}

