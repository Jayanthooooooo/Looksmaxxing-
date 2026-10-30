package com.example.data.local

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.CipherInputStream
import javax.crypto.CipherOutputStream
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class PhotoVault(private val context: Context) {

    private val keyAlias = "LumoraPhotoVaultKey"
    private val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
    private val photosDir = File(context.filesDir, "photos").apply { if (!exists()) mkdirs() }

    init {
        getOrCreateKey()
    }

    private fun getOrCreateKey(): SecretKey {
        if (!keyStore.containsAlias(keyAlias)) {
            val keyGenerator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES,
                "AndroidKeyStore"
            )
            val spec = KeyGenParameterSpec.Builder(
                keyAlias,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build()
            keyGenerator.init(spec)
            keyGenerator.generateKey()
        }
        return (keyStore.getEntry(keyAlias, null) as KeyStore.SecretKeyEntry).secretKey
    }

    fun saveEncryptedPhoto(fileId: String, bitmap: Bitmap): File? {
        return try {
            val file = File(photosDir, "$fileId.bin")
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey())
            val iv = cipher.iv

            FileOutputStream(file).use { fos ->
                // Write IV length and IV first
                fos.write(iv.size)
                fos.write(iv)
                CipherOutputStream(fos, cipher).use { cos ->
                    // Re-encode photo as clean JPEG with no metadata
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 85, cos)
                }
            }
            file
        } catch (e: Exception) {
            null
        }
    }

    fun loadDecryptedPhoto(fileId: String): Bitmap? {
        return try {
            val file = File(photosDir, "$fileId.bin")
            if (!file.exists()) return null

            FileInputStream(file).use { fis ->
                val ivSize = fis.read()
                val iv = ByteArray(ivSize)
                fis.read(iv)

                val cipher = Cipher.getInstance("AES/GCM/NoPadding")
                val spec = GCMParameterSpec(128, iv)
                cipher.init(Cipher.DECRYPT_MODE, getOrCreateKey(), spec)

                CipherInputStream(fis, cipher).use { cis ->
                    BitmapFactory.decodeStream(cis)
                }
            }
        } catch (e: Exception) {
            null
        }
    }

    fun deletePhoto(fileId: String): Boolean {
        val file = File(photosDir, "$fileId.bin")
        return if (file.exists()) file.delete() else true
    }

    fun clearAllPhotos(): Boolean {
        var allDeleted = true
        photosDir.listFiles()?.forEach {
            if (!it.delete()) allDeleted = false
        }
        return allDeleted
    }

    fun wipeKey() {
        try {
            if (keyStore.containsAlias(keyAlias)) {
                keyStore.deleteEntry(keyAlias)
            }
        } catch (_: Exception) {}
    }
}
