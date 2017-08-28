package controllers.utils;

import controllers.repositories.S3FileRepository;
import models.S3File;

import javax.inject.Inject;
import java.io.File;

public class FileUploader {

	private final ImageMagickService imageMagickService;
	private final S3FileRepository s3FileRepository;

	@Inject
	public FileUploader(ImageMagickService imageMagickService, S3FileRepository s3FileRepository) {
		this.imageMagickService = imageMagickService;
		this.s3FileRepository = s3FileRepository;
	}

	public String uploadImageAndShrink(File image, int shrinkSize) {
		if (imageMagickService.shrinkImage(image.getAbsolutePath(), shrinkSize)) {
			S3File s3File = new S3File();
			s3File.file = image;
			s3FileRepository.saveFile(s3File);
			return s3File.getUrl();
		} else {
			return null;
		}
	}

	public String uploadImageAndCropSquared(File image, int cropSize) {
		if (imageMagickService.cropImageSquared(image.getAbsolutePath(), cropSize)) {
			S3File s3File = new S3File();
			s3File.file = image;
			s3FileRepository.saveFile(s3File);
			return s3File.getUrl();
		} else {
			return null;
		}
	}
}