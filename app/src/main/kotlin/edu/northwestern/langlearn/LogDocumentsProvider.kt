package edu.northwestern.langlearn

import android.content.Context
import android.database.Cursor
import android.provider.DocumentsProvider
import android.provider.DocumentsContract.Root
import android.provider.DocumentsContract.Document
import android.database.MatrixCursor
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.util.Log
import android.webkit.MimeTypeMap
import android.os.Handler

import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.io.FileOutputStream
import java.io.ByteArrayOutputStream

class LogDocumentsProvider : DocumentsProvider() {
    private val TAG = javaClass.simpleName
    private val ROOT = "root"
    private val DEFAULT_ROOT_PROJECTION = arrayOf(
            Root.COLUMN_ROOT_ID,
            Root.COLUMN_ICON,
            Root.COLUMN_TITLE,
            Root.COLUMN_FLAGS,
            Root.COLUMN_MIME_TYPES,
            Root.COLUMN_DOCUMENT_ID)
    private val DEFAULT_DOCUMENT_PROJECTION = arrayOf(
            Document.COLUMN_DOCUMENT_ID,
            Document.COLUMN_DISPLAY_NAME,
            Document.COLUMN_MIME_TYPE,
            Document.COLUMN_FLAGS,
            Document.COLUMN_SIZE,
            Document.COLUMN_LAST_MODIFIED)

    lateinit var baseDir: File
        private set

    override fun onCreate(): Boolean {
        Log.v(TAG, "onCreate")

        baseDir = context!!.getFilesDir(); // If baseDir could be null this is where to deal with it since everything below depends on it not being null.
        Log.v(TAG, baseDir.getPath().toString())
        // writeDummyFilesToStorage()
        return true
    }

    @Throws(FileNotFoundException::class)
    override fun queryRoots(projection: Array<out String>?): Cursor {
        Log.v(TAG, "queryRoots")

        val result = MatrixCursor(projection ?: DEFAULT_ROOT_PROJECTION) // The cursor is for the system picker UI and used to display the root from this provider.
        val row = result.newRow()

        row.add(Root.COLUMN_ROOT_ID, ROOT)
        row.add(Root.COLUMN_ICON, R.mipmap.ic_launcher)
        row.add(Root.COLUMN_TITLE, context!!.getString(R.string.app_name)) // COLUMN_TITLE is the root title (e.g. what will be displayed to identify your provider).
        row.add(Root.COLUMN_FLAGS, Root.FLAG_LOCAL_ONLY)
        row.add(Root.COLUMN_MIME_TYPES, getChildMimeTypes(baseDir));
        row.add(Root.COLUMN_DOCUMENT_ID, getDocIdForFile(baseDir)) // Unique and consistent across time. The system picker UI may save it and refer to it later.
        return result
    }

    @Throws(FileNotFoundException::class)
    override fun queryChildDocuments(parentDocumentId: String?, projection: Array<out String>?, sortOrder: String?): Cursor {
        Log.v(TAG, "queryChildDocuments, parentDocumentId: $parentDocumentId sortOrder: $sortOrder");

        val result = MatrixCursor(projection ?: DEFAULT_DOCUMENT_PROJECTION)
        val parent = getFileForDocId(parentDocumentId!!)

        for (file in parent.listFiles()) {
            includeByFile(result, file)
        }

        return result
    }

    override fun queryDocument(documentId: String?, projection: Array<out String>?): Cursor {
        Log.v(TAG, "queryDocument");

        // Create a cursor with the requested projection, or the default projection.
        val result = MatrixCursor(projection ?: DEFAULT_DOCUMENT_PROJECTION)

        documentId?.let { includeByDocId(result, it) } // if (documentId != null) includeByDocId(result, documentId)
        return result;
    }

    override fun openDocument(documentId: String?, mode: String?, signal: CancellationSignal?): ParcelFileDescriptor {
        Log.v(TAG, "openDocument, mode: $mode")

        val file = getFileForDocId(documentId!!)
        val accessMode = ParcelFileDescriptor.parseMode(mode)
        val isWrite = mode?.indexOf('w') ?: -1 != -1

        if (isWrite) { // Attach a close listener if the document is opened in write mode.
            try {
                val handler = Handler(context!!.mainLooper)

                return ParcelFileDescriptor.open(file, accessMode, handler) {
                    Log.i(TAG, "A file with id $documentId has been closed! Time to update the server.")
                }
            } catch (e: IOException) {
                throw FileNotFoundException("Failed to open document with id $documentId and mode $mode")
            }
        } else {
            return ParcelFileDescriptor.open(file, accessMode)
        }

    }

    private fun getChildMimeTypes(parent: File): String {
        val mimeTypes = HashSet<String>()
        val mimeTypesString = StringBuilder()

        mimeTypes.add("image/*")
        mimeTypes.add("text/*")

        for (mimeType in mimeTypes) {
            mimeTypesString.append(mimeType).append("\n")
        }

        return mimeTypesString.toString()
    }

    private fun getDocIdForFile(file: File): String {
        val rootPath = baseDir.getPath()
        var path = file.absolutePath

        if (rootPath == path) {
            path = ""
        } else if (rootPath.endsWith("/")) {
            path = path.substring(rootPath.length)
        } else {
            path = path.substring(rootPath.length + 1)
        }

        Log.d(TAG, "DocIdForFile path: $path")
        return "$ROOT:$path"
    }

    @Throws(FileNotFoundException::class)
    private fun getFileForDocId(docId: String): File {
        var target = baseDir

        if (docId == ROOT) {
            return target
        }

        val splitIndex = docId.indexOf(':', 1)

        if (splitIndex < 0) {
            throw FileNotFoundException("Missing root for " + docId)
        } else {
            val path = docId.substring(splitIndex + 1)

            target = File(target, path)

            if (!target.exists()) {
                throw FileNotFoundException("Missing file for $docId at $target")
            }

            return target
        }
    }

    private fun getTypeForName(name: String): String {
        val lastDot = name.lastIndexOf('.')
        var mime: String? = null

        if (lastDot >= 0) {
            val extension = name.substring(lastDot + 1)

            mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
        }

        return mime ?: "application/octet-stream"
    }

    private fun getTypeForFile(file: File): String {
        if (file.isDirectory) {
            return Document.MIME_TYPE_DIR
        } else {
            return getTypeForName(file.name)
        }
    }

    @Throws(FileNotFoundException::class)
    private fun includeByDocId(result: MatrixCursor, docId: String) {
        val file = getFileForDocId(docId)

        return include(result, docId, file)
    }

    @Throws(FileNotFoundException::class)
    private fun includeByFile(result: MatrixCursor, file: File) {
        val docId = getDocIdForFile(file)

        return include(result, docId, file)
    }

    @Throws(FileNotFoundException::class)
    private fun include(result: MatrixCursor, docId: String, file: File) {
        val flags = 0
        val displayName = file.name
        val mimeType = getTypeForFile(file)
        val row = result.newRow()

        row.add(Document.COLUMN_DOCUMENT_ID, docId)
        row.add(Document.COLUMN_DISPLAY_NAME, displayName)
        row.add(Document.COLUMN_SIZE, file.length())
        row.add(Document.COLUMN_MIME_TYPE, mimeType)
        row.add(Document.COLUMN_LAST_MODIFIED, file.lastModified())
        row.add(Document.COLUMN_FLAGS, flags)
        row.add(Document.COLUMN_ICON, R.mipmap.ic_launcher)
    }

    private fun getResourceIdArray(arrayResId: Int): IntArray {
        val ar = context!!.resources.obtainTypedArray(arrayResId)
        val len = ar.length()
        val resIds = IntArray(len)

        for (i in 0..len - 1) {
            resIds[ i ] = ar.getResourceId(i, 0)
        }

        ar.recycle()
        return resIds
    }

    private fun writeFileToInternalStorage(resId: Int, extension: String) {
        val filename = context!!.resources.getResourceEntryName(resId) + extension
        val ins = context!!.resources.openRawResource(resId)
        val outputStream = ByteArrayOutputStream()
        var fos: FileOutputStream? = null
        var size: Int
        var buffer = ByteArray(1024)

        try {
            while (true) {
                size = ins.read(buffer, 0, 1024)

                if (size < 0) break

                outputStream.write(buffer, 0, size)
            }

            buffer = outputStream.toByteArray()
            fos = context!!.openFileOutput(filename, Context.MODE_PRIVATE)
            fos?.write(buffer)
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            ins.close()
            outputStream.close()
            fos?.close()
        }
    }
}