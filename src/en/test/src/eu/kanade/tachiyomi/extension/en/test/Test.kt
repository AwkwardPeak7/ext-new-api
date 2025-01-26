package eu.kanade.tachiyomi.extension.en.test

import android.util.Log
import androidx.preference.EditTextPreference
import androidx.preference.PreferenceScreen
import eu.kanade.tachiyomi.source.ConfigurableSource
import eu.kanade.tachiyomi.source.model.Filter
import eu.kanade.tachiyomi.source.model.FilterList
import eu.kanade.tachiyomi.source.model.MangasPage
import eu.kanade.tachiyomi.source.model.Page
import eu.kanade.tachiyomi.source.model.SChapter
import eu.kanade.tachiyomi.source.model.SManga
import eu.kanade.tachiyomi.source.online.HttpSource
import kotlinx.coroutines.delay
import mihonx.source.model.UserAgentType
import mihonx.source.utils.sourcePreferences
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Request
import okhttp3.Response

class Test : HttpSource(), ConfigurableSource {
    override val name = "New Api Test"
    override val language = "en"
    override val baseUrl = "http://127.0.0.1"

    override val hasLatestListing = true
    override val hasSearchFilters = true
    override val supportedUserAgentType = UserAgentType.Mobile

    private val preferences = sourcePreferences()

    init {
        headers["User-Agent"]?.also {
            Log.i(name, "Useragent in headers: $it")
            Log.i(name, "Useragent from fun: ${getUserAgent()}")
        }
    }

    override suspend fun getSearchFilters(): FilterList {
        delay(500) // simulate network call
        return FilterList(
            Filter.Header("Test filters")
        )
    }

    override suspend fun getDefaultMangaList(page: Int): MangasPage {
        return MangasPage(
            mangas = listOf(
                SManga.create().apply {
                    url = "/test1"
                    title = "Test 1"
                    thumbnail_url = "https://fakeimg.pl/300/?text=Test+1"
                },
                SManga.create().apply {
                    url = "/test2"
                    title = "Test 2"
                    thumbnail_url = "https://fakeimg.pl/300/?text=Test+2"
                }
            ),
            hasNextPage = false
        )
    }

    override suspend fun getLatestMangaList(page: Int): MangasPage {
        return MangasPage(
            mangas = listOf(
                SManga.create().apply {
                    url = "/test2"
                    title = "Test 2"
                    thumbnail_url = "https://fakeimg.pl/300/?text=Test+2"
                },
                SManga.create().apply {
                    url = "/test1"
                    title = "Test 1"
                    thumbnail_url = "https://fakeimg.pl/300/?text=Test+1"
                }
            ),
            hasNextPage = false
        )
    }

    override suspend fun getMangaList(query: String, filters: FilterList, page: Int): MangasPage {
        val entries = getDefaultMangaList(page).mangas

        return MangasPage(
            mangas = entries.filter { it.title.contains(query) },
            hasNextPage = false
        )
    }

    override suspend fun getMangaDetailsAndChapters(manga: SManga): Pair<SManga, List<SChapter>> {
        val newManga = SManga.create().apply {
            description = buildString {
                append("title: ", manga.title, "\n")
                append("pref val: ", preferences.getString("key", "empty"))
            }
        }
        val chapter = SChapter.create().apply {
            url = manga.url
            name = "Chapter " + manga.title
        }

        return newManga to listOf(chapter)
    }

    override suspend fun getPageList(chapter: SChapter): List<Page> {
        val url = "https://fakeimg.pl/300/".toHttpUrl().newBuilder()
            .addQueryParameter("text", chapter.name)
            .build()
            .toString()

        return listOf(
            Page(1, imageUrl = url)
        )
    }

    override fun setupPreferenceScreen(screen: PreferenceScreen) {
        EditTextPreference(screen.context).apply {
            key = "key"
            title = "Test Preference"
            summary = "%s"
        }
    }

    override fun popularMangaRequest(page: Int): Request {
        throw UnsupportedOperationException()
    }

    override fun popularMangaParse(response: Response): MangasPage {
        throw UnsupportedOperationException()
    }

    override fun latestUpdatesRequest(page: Int): Request {
        throw UnsupportedOperationException()
    }

    override fun latestUpdatesParse(response: Response): MangasPage {
        throw UnsupportedOperationException()
    }

    override fun searchMangaRequest(page: Int, query: String, filters: FilterList): Request {
        throw UnsupportedOperationException()
    }

    override fun searchMangaParse(response: Response): MangasPage {
        throw UnsupportedOperationException()
    }

    override fun mangaDetailsParse(response: Response): SManga {
        throw UnsupportedOperationException()
    }

    override fun chapterListParse(response: Response): List<SChapter> {
        throw UnsupportedOperationException()
    }

    override fun pageListParse(response: Response): List<Page> {
        throw UnsupportedOperationException()
    }

    override fun imageUrlParse(response: Response): String {
        throw UnsupportedOperationException()
    }
}
