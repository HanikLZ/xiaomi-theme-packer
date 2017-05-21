package org.mdvsc.tools.xiaomi.skin.model

import org.w3c.dom.Node
import java.io.File
import java.io.FileOutputStream
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

/**
 * @author HanikLZ
 * @since 2017/5/10
 */
class MIUITheme private constructor(val themePath: File) {

    companion object {
        const val THEME_DESCRIPTION_FILE_NAME = "description.xml"
        const val THEME_ROOT_TAG = "MIUI-Theme"
        const val THEME_ROOT_TAG_2 = "theme"
        const val KEY_THEME_TITLE = "title"
        const val KEY_THEME_DESIGNER = "designer"
        const val KEY_THEME_AUTHOR = "author"
        const val KEY_THEME_VERSION = "version"
        const val KEY_THEME_UI_VERSION = "uiVersion"

        fun create(path: File) = try { MIUITheme(path) } catch (ignored : Exception) { null }
        fun create(path: String) = try { MIUITheme(File(path)) } catch (ignored : Exception) { null }
    }

    val descriptionMap = parseThemeDescription(File(themePath, THEME_DESCRIPTION_FILE_NAME))

    fun parseThemeDescription(descriptionFile: File): MutableMap<String, String> {
        val document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(descriptionFile)
        document.normalizeDocument()
        val themes = document.getElementsByTagName(THEME_ROOT_TAG).let { if (it.length == 0) document.getElementsByTagName(THEME_ROOT_TAG_2) else it }
        return if (themes.length > 0) mutableMapOf<String, String>().apply {
            themes.item(0).firstChild.let {
                var node = it
                while (node != null) {
                    if (node.nodeType == Node.ELEMENT_NODE) {
                        put(node.nodeName, node.firstChild?.textContent ?: "")
                    }
                    node = node.nextSibling
                }
            }
        } else throw IllegalArgumentException()
    }

    fun save() {
        val document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument()
        val transformer = TransformerFactory.newInstance().newTransformer().apply {
            setOutputProperty(OutputKeys.INDENT, "yes")
        }
        val root = document.createElement(THEME_ROOT_TAG).apply {
            descriptionMap.forEach { t, u ->
                appendChild(document.createElement(t).also { it.appendChild(document.createTextNode(u)) })
            }
        }
        document.appendChild(root)
        val descriptionFile = File(themePath, THEME_DESCRIPTION_FILE_NAME)
        if (!descriptionFile.exists()) {
            descriptionFile.parentFile?.mkdirs()
            descriptionFile.createNewFile()
        }
        FileOutputStream(descriptionFile).use {
            transformer.transform(DOMSource(document), StreamResult(it))
        }
    }

    fun getThemeTitle() = KEY_THEME_TITLE.getDescriptionValue()
    fun getThemeDesigner() = KEY_THEME_DESIGNER.getDescriptionValue()
    fun getThemeAuthor() = KEY_THEME_AUTHOR.getDescriptionValue()
    fun getThemeVersion() = KEY_THEME_VERSION.getDescriptionValue()
    fun getThemeUiVersion() = KEY_THEME_UI_VERSION.getDescriptionValue()

    fun setThemeTitle(value: Any) = KEY_THEME_TITLE.setDescriptionValue(value)
    fun setThemeDesigner(value : Any) = KEY_THEME_DESIGNER.setDescriptionValue(value)
    fun setThemeAuthor(value: Any) = KEY_THEME_AUTHOR.setDescriptionValue(value)
    fun setThemeVersion(value: Any) = KEY_THEME_VERSION.setDescriptionValue(value)
    fun setThemeUiVersion(value: Any) = KEY_THEME_UI_VERSION.setDescriptionValue(value)

    private fun String.getDescriptionValue() = descriptionMap[this] ?:""
    private fun String.setDescriptionValue(value: Any) = descriptionMap.put(this, value.toString())

}
