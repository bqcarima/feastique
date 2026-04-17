package com.qinet.feastique.service.media

import com.qinet.feastique.model.entity.image.VendorImage
import com.qinet.feastique.repository.user.CustomerRepository
import com.qinet.feastique.repository.user.VendorRepository
import com.qinet.feastique.security.UserSecurity
import com.qinet.feastique.utility.SecurityUtility
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.server.ResponseStatusException
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import software.amazon.awssdk.services.s3.model.S3Exception
import java.util.*

@Service
class MediaService(
    private val s3Client: S3Client,
    @Value($$"${aws.s3.bucket-name}") private val bucketName: String,
    @Value($$"${aws.s3.endpoint}") private val endpoint: String,
    private val vendorRepository: VendorRepository,
    private val customerRepository: CustomerRepository,
    val securityUtility: SecurityUtility
) {
    val logger: Logger = LoggerFactory.getLogger(javaClass)
    @Transactional
    fun updateCustomerDisplayPicture(customerDetails: UserSecurity, file: MultipartFile): String {
        validateImageFile(file)
        val customer = customerRepository.findById(customerDetails.id)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found") }

        customer.displayPicture?.let {oldUrl ->
            val oldKey = extractKeyFromUrl(oldUrl)
            deleteFileFromS3(oldKey)
        }

        val newImageUrl = uploadFileToS3(file, "customers/${customer.id}/display-picture")
        customer.displayPicture = newImageUrl
        customerRepository.save(customer)
        return newImageUrl
    }

    @Transactional
    fun addVendorPreviewImage(vendorDetails: UserSecurity, file: MultipartFile): String {
        validateImageFile(file)

        val vendor = vendorRepository.findById(vendorDetails.id)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Vendor not found") }

        if (vendor.previewImages.size >= 5) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Maximum 5 preview images allowed")
        }

        val key = "vendors/${vendorDetails.id}/preview/${UUID.randomUUID()}"
        val imageUrl = uploadFileToS3(file, key)

        val vendorImage = VendorImage().apply {
            this.imageUrl = imageUrl
            this.vendor = vendor
        }

        vendor.previewImages.add(vendorImage)
        vendorRepository.save(vendor)

        return imageUrl
    }

    @Transactional
    fun removeVendorPreviewImage(vendorDetails: UserSecurity, imageId: UUID) {
        val vendor = vendorRepository.findById(vendorDetails.id)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Vendor not found") }

        val image = vendor.previewImages.find { it.id == imageId }
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Preview image not found for this vendor")

        // Delete from S3
        val key = extractKeyFromUrl(image.imageUrl!!)
        deleteFileFromS3(key)

        vendor.previewImages.remove(image)
        vendorRepository.save(vendor)
    }

    fun getVendorPreviewImages(vendorDetails: UserSecurity): Set<String> {
        val vendor = vendorRepository.findById(vendorDetails.id)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Vendor not found") }
        return vendor.previewImages.map { it.imageUrl!! }.toSet()
    }

    @Transactional
    fun updateVendorDisplayPicture(vendorDetails: UserSecurity, file: MultipartFile): String {
        validateImageFile(file)
        val vendor = vendorRepository.findById(vendorDetails.id)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Vendor not found") }

        vendor.displayPicture?.let { oldUrl ->
            val oldKey = extractKeyFromUrl(oldUrl)
            deleteFileFromS3(oldKey)
        }

        val newImageUrl = uploadFileToS3(file, "vendors/${vendor.id}/display-picture")
        vendor.displayPicture = newImageUrl
        vendorRepository.save(vendor)
        return newImageUrl
    }

    private fun validateImageFile(file: MultipartFile) {
        val contentType = file.contentType
        if (contentType == null || !contentType.startsWith("image/")) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid file type. Only image files are allowed.")
        }

        if (file.size > 5 * 1024 * 1024) { // 5MB limit
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "File size exceeds the maximum allowed limit of 5MB.")
        }
    }

    private fun uploadFileToS3(file: MultipartFile, key: String): String {
        val request = PutObjectRequest
            .builder()
            .bucket(bucketName)
            .key(key)
            .contentType(file.contentType)
            .build()

        s3Client.putObject(request, RequestBody.fromBytes(file.bytes))
        return "$endpoint/$bucketName/$key"
    }

    private fun deleteFileFromS3(key: String) {
        try {
            val request = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build()
            s3Client.deleteObject(request)
        } catch (e: S3Exception) {
            logger.warn("Failed to delete $key", e)
        }
    }

    private fun extractKeyFromUrl(url: String): String {
        val prefix = "$endpoint/$bucketName/"
        return if (url.startsWith(prefix)) url.substring(prefix.length)else url
    }
}

