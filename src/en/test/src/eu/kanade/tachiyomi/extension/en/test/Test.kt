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
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import mihonx.network.rateLimit
import mihonx.source.model.UserAgentType
import mihonx.source.utils.sourcePreferences
import mihonx.utils.parseAs
import mihonx.utils.parseAsDocument
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import kotlin.time.Duration.Companion.seconds

class Test : HttpSource(), ConfigurableSource {
    override val name = "New Api Test"
    override val language = "en"
    override val baseUrl = "http://127.0.0.1"

    override val client: OkHttpClient = super.client
        .newBuilder()
        .rateLimit(5, 1.seconds)
        .build()

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
        val manga = MANGA_LIST.parseAs<JsonArray>().map {
            with(it.jsonObject) {
                SManga.create().apply {
                    url = get("url")!!.jsonPrimitive.content
                    title = get("name")!!.jsonPrimitive.content
                    thumbnail_url = get("thumb")!!.jsonPrimitive.content
                }
            }
        }

        return MangasPage(
            manga,
            hasNextPage = false
        )
    }

    override suspend fun getLatestMangaList(page: Int): MangasPage {
        return getDefaultMangaList(page)
    }

    override suspend fun getMangaList(query: String, filters: FilterList, page: Int): MangasPage {
        val entries = getDefaultMangaList(page).mangas

        return MangasPage(
            mangas = entries.filter { it.title.contains(query) },
            hasNextPage = false
        )
    }

    override suspend fun getMangaDetails(
        manga: SManga,
        updateManga: Boolean,
        fetchChapters: Boolean
    ): Pair<SManga, List<SChapter>> {
        val newManga = if (updateManga) {
            SManga.create().apply {
                description = buildString {
                    append("title: ", manga.title, "\n")
                    append("pref val: ", preferences.getString("key", "empty"))
                }
            }
        } else {
            manga
        }

        val chapter = if (fetchChapters) {
            CHAPTERS.parseAsDocument(baseUrl).select("a.chap").map {
                SChapter.create().apply {
                    setUrlWithoutDomain(it.absUrl("href"))
                    name = it.text()
                }
            }
        } else {
            emptyList()
        }

        return newManga to chapter
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
}
