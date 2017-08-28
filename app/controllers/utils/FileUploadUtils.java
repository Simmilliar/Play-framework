package controllers.utils;

import controllers.repositories.S3FileRepository;
import models.S3File;

import javax.inject.Inject;
import java.io.File;

public class FileUploadUtils {

	private final ImageMagickUtils imageMagickUtils;
	private final S3FileRepository s3FileRepository;

	@Inject
	public FileUploadUtils(ImageMagickUtils imageMagickUtils, S3FileRepository s3FileRepository) {
		this.imageMagickUtils = imageMagickUtils;
		this.s3FileRepository = s3FileRepository;
	}

	public String uploadImageAndShrink(File image, int shrinkSize) {
		if (imageMagickUtils.shrinkImage(image.getAbsolutePath(), shrinkSize)) {
			S3File s3File = new S3File();
			s3File.setFile(image);
			s3FileRepository.saveFile(s3File);
			return s3File.getUrl();
		} else {
			return null;
		}
	}

	public String uploadImageAndCropSquared(File image, int cropSize) {
		if (imageMagickUtils.cropImageSquared(image.getAbsolutePath(), cropSize)) {
			S3File s3File = new S3File();
			s3File.setFile(image);
			s3FileRepository.saveFile(s3File);
			return s3File.getUrl();
		} else {
			return null;
		}
	}
}