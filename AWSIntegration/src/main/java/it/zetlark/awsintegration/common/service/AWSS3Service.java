package it.zetlark.awsintegration.common.service;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectRequest;
import it.zetlark.awsintegration.application.config.DocumentProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@Service
@Slf4j
public class AWSS3Service {

	@Autowired
	private AmazonS3 amazonS3;

	@Value("${s3.bucket.name}")
	private String s3BucketName;
	@Autowired
	private DocumentProperties documentProperties;

	@Async
	public String upload(final File file, String awsDestPath) {
		log.info("Uploading file with name {}", file.getName());
		String filePath = replaceSlash(documentProperties.getAwsS3BaseFolder()) + awsDestPath;
		try {
			final PutObjectRequest putObjectRequest = new PutObjectRequest(s3BucketName, filePath, file);
			amazonS3.putObject(putObjectRequest);
		} catch (AmazonServiceException e) {
			log.error("Error {} occurred while uploading file", e.getLocalizedMessage());

		}
		return URLDecoder.decode(amazonS3.getUrl(s3BucketName, filePath).toString(), StandardCharsets.UTF_8);
	}

	@Async
	public String upload(final MultipartFile multipartFile, String awsDestPath) {
		final File file = convertMultiPartFileToFile(multipartFile);
		final String fileName = file.getName();
		log.info("Uploading file with name {}", fileName);
		String filePath = replaceSlash(documentProperties.getAwsS3BaseFolder()) + awsDestPath;
		try {
			final PutObjectRequest putObjectRequest = new PutObjectRequest(s3BucketName, filePath + fileName, file);
			amazonS3.putObject(putObjectRequest);
		} catch (AmazonServiceException e) {
			log.error("Error {} occurred while uploading file", e.getLocalizedMessage());
		}
		return URLDecoder.decode(amazonS3.getUrl(s3BucketName, filePath).toString(), StandardCharsets.UTF_8);
	}

	public File convertMultiPartFileToFile(final MultipartFile multipartFile) {
		final File file = new File(multipartFile.getOriginalFilename());
		try (final FileOutputStream outputStream = new FileOutputStream(file)) {
			outputStream.write(multipartFile.getBytes());
		} catch (IOException e) {
			log.error("Error {} occurred while converting the multipart file", e.getLocalizedMessage());
		}
		return file;
	}

	private String replaceSlash(String filePath) {
		final String path = filePath.replaceAll("\\\\", "/");
		return path;
	}
}
