package io.legado.app.model.rss

import io.legado.app.data.entities.RssArticle
import io.legado.app.data.entities.RssSource
import io.legado.app.help.coroutine.Coroutine
import io.legado.app.model.DebugLog
import io.legado.app.model.analyzeRule.AnalyzeRule
import io.legado.app.model.analyzeRule.AnalyzeUrl
import io.legado.app.utils.NetworkUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.CoroutineContext

@Suppress("MemberVisibilityCanBePrivate")
object Rss {

    suspend fun getArticles(
        rssSource: RssSource,
        page: Int,
        debugLog: DebugLog? = null
    ): MutableList<RssArticle> {
        val analyzeUrl = AnalyzeUrl(
            rssSource.sourceUrl,
            page = page
        )
        val body = analyzeUrl.getResponseAwait().body
        return RssParserByRule.parseXML(body, rssSource, debugLog)
    }

    suspend fun getContent(
        rssArticle: RssArticle,
        rssSource: RssSource,
        debugLog: DebugLog? = null
    ): String {
        val analyzeUrl = AnalyzeUrl(
            rssArticle.link,
            baseUrl = rssArticle.origin
        )
        val body = analyzeUrl.getResponseAwait().body
        debugLog?.log(rssSource.sourceUrl, "≡获取成功:${rssSource.sourceUrl}")
        debugLog?.log(rssSource.sourceUrl, body)
        val analyzeRule = AnalyzeRule(rssArticle)
        analyzeRule.setContent(body, NetworkUtils.getAbsoluteURL(rssArticle.origin, rssArticle.link))
        return analyzeRule.getString(rssSource.ruleContent)
    }
}