package controllers.utils;

import org.im4java.core.ConvertCmd;
import org.im4java.core.IMOperation;

public class ImageMagickService
{
	public boolean shrinkImage(String fileUrl, int maxDimen)
	{
		ConvertCmd convertCmd = new ConvertCmd();
		IMOperation imOperation = new IMOperation();
		imOperation.addImage(fileUrl);
		imOperation.resize(maxDimen, maxDimen, '>');
		imOperation.addImage(fileUrl);
		try
		{
			convertCmd.run(imOperation);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public boolean cropImageSquared(String fileUrl, int size)
	{
		ConvertCmd convertCmd = new ConvertCmd();
		IMOperation imOperation = new IMOperation();
		imOperation.addImage(fileUrl);
		imOperation.resize(size, size, '^');
		imOperation.gravity("Center");
		imOperation.crop(size, size, 0, 0);
		imOperation.addImage(fileUrl);
		try
		{
			convertCmd.run(imOperation);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return false;
		}
		return true;
	}
}