package com.wakaztahir.kate

import com.wakaztahir.kate.model.LazyBlock
import com.wakaztahir.kate.parser.stream.EmbeddingManager
import com.wakaztahir.kate.parser.stream.SourceStream
import com.wakaztahir.kate.parser.stream.getErrorInfoAtCurrentPointer
import java.io.InputStream
import java.net.URL
import java.nio.file.Paths

open class RelativeResourceEmbeddingManager(
    private val basePath: String,
    private val classLoader: Class<Any> = object {}.javaClass
) : EmbeddingManager {

    override val embeddedStreams: MutableMap<String, Boolean> = mutableMapOf()

    override fun handleException(path: String, stream: SourceStream, exception: Throwable) {
        val indo = stream.getErrorInfoAtCurrentPointer()
        throw Throwable("${completePath(path)}:${indo.first}:${indo.second}", cause = exception)
    }

    fun relativeParentPath(other : String) : String {
        return Paths.get(basePath).resolve(Paths.get(other)).parent.normalize().toString().replace('\\','/')
    }

    fun relativePath(other : String) : String {
        return Paths.get(basePath).resolve(Paths.get(other)).normalize().toString().replace('\\','/')
    }

    fun getUrl(path : String): URL {
        return classLoader.getResource(relativePath(path)) ?: throw IllegalStateException("embedding with path not found ${relativePath(path)} where base $basePath and other $path")
    }

    fun completePath(other : String) : String {
        return getUrl(other).file.removePrefix("/")
    }

    fun getStream(path : String): InputStream {
        return getUrl(path).openStream()
    }

    override fun provideStream(block: LazyBlock, path: String): SourceStream? {
        return InputSourceStream(
            inputStream = getStream(path),
            model = block.model,
            embeddingManager = RelativeResourceEmbeddingManager(
                basePath = relativeParentPath(path),
                classLoader = classLoader
            ),
            placeholderManager = block.source.placeholderManager
        )
    }

}